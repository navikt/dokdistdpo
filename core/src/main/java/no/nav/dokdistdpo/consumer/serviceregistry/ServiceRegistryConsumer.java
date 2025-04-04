package no.nav.dokdistdpo.consumer.serviceregistry;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.consumer.maskinporten.MaskinportenConsumer;
import no.nav.dokdistdpo.exception.technical.ServiceRegistryTechnicalException;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class ServiceRegistryConsumer {

	public static final String TEKNISK_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Teknisk feil: ";
	public static final String FUNKSJONELL_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Funksjonell feil: ";

	private final RestClient restClient;

	public ServiceRegistryConsumer(MaskinportenConsumer maskinportenConsumer,
								   DokdistdpoProperties dokdistdpoProperties,
								   RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder
				.baseUrl(dokdistdpoProperties.serviceRegistry().url())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setContentType(MediaType.APPLICATION_JSON);
					httpHeaders.setBearerAuth(maskinportenConsumer.fetchMaskinportenToken().accessToken());
				})
				.build();
	}

	@Retryable(retryFor = ServiceRegistryTechnicalException.class)
	public IdentifierResource getIdentifierResource(final String orgnummer, final String processIdentifier) {
		try {
			return restClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("identifier/{orgnummer}/process/{processIdentifier}")
							.build(orgnummer, processIdentifier))
					.retrieve()
					.body(IdentifierResource.class);
		} catch (RestClientResponseException e) {
			if (e.getStatusCode().is4xxClientError()) {
				log.warn(FUNKSJONELL_FEIL_ERROR_MESSAGE + "{}", e.getResponseBodyAsString());
				return IdentifierResource.empty();
			}
			final String errorMessage = TEKNISK_FEIL_ERROR_MESSAGE + e.getMessage();
			log.error(errorMessage, e);
			throw new ServiceRegistryTechnicalException(errorMessage, e);
		}
	}
}
