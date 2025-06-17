package no.nav.dokdistdpo.azure;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.io.IOException;

public class OAuth2AuthInterceptor implements ClientHttpRequestInterceptor {

	private final OAuth2AuthorizedClientManager authorizedClientManager;
	private final ClientRegistration clientRegistration;

	public OAuth2AuthInterceptor(OAuth2AuthorizedClientManager authorizedClientManager,
								 ClientRegistration clientRegistration) {
		this.authorizedClientManager = authorizedClientManager;
		this.clientRegistration = clientRegistration;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(OAuth2AuthorizeRequest
				.withClientRegistrationId(clientRegistration.getRegistrationId())
				.principal(clientRegistration.getClientId())
				.build());

		request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
		return execution.execute(request, body);
	}
}
