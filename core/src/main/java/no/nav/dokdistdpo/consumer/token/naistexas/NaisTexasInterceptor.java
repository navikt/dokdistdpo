package no.nav.dokdistdpo.consumer.token.naistexas;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

public class NaisTexasInterceptor implements ClientHttpRequestInterceptor {

	public static final String MASKINPORTEN_TARGET_SCOPES = "maskinportenTargetScopes";

	private final NaisTexasTokenConsumer naistexasTokenConsumer;

	public NaisTexasInterceptor(NaisTexasTokenConsumer naistexasTokenConsumer) {
		this.naistexasTokenConsumer = naistexasTokenConsumer;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		Map<String, Object> attributes = request.getAttributes();
		if (attributes.containsKey(MASKINPORTEN_TARGET_SCOPES)) {
			request.getHeaders().setBearerAuth(naistexasTokenConsumer.getMaskinportenMedAuthorizationDetails());
		}
		return execution.execute(request, body);
	}
}
