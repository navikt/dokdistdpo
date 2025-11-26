package no.nav.dokdistdpo.consumer.dokdistadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.FeilregistrerForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Forsendelse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentEformidlingforsendelserResponse;
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
import static java.lang.String.valueOf;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_DOKDISTADMIN;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_FORSENDELSE_ID;
import static no.nav.dokdistdpo.constant.MDCConstant.CALL_ID;
import static no.nav.dokdistdpo.constant.NavHeaders.NAV_CALLID;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Slf4j
@Component
public class DokdistAdminConsumer {

	private static final String DISTRIBUSJONSKANAL_DPO = "DPO";

	private final RestClient dokdistadminRestClient;
	private final ObjectMapper objectMapper;

	public DokdistAdminConsumer(RestClient dokdistadminRestClient,
								ObjectMapper objectMapper) {
		this.dokdistadminRestClient = dokdistadminRestClient;
		this.objectMapper = objectMapper;
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public HentForsendelseResponse hentForsendelse(Long forsendelseId) {
		log.info("hentForsendelse henter forsendelse med forsendelseId={}", forsendelseId);

		return dokdistadminRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/{forsendelseId}")
						.build(forsendelseId))
				.header(NAV_CALLID, MDC.get(CALL_ID))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> dokdistadminHandleError(res, "hentForsendelse", PROPERTY_FORSENDELSE_ID, valueOf(forsendelseId)))
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
						dokdistadminHandleError(res, "feilregistrerForsendelse", PROPERTY_FORSENDELSE_ID, feilregistrerForsendelse.forsendelseId().toString()))
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
				.body(oppdaterForsendelse)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) ->
						dokdistadminHandleError(res, "oppdaterForsendelse", PROPERTY_FORSENDELSE_ID, oppdaterForsendelse.forsendelseId().toString()))
				.toBodilessEntity();

		log.info("oppdaterForsendelse har oppdatert forsendelse med forsendelseId={} og forsendelseStatus={}", oppdaterForsendelse.forsendelseId(), oppdaterForsendelse.forsendelseStatus());
	}

	public HentEformidlingforsendelserResponse hentEformidlingForsendelser() {
		return dokdistadminRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/henteformidlingforsendelser")
						.queryParam("distribusjonKanal", DISTRIBUSJONSKANAL_DPO)
						.build())
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) ->
						dokdistadminHandleError(res, "hentEformidlingForsendelser"))
				.body(HentEformidlingforsendelserResponse.class);

	}

	private void dokdistadminHandleError(ClientHttpResponse response, String tjeneste, String feltnavn, String feltVerdi) throws IOException {
		ProblemDetail problemDetail = objectMapper.readValue(response.getBody(), ProblemDetail.class);
		if (response.getStatusCode().is4xxClientError()) {
			throw new DokdistadminFunctionalException(format("%s feilet funksjonelt med %s=%s. Feilmelding=%s",
					tjeneste, feltnavn, feltVerdi, problemDetail.getDetail()), problemDetail);
		}
		throw new DokdistadminTechnicalException(format("%s feilet teknisk med forsendelseId=%s, feilmelding=%s",
				tjeneste, feltVerdi, problemDetail.getDetail()), problemDetail);
	}

	private void dokdistadminHandleError(ClientHttpResponse response, String tjeneste) throws IOException {
		ProblemDetail problemDetail = objectMapper.readValue(response.getBody(), ProblemDetail.class);
		if (response.getStatusCode().is4xxClientError()) {
			throw new DokdistadminFunctionalException(format("%s feilet funksjonelt. Feilmelding=%s",
					tjeneste, problemDetail.getDetail()), problemDetail);
		}
		throw new DokdistadminTechnicalException(format("%s feilet teknisk, feilmelding=%s",
				tjeneste, problemDetail.getDetail()), problemDetail);
	}
}
