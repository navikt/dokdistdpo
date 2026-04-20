package no.nav.dokdistdpo.consumer.token.naistexas;

import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.config.properties.NaisTexasProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static java.util.Objects.requireNonNull;
import static no.nav.dokdistdpo.consumer.token.naistexas.AuthorizationDetail.authorizationDetails;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Component
public class NaisTexasTokenConsumer {

	private final RestClient restClient;
	private final DokdistdpoProperties.Altinn3Properties altinn3Properties;

	public NaisTexasTokenConsumer(RestClient.Builder restClientBuilder,
								  NaisTexasProperties naistexasProperties,
								  DokdistdpoProperties dokdistdpoProperties) {
		this.restClient = restClientBuilder
				.baseUrl(naistexasProperties.tokenEndpoint())
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
		this.altinn3Properties = dokdistdpoProperties.altinn3();
	}

	public String getMaskinportenToken(String targetScope) {
		MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "maskinporten");
		formData.add("target", targetScope);

		formData.add("authorization_details", authorizationDetails(altinn3Properties.systemId()));

		return requireNonNull(restClient.post()
				.accept(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.body(NaisTexasToken.class)).accessToken();
	}
}
