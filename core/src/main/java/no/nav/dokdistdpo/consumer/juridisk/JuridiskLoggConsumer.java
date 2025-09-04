package no.nav.dokdistdpo.consumer.juridisk;

import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.functional.LagreJuridiskLoggFunctionalException;
import no.nav.dokdistdpo.exception.technical.LagreJuridiskLoggTechnicalException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static java.lang.String.format;
import static no.nav.dokdistdpo.constant.MDCConstant.CALL_ID;
import static no.nav.dokdistdpo.constant.NavHeaders.NAV_CALLID;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class JuridiskLoggConsumer {

	private final RestClient restClient;

	public JuridiskLoggConsumer(RestClient.Builder restClientBuilder,
								DokdistdpoProperties dokdistdpoProperties) {
		this.restClient = restClientBuilder
				.baseUrl(dokdistdpoProperties.juridisklogg().url())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setContentType(APPLICATION_JSON);
					httpHeaders.setBasicAuth(dokdistdpoProperties.serviceuser().username(), dokdistdpoProperties.serviceuser().password());
				})
				.build();
	}

	@Retryable(retryFor = LagreJuridiskLoggTechnicalException.class)
	public LoggmeldingResponse lagreJuridisklogg(final LoggmeldingRequest loggmeldingRequest) {
		try {
			return restClient.post()
					.uri("/api/rest/logg")
					.header(NAV_CALLID, MDC.get(CALL_ID))
					.body(loggmeldingRequest)
					.retrieve()
					.onStatus(HttpStatusCode::isError, (req, res) -> {
						if (res.getStatusCode().is4xxClientError()) {
							throw new LagreJuridiskLoggFunctionalException(format("lagreJuridisklogg feilet funksjonelt med status=%s, feilmelding=%s", res.getStatusCode(), res.getStatusText()));
						}
						throw new LagreJuridiskLoggTechnicalException(format("lagreJuridiskLogg feilet teknisk med status=%s, feilmelding=%s", res.getStatusCode(), res.getStatusText()));
					}).body(LoggmeldingResponse.class);
		} catch (ResourceAccessException e) {
			// For å få retry ved følgende feil:
			// org.springframework.web.client.ResourceAccessException: I/O error on POST request for "https://app.adeo.no/juridisklogg/api/rest/logg": Connection reset
			throw new LagreJuridiskLoggTechnicalException(format("lagreJuridiskLogg feilet teknisk. Feilmelding=%s", e.getMessage()), e);
		}
	}
}
