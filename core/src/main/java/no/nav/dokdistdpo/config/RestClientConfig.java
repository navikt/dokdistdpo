package no.nav.dokdistdpo.config;

import no.nav.dokdistdpo.consumer.token.altinn3.Altinn3TokenExchangeConsumer;
import no.nav.dokdistdpo.consumer.token.altinn3.Altinn3TokenInterceptor;
import no.nav.dokdistdpo.consumer.token.naistexas.NaisTexasInterceptor;
import no.nav.dokdistdpo.consumer.token.naistexas.NaisTexasTokenConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient altinn3AuthorizeRestClient(Altinn3TokenExchangeConsumer altinn3TokenExchangeConsumer) {
		return RestClient.builder()
				.requestFactory(jdkClientHttpRequestFactory())
				.requestInterceptor(new Altinn3TokenInterceptor(altinn3TokenExchangeConsumer))
				.build();
	}

	@Bean
	public RestClient maskinportenAuthDetailsRestClient(NaisTexasTokenConsumer naisTexasTokenConsumer) {
		return RestClient.builder()
				.requestFactory(jdkClientHttpRequestFactory())
				.requestInterceptor(new NaisTexasInterceptor(naisTexasTokenConsumer))
				.build();
	}

	private static JdkClientHttpRequestFactory jdkClientHttpRequestFactory() {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(Duration.ofSeconds(20));
		return factory;
	}
}
