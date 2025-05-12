package no.nav.dokdistdpo.consumer.ereg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.functional.EregFunctionalException;
import no.nav.dokdistdpo.exception.technical.EregTechnicalException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static java.lang.String.format;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.getProblemDetail;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class EregConsumer {

	private final RestClient restClient;

	public EregConsumer(RestClient restClient,
						DokdistdpoProperties dokdistdpoProperties) {
		this.restClient = restClient
				.mutate()
				.baseUrl(dokdistdpoProperties.endpoints().ereg().scope())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}


	@Retryable(retryFor = EregTechnicalException.class)
	public String hentOrganisasjonsnavn(String orgnr) {
		log.info("Henter organisasjonsnavn for orgnr={}", orgnr);

		EregResponse eregResponse = restClient.get()
				.uri("{orgnummer}/noekkelinfo", orgnr.strip())
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res, orgnr))
				.body(EregResponse.class);

		if (isOrganisasjonsnavnNull(eregResponse)) {
			throw new EregFunctionalException("Organisasjonsnavn mangler i Enhetsregisteret for orgnr=" + orgnr, null);
		}

		return eregResponse.navn().sammensattnavn();
	}

	private void handleError(ClientHttpResponse response, String orgnr) throws IOException {
		ProblemDetail problemDetail = getProblemDetail(response.getBody());
		if (response.getStatusCode().is4xxClientError()) {
			throw new EregFunctionalException(format("Kall mot Ereg feilet funksjonelt med feilmelding=%s, status=%s for orgnr=%s",
					response.getStatusText(), response.getStatusCode(), orgnr), problemDetail);
		}

		throw new EregTechnicalException(format("Kall mot Ereg feilet teknisk med status=%s, feilmelding=%s for orgnr=%s",
				response.getStatusCode(), response.getStatusText(), orgnr), problemDetail);
	}

	private boolean isOrganisasjonsnavnNull(EregResponse eregResponse) {
		return (eregResponse == null || (eregResponse.navn() == null || isBlank(eregResponse.navn().sammensattnavn())));
	}
}
