package no.nav.dokdistdpo.azure;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.io.IOException;

public record OAuth2TokenInterceptor(OAuth2AuthorizedClientManager authorizedClientManager,
									 String clientRegistrationId) implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(
				OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId).build());

		if (authorizedClient != null) {
			OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
			if (accessToken != null) {
				request.getHeaders().setBearerAuth(accessToken.getTokenValue());
			}
		}
		return execution.execute(request, body);
	}
}
