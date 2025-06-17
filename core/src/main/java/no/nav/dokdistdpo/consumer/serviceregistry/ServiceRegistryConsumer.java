package no.nav.dokdistdpo.consumer.serviceregistry;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.exception.technical.ServiceRegistryTechnicalException;
import no.nav.dokdistdpo.utils.DokdistdpoUtils;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.lang.String.format;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_MASKINPORTEN;
import static no.nav.dokdistdpo.constant.MDCConstant.CALL_ID;
import static no.nav.dokdistdpo.constant.NavHeaders.NAV_CALLID;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Slf4j
@Component
public class ServiceRegistryConsumer {

	public static final String TEKNISK_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Teknisk feil: ";
	public static final String FUNKSJONELL_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Funksjonell feil: ";

	private final RestClient maskinportenRestClient;

	public ServiceRegistryConsumer(RestClient maskinportenRestClient) {
		this.maskinportenRestClient = maskinportenRestClient;
	}

	@Retryable(retryFor = ServiceRegistryTechnicalException.class)
	public IdentifierResource getIdentifierResource(final String orgnummer, final String processIdentifier) {
		return maskinportenRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/identifier/{orgnummer}/process/" + processIdentifier)
						.build(orgnummer))
				.header(NAV_CALLID, MDC.get(CALL_ID))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_MASKINPORTEN))
				.exchange((req, res) -> {
					if (res.getStatusCode().isError()) {
						ProblemDetail problemDetail = DokdistdpoUtils.getProblemDetail(res);

						if (res.getStatusCode().is4xxClientError()) {
							log.warn(FUNKSJONELL_FEIL_ERROR_MESSAGE + "{}", problemDetail.getDetail());
							return null;
						}
						final String errorMessage = TEKNISK_FEIL_ERROR_MESSAGE + problemDetail.getDetail();
						log.error(errorMessage);
						throw new ServiceRegistryTechnicalException(format("hentForsendelse feilet teknisk med feilmelding=%s", problemDetail));
					}
					return res.bodyTo(IdentifierResource.class);
				});
	}
}
