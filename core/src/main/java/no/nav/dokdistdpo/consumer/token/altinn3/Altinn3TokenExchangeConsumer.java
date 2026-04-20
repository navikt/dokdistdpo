package no.nav.dokdistdpo.consumer.token.altinn3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.technical.Altinn3BrokerTechnicalException;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ProblemDetail;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.util.Objects.requireNonNull;
import static no.nav.dokdistdpo.config.cache.LocalCacheConfig.ALTINN3_TOKEN_CACHE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class Altinn3TokenExchangeConsumer {

	private final RestClient maskinportenAuthDetailsRestClient;
	private final ObjectMapper objectMapper;

	public Altinn3TokenExchangeConsumer(RestClient maskinportenAuthDetailsRestClient,
										ObjectMapper objectMapper,
										DokdistdpoProperties dokdistdpoProperties) {
		this.objectMapper = objectMapper;
		this.maskinportenAuthDetailsRestClient = maskinportenAuthDetailsRestClient
				.mutate()
				.baseUrl(dokdistdpoProperties.altinn3().url())
				.build();
	}

	@Cacheable(ALTINN3_TOKEN_CACHE)
	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public String getAltinnToken() {
		return maskinportenAuthDetailsRestClient.get()
				.uri("/authentication/api/v1/exchange/maskinporten")
				.accept(APPLICATION_JSON)
				.exchange((_, res) -> {
					if (res.getStatusCode().isError()) {
						ProblemDetail problemDetail = objectMapper.readValue(res.getBody(), ProblemDetail.class);
						throw new Altinn3BrokerTechnicalException("Teknisk feilet mot Altinn3 ved exchange av maskinporten token med feilmelding=" + problemDetail);
					}
					return requireNonNull(res.bodyTo(TextNode.class)).textValue();
				});
	}
}
