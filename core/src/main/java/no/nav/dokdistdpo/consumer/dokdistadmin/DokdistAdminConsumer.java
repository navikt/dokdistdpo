package no.nav.dokdistdpo.consumer.dokdistadmin;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.FeilregistrerForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OpprettForsendelseRequest;
import no.nav.dokdistdpo.exception.functional.DokdistadminFunctionalException;
import no.nav.dokdistdpo.exception.technical.DokdistadminTechnicalException;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.lang.String.format;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_DOKDISTADMIN;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.getProblemDetail;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Slf4j
@Component
public class DokdistAdminConsumer {

	private final RestClient restClient;

	public DokdistAdminConsumer(RestClient.Builder restClientBuilder,
								DokdistdpoProperties dokdistdpoProperties) {
		this.restClient = restClientBuilder
				.baseUrl(dokdistdpoProperties.endpoints().dokdistadmin().url())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public HentForsendelseResponse hentForsendelse(String forsendelseId) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/{forsendelseId}")
						.build(forsendelseId))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					ProblemDetail problemDetail = getProblemDetail(res.getBody());
					if (res.getStatusCode().is4xxClientError()) {
						throw new DokdistadminFunctionalException(format("hentForsendelse feilet funksjonelt med forsendelseId=%s. Feilmelding=%s",
								forsendelseId, res.getStatusText()), problemDetail);
					}
					throw new DokdistadminTechnicalException(format("hentForsendelse feilet teknisk med forsendelseId=%s, feilmelding=%s",
							forsendelseId, res.getStatusText()), problemDetail);
				})
				.body(HentForsendelseResponse.class);
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public Long opprettForsendelse(OpprettForsendelseRequest opprettForsendelseRequest) {
		return restClient.post()
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.body(opprettForsendelseRequest)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					ProblemDetail problemDetail = getProblemDetail(res.getBody());
					if (res.getStatusCode().is4xxClientError()) {
						throw new DokdistadminFunctionalException(format("opprettForsendelse feilet funksjonelt med bestillingsId=%s. Feilmelding=%s",
								opprettForsendelseRequest.bestillingsId(), res.getStatusText()), problemDetail);
					}
					throw new DokdistdpoTechnicalException(format("opprettForsendelse feilet teknisk med bestillingsId=%s, feilmelding=%s",
							opprettForsendelseRequest.bestillingsId(), res.getStatusText()));
				})
				.body(Long.class);
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public void feilregistrerForsendelse(FeilregistrerForsendelseRequest feilregistrerForsendelse) {

		log.info("feilregistrerForsendelse feilregistrerer forsendelse med forsendelseId={}", feilregistrerForsendelse.forsendelseId());

		restClient.put()
				.uri("/feilregistrerforsendelse")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.body(feilregistrerForsendelse)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					ProblemDetail problemDetail = getProblemDetail(res.getBody());
					if (res.getStatusCode().is4xxClientError()) {
						throw new DokdistadminFunctionalException(format("feilregistrerForsendelse feilet funksjonelt med forsendelseId=%s. Feilmelding=%s",
								feilregistrerForsendelse.forsendelseId(), problemDetail.getDetail()), problemDetail);
					}
					throw new DokdistdpoTechnicalException(format("feilregistrerForsendelse feilet teknisk med bestillingsId=%s, feilmelding=%s",
							feilregistrerForsendelse.forsendelseId(), res.getStatusText()));

				})
				.toBodilessEntity();

		log.info("feilregistrerForsendelse har feilregistrert forsendelse med forsendelseId={}", feilregistrerForsendelse.forsendelseId());
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelse) {
		log.info("oppdaterForsendelse oppdaterer forsendelse med forsendelseId={}", oppdaterForsendelse.forsendelseId());

		restClient.put()
				.uri("/oppdaterforsendelse")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					ProblemDetail problemDetail = getProblemDetail(res.getBody());
					if (res.getStatusCode().is4xxClientError()) {
						throw new DokdistadminFunctionalException(format("oppdaterForsendelse feilet funksjonelt med forsendelseId=%s. Feilmelding=%s",
								oppdaterForsendelse.forsendelseId(), res.getStatusText()), problemDetail);
					}
					throw new DokdistdpoTechnicalException(format("oppdaterForsendelse feilet teknisk med forsendelseId=%s, feilmelding=%s",
							oppdaterForsendelse.forsendelseId(), res.getStatusText()));
				}).toBodilessEntity();

		log.info("oppdaterForsendelse har oppdatert forsendelse med forsendelseId={} og forsendelseStatus={}", oppdaterForsendelse.forsendelseId(), oppdaterForsendelse.forsendelseStatus());
	}
}
