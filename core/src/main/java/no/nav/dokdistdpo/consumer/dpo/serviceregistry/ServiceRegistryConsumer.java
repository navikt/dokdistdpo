package no.nav.dokdistdpo.consumer.dpo.serviceregistry;

import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.technical.ServiceRegistryTechnicalException;
import org.springframework.http.ProblemDetail;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class ServiceRegistryConsumer {

	private final RestClient maskinportenAuthorizedRestClient;
	private final JsonMapper jsonMapper;

	public ServiceRegistryConsumer(RestClient maskinportenAuthorizedRestClient,
								   DokdistdpoProperties dokdistdpoProperties,
								   JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
		this.maskinportenAuthorizedRestClient = maskinportenAuthorizedRestClient.mutate()
				.baseUrl(dokdistdpoProperties.serviceRegistry().url())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Retryable(includes = ServiceRegistryTechnicalException.class)
	public IdentifierResource getIdentifierResource(final String orgnummer, final String processIdentifier) {
		return maskinportenAuthorizedRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/identifier/{orgnummer}/process/" + processIdentifier)
						.build(orgnummer))
				.exchange((request, response) -> {
					if (response.getStatusCode().isError()) {
						ProblemDetail problemDetail = jsonMapper.readValue(response.getBody(), ProblemDetail.class);

						final String errorMessage = String.format("Serviceregistry feilet med statuskode=%s og feilmelding=%s", problemDetail.getStatus(), problemDetail.getDetail());
						log.error(errorMessage);
						throw new ServiceRegistryTechnicalException(errorMessage);
					}
					return response.bodyTo(IdentifierResource.class);
				});
	}
}
