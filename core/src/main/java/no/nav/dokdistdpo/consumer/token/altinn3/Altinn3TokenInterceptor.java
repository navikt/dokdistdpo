package no.nav.dokdistdpo.consumer.token.altinn3;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

import static no.nav.dokdistdpo.consumer.token.naistexas.NaisTexasInterceptor.MASKINPORTEN_TARGET_SCOPES;

public class Altinn3TokenInterceptor implements ClientHttpRequestInterceptor {

	private final Altinn3TokenExchangeConsumer altinn3TokenExchangeConsumer;

	public Altinn3TokenInterceptor(Altinn3TokenExchangeConsumer altinn3TokenExchangeConsumer) {
		this.altinn3TokenExchangeConsumer = altinn3TokenExchangeConsumer;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		Map<String, Object> attributes = request.getAttributes();

		if (attributes.containsKey(MASKINPORTEN_TARGET_SCOPES)) {
			String maskinportenTargetScopes = (String) attributes.get(MASKINPORTEN_TARGET_SCOPES);
			request.getHeaders().setBearerAuth(altinn3TokenExchangeConsumer.getAltinnToken(maskinportenTargetScopes));
		}
		return execution.execute(request, body);
	}
}
