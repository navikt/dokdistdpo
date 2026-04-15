package no.nav.dokdistdpo.consumer.token.naistexas;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokdistdpo.config.properties.NaisTexasProperties;
import no.nav.dokdistdpo.exception.functional.DokumentpakkingException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static java.util.Objects.requireNonNull;
import static no.nav.dokdistdpo.consumer.token.naistexas.AutorizationDetail.authorizationDetails;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Component
public class NaisTexasTokenConsumer {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public NaisTexasTokenConsumer(RestClient.Builder restClientBuilder,
								  NaisTexasProperties naistexasProperties,
								  ObjectMapper objectMapper) {
		this.restClient = restClientBuilder
				.baseUrl(naistexasProperties.tokenEndpoint())
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
		this.objectMapper = objectMapper;
	}

	public String getMaskinportenToken(String targetScope) {
		MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "maskinporten");
		formData.add("target", targetScope);

		formData.add("authorization_details", serializeAuthorizationDetails());

		return requireNonNull(restClient.post()
				.accept(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.body(NaisTexasToken.class)).accessToken();
	}

	private String serializeAuthorizationDetails() {
		try {
			return objectMapper.writeValueAsString(authorizationDetails());
		} catch (Exception e) {
			throw new DokumentpakkingException("Failed to serialize authorization details", e);
		}
	}
}
