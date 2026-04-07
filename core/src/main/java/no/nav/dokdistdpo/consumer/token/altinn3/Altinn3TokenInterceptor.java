package no.nav.dokdistdpo.consumer.token.altinn3;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class Altinn3TokenInterceptor implements ClientHttpRequestInterceptor {

	private final Altinn3TokenExchangeConsumer altinn3TokenExchangeConsumer;

	public Altinn3TokenInterceptor(Altinn3TokenExchangeConsumer altinn3TokenExchangeConsumer) {
		this.altinn3TokenExchangeConsumer = altinn3TokenExchangeConsumer;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		request.getHeaders().setBearerAuth(altinn3TokenExchangeConsumer.getAltinnToken());
		return execution.execute(request, body);
	}
}
