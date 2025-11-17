package no.nav.dokdistdpo.azure;

import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.consumer.dpo.maskinporten.MaskinportenConsumer;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;

@Configuration
public class OAuthEnabledRestClientConfig {

	public static final String CLIENT_REGISTRATION_DOKDISTADMIN = "azure-dokdistadmin";

	@Bean
	public RestClient dokdistadminRestClient(DokdistdpoProperties dokdistdpoProperties,
											 OAuth2AuthorizedClientManager authorizedClientManager) {

		var oauth2Interceptor =
				new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

		return RestClient.builder()
				.baseUrl(dokdistdpoProperties.endpoints().dokdistadmin().url())
				.requestInterceptor(oauth2Interceptor)
				.requestFactory(httpComponentsClientHttpRequestFactory())
				.build();
	}

	@Bean
	public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(Timeout.ofSeconds(15))
				.setResponseTimeout(Timeout.ofSeconds(30))
				.build();

		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.build();

		HttpComponentsClientHttpRequestFactory requestFactory =
				new HttpComponentsClientHttpRequestFactory(httpClient);

		requestFactory.setConnectionRequestTimeout(Duration.ofSeconds(15));
		requestFactory.setReadTimeout(Duration.ofSeconds(30));
		return requestFactory;
	}


	@Bean
	public RestClient maskinportenAuthorizedRestClient(MaskinportenConsumer maskinportenConsumer) {
		return RestClient.builder()
				.requestFactory(jdkClientHttpRequestFactory())
				.requestInterceptor(new MaskinportenRequestInterceptor(maskinportenConsumer))
				.build();
	}

	@Bean
	public OAuth2ClientHttpRequestInterceptor oAuth2ClientHttpRequestInterceptor(
			OAuth2AuthorizedClientManager authorizedClientManager) {

		var oAuth2ClientHttpRequestInterceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
		oAuth2ClientHttpRequestInterceptor.setClientRegistrationIdResolver(clientRegistrationIdResolver());

		return oAuth2ClientHttpRequestInterceptor;
	}

	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
																 OAuth2AuthorizedClientService auth2AuthorizedClientService) {

		var authorizedClientManager =
				new AuthorizedClientServiceOAuth2AuthorizedClientManager(
						clientRegistrationRepository, auth2AuthorizedClientService);


		authorizedClientManager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
				.authorizationCode()
				.refreshToken()
				.clientCredentials()
				.build());

		return authorizedClientManager;
	}

	@Bean
	OAuth2AuthorizedClientService oAuth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}

	@Bean
	ClientRegistrationRepository clientRegistrationRepository(AzureProperties azureProperties, DokdistdpoProperties properties) {
		return new InMemoryClientRegistrationRepository(clientRegistrations(azureProperties, properties));
	}

	List<ClientRegistration> clientRegistrations(AzureProperties azureProperties, DokdistdpoProperties properties) {
		return List.of(ClientRegistration.withRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN)
				.tokenUri(azureProperties.openidConfigTokenEndpoint())
				.clientId(azureProperties.appClientId())
				.clientSecret(azureProperties.appClientSecret())
				.clientAuthenticationMethod(CLIENT_SECRET_BASIC)
				.authorizationGrantType(CLIENT_CREDENTIALS)
				.scope(properties.endpoints().dokdistadmin().scope())
				.build()
		);
	}

	private OAuth2ClientHttpRequestInterceptor.ClientRegistrationIdResolver clientRegistrationIdResolver() {
		return new RequestAttributeClientRegistrationIdResolver();
	}

	private static JdkClientHttpRequestFactory jdkClientHttpRequestFactory() {
		return ClientHttpRequestFactoryBuilder.jdk()
				.withCustomizer(jdkClientHttpRequestFactory ->
						jdkClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(20)))
				.build();
	}
}
