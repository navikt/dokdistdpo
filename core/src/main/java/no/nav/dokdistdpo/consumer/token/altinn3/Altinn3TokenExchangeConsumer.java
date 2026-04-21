package no.nav.dokdistdpo.consumer.token.altinn3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.consumer.token.naistexas.NaisTexasTokenConsumer;
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

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final NaisTexasTokenConsumer naisTexasTokenConsumer;

	public Altinn3TokenExchangeConsumer(RestClient.Builder restClientBuilder,
										ObjectMapper objectMapper,
										NaisTexasTokenConsumer naisTexasTokenConsumer,
										DokdistdpoProperties dokdistdpoProperties) {
		this.objectMapper = objectMapper;
		this.naisTexasTokenConsumer = naisTexasTokenConsumer;
		this.restClient = restClientBuilder
				.baseUrl(dokdistdpoProperties.altinn3().url())
				.build();
	}

	@Cacheable(ALTINN3_TOKEN_CACHE)
	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public String getAltinnToken() {
		return restClient.get()
				.uri("/authentication/api/v1/exchange/maskinporten")
				.accept(APPLICATION_JSON)
				.headers(httpHeaders -> httpHeaders.setBearerAuth(naisTexasTokenConsumer.maskinportenMedAuthorizationDetails()))
				.exchange((_, res) -> {
					if (res.getStatusCode().isError()) {
						ProblemDetail problemDetail = objectMapper.readValue(res.getBody(), ProblemDetail.class);
						throw new Altinn3BrokerTechnicalException("Teknisk feilet mot Altinn3 ved exchange av maskinporten token med feilmelding=" + problemDetail);
					}
					return requireNonNull(res.bodyTo(TextNode.class)).textValue();
				});
	}
}
