package no.nav.dokdistdpo.consumer.maskinporten;

import no.nav.dokdistdpo.config.properties.MaskinportenProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@Disabled
class MaskinportenConsumerTest {

	private MaskinportenProperties maskinportenProperties;
	private RestClient.Builder restClientBuilder;
	@BeforeEach
	public void setup() {
		// test
		maskinportenProperties = new MaskinportenProperties("https://test.maskinporten.no/",
				System.getProperty("{maskinporten.client-id}"),
				"move/dpo.read",
				"https://test.maskinporten.no/token",
				System.getProperty("{maskinporten.client-jwk}"));

		restClientBuilder = RestClient.builder();
	}

	@Test
	void shouldFetchTokenWhenSystemPropertiesSet() {
		MaskinportenConsumer oidcTokenClient = new MaskinportenConsumer(maskinportenProperties, restClientBuilder);
		final OidcTokenResponse oidcTokenResponse = oidcTokenClient.fetchMaskinportenToken();
		System.out.println(oidcTokenResponse.accessToken());
	}
}