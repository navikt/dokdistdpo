package no.nav.dokdistdpo.consumer.serviceregistry;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.technical.ServiceRegistryTechnicalException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.lang.String.format;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_MASKINPORTEN;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Slf4j
@Component
public class ServiceRegistryConsumer {

	public static final String TEKNISK_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Teknisk feil: ";
	public static final String FUNKSJONELL_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Funksjonell feil: ";

	private final RestClient restClient;

	public ServiceRegistryConsumer(DokdistdpoProperties dokdistdpoProperties,
								   RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder
				.baseUrl(dokdistdpoProperties.serviceRegistry().url())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setContentType(MediaType.APPLICATION_JSON);
				})
				.build();
	}

	@Retryable(retryFor = ServiceRegistryTechnicalException.class)
	public IdentifierResource getIdentifierResource(final String orgnummer, final String processIdentifier) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("identifier/{orgnummer}/process/{processIdentifier}")
						.build(orgnummer, processIdentifier))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_MASKINPORTEN))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					if (res.getStatusCode().is4xxClientError()) {
						log.warn(FUNKSJONELL_FEIL_ERROR_MESSAGE + "{}", res.getStatusText());
						return;
					}
					final String errorMessage = TEKNISK_FEIL_ERROR_MESSAGE + res.getStatusText();
					log.error(errorMessage, res.getStatusText());
					throw new ServiceRegistryTechnicalException(format("hentForsendelse feilet teknisk med feilmelding=%s", res.getStatusText()));
				})
				.body(IdentifierResource.class);
	}
}
