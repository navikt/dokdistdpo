package no.nav.dokdistdpo.consumer.dokdistadmin;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.FeilregistrerForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Forsendelse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OpprettForsendelseRequest;
import no.nav.dokdistdpo.exception.functional.DokdistadminFunctionalException;
import no.nav.dokdistdpo.exception.technical.DokdistadminTechnicalException;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static java.lang.String.format;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_DOKDISTADMIN;
import static no.nav.dokdistdpo.constant.MDCConstant.CALL_ID;
import static no.nav.dokdistdpo.constant.NavHeaders.NAV_CALLID;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.getProblemDetail;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Slf4j
@Component
public class DokdistAdminConsumer {

	private final RestClient dokdistadminRestClient;

	public DokdistAdminConsumer(RestClient dokdistadminRestClient,
								DokdistdpoProperties dokdistdpoProperties) {
		this.dokdistadminRestClient = dokdistadminRestClient;
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public HentForsendelseResponse hentForsendelse(String forsendelseId) {
		log.info("hentForsendelse henter forsendelse med forsendelseId={}", forsendelseId);

		return dokdistadminRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/{forsendelseId}")
						.build(forsendelseId))
				.header(NAV_CALLID, MDC.get(CALL_ID))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					dokdistadminHandleError(res, "hentForsendelse", "forsendelseId", forsendelseId);
				})
				.body(HentForsendelseResponse.class);
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public Forsendelse opprettForsendelse(OpprettForsendelseRequest opprettForsendelseRequest) {
		return dokdistadminRestClient.post()
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.header(NAV_CALLID, MDC.get(CALL_ID))
				.body(opprettForsendelseRequest)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) ->
						dokdistadminHandleError(res, "opprettForsendelse", "bestillingsId", opprettForsendelseRequest.bestillingsId()))
				.body(Forsendelse.class);
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public void feilregistrerForsendelse(FeilregistrerForsendelseRequest feilregistrerForsendelse) {

		log.info("feilregistrerForsendelse feilregistrerer forsendelse med forsendelseId={}", feilregistrerForsendelse.forsendelseId());

		dokdistadminRestClient.put()
				.uri("/feilregistrerforsendelse")
				.header(NAV_CALLID, MDC.get(CALL_ID))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.body(feilregistrerForsendelse)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) ->
						dokdistadminHandleError(res, "feilregistrerForsendelse", "forsendelseId", feilregistrerForsendelse.forsendelseId().toString()))
				.toBodilessEntity();

		log.info("feilregistrerForsendelse har feilregistrert forsendelse med forsendelseId={}", feilregistrerForsendelse.forsendelseId());
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelse) {
		log.info("oppdaterForsendelse oppdaterer forsendelse med forsendelseId={}", oppdaterForsendelse.forsendelseId());

		dokdistadminRestClient.put()
				.uri("/oppdaterforsendelse")
				.header(NAV_CALLID, MDC.get(CALL_ID))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) ->
						dokdistadminHandleError(res, "oppdaterForsendelse", "forsendelseId", oppdaterForsendelse.forsendelseId().toString()))
				.toBodilessEntity();

		log.info("oppdaterForsendelse har oppdatert forsendelse med forsendelseId={} og forsendelseStatus={}", oppdaterForsendelse.forsendelseId(), oppdaterForsendelse.forsendelseStatus());
	}

	private void dokdistadminHandleError(ClientHttpResponse response, String tjeneste, String feltnavn, String feltVerdi) throws IOException {
		ProblemDetail problemDetail = getProblemDetail(response);
		if (response.getStatusCode().is4xxClientError()) {
			throw new DokdistadminFunctionalException(format("%s feilet funksjonelt med %s=%s. Feilmelding=%s",
					tjeneste, feltnavn, feltVerdi, problemDetail.getDetail()), problemDetail);
		}
		throw new DokdistadminTechnicalException(format("%s feilet teknisk med forsendelseId=%s, feilmelding=%s",
				tjeneste, feltVerdi, problemDetail.getDetail()), problemDetail);
	}
}
