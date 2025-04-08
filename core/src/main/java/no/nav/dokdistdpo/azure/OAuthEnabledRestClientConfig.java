package no.nav.dokdistdpo.azure;

import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.config.properties.MaskinportenProperties;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import java.net.ProxySelector;
import java.util.List;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.JWT_BEARER;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.NONE;

@Configuration
public class OAuthEnabledRestClientConfig {

	public static final String CLIENT_REGISTRATION_DOKDISTADMIN = "azure-dokdistadmin";
	public static final String CLIENT_REGISTRATION_MASKINPORTEN = "maskinporten";

	@Bean
	public RestClient restClient(OAuth2AuthorizedClientManager authorizedClientManager) {
		var oauth2Interceptor =
				new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

		oauth2Interceptor.setClientRegistrationIdResolver(clientRegistrationIdResolver());

		return RestClient.builder()
				.requestInterceptor(oauth2Interceptor)
				.build();
	}

	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(
			ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AuthorizedClientService authorizedClientService) {

		ClientCredentialsOAuth2AuthorizedClientProvider auth2AuthorizedClientProvider = new ClientCredentialsOAuth2AuthorizedClientProvider();

		var connectionManager = new PoolingHttpClientConnectionManager();
		HttpClient httpClient = HttpClientBuilder.create()
				.setConnectionManager(connectionManager)
				.setProxySelector(ProxySelector.getDefault())
				.build();

		RestClient restClientWithProxy = RestClient.builder()
				.requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
				.build();

		RestClientClientCredentialsTokenResponseClient tokenResponseClient = new RestClientClientCredentialsTokenResponseClient();
		tokenResponseClient.setRestClient(restClientWithProxy);
		auth2AuthorizedClientProvider.setAccessTokenResponseClient(tokenResponseClient);

		var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
		authorizedClientManager.setAuthorizedClientProvider(auth2AuthorizedClientProvider);

		return authorizedClientManager;
	}

	@Bean
	OAuth2AuthorizedClientService oAuth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}

	@Bean
	ClientRegistrationRepository clientRegistrationRepository(List<ClientRegistration> clientRegistration) {
		return new InMemoryClientRegistrationRepository(clientRegistration);
	}

	@Bean
	List<ClientRegistration> clientRegistration(AzureProperties azureProperties, DokdistdpoProperties properties, MaskinportenProperties maskinportenProperties) {
		return List.of(ClientRegistration.withRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN)
						.tokenUri(azureProperties.openidConfigTokenEndpoint())
						.clientId(azureProperties.appClientId())
						.clientSecret(azureProperties.appClientSecret())
						.clientAuthenticationMethod(CLIENT_SECRET_BASIC)
						.authorizationGrantType(CLIENT_CREDENTIALS)
						.scope(properties.endpoints().dokdistadmin().scope())
						.build(),
				ClientRegistration.withRegistrationId(CLIENT_REGISTRATION_MASKINPORTEN)
						.tokenUri(maskinportenProperties.tokenEndpoint())
						.clientId(maskinportenProperties.clientId())
						.issuerUri(maskinportenProperties.issuer())
						.clientAuthenticationMethod(NONE)
						.authorizationGrantType(JWT_BEARER)
						.scope(maskinportenProperties.scopes())
						.build()
		);
	}

	private OAuth2ClientHttpRequestInterceptor.ClientRegistrationIdResolver clientRegistrationIdResolver() {
		return (request) -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			return (authentication instanceof OAuth2AuthenticationToken principal) ?
					principal.getAuthorizedClientRegistrationId() : null;
		};
	}
}
