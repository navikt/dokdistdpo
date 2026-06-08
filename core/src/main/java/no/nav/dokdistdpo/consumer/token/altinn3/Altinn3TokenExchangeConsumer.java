package no.nav.dokdistdpo.consumer.token.altinn3;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.technical.Altinn3BrokerTechnicalException;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ProblemDetail;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.util.Objects.requireNonNull;
import static no.nav.dokdistdpo.config.cache.LocalCacheConfig.ALTINN3_TOKEN_CACHE;
import static no.nav.dokdistdpo.consumer.token.naistexas.NaisTexasInterceptor.MASKINPORTEN_TARGET_SCOPES;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class Altinn3TokenExchangeConsumer {

	private final JsonMapper jsonMapper;
	private final RestClient maskinportenAuthDetailsRestClient;

	public Altinn3TokenExchangeConsumer(RestClient maskinportenAuthDetailsRestClient,
										JsonMapper jsonMapper,
										DokdistdpoProperties dokdistdpoProperties) {
		this.jsonMapper = jsonMapper;
		this.maskinportenAuthDetailsRestClient = maskinportenAuthDetailsRestClient
				.mutate()
				.baseUrl(dokdistdpoProperties.altinn3().url())
				.build();
	}

	@Cacheable(value = ALTINN3_TOKEN_CACHE, key = "#maskinportenScopes")
	@Retryable(includes = DokdistdpoTechnicalException.class)
	public String getAltinnToken(String maskinportenScopes) {
		return maskinportenAuthDetailsRestClient.get()
				.uri("/authentication/api/v1/exchange/maskinporten")
				.accept(APPLICATION_JSON)
				.attribute(MASKINPORTEN_TARGET_SCOPES, maskinportenScopes)
				.exchange((_, res) -> {
					if (res.getStatusCode().isError()) {
						ProblemDetail problemDetail = jsonMapper.readValue(res.getBody(), ProblemDetail.class);
						throw new Altinn3BrokerTechnicalException("Teknisk feilet mot Altinn3 ved exchange av maskinporten token med feilmelding=" + problemDetail);
					}
					return requireNonNull(res.bodyTo(StringNode.class)).stringValue();
				});
	}
}
