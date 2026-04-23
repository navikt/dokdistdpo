package no.nav.dokdistdpo.consumer.token.naistexas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.config.properties.MaskinportenProperties;
import no.nav.dokdistdpo.config.properties.NaisTexasProperties;
import no.nav.dokdistdpo.exception.technical.JsonSerializeException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static java.util.Objects.requireNonNull;
import static no.nav.dokdistdpo.consumer.token.naistexas.AuthorizationDetail.authorizationDetails;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Component
public class NaisTexasTokenConsumer {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final MaskinportenProperties maskinportenProperties;
	private final DokdistdpoProperties.Altinn3Properties altinn3Properties;

	public NaisTexasTokenConsumer(RestClient.Builder restClientBuilder,
								  ObjectMapper objectMapper,
								  NaisTexasProperties naistexasProperties,
								  MaskinportenProperties maskinportenProperties,
								  DokdistdpoProperties dokdistdpoProperties) {
		this.objectMapper = objectMapper;
		this.restClient = restClientBuilder
				.baseUrl(naistexasProperties.tokenEndpoint())
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
		this.altinn3Properties = dokdistdpoProperties.altinn3();
		this.maskinportenProperties = maskinportenProperties;
	}

	public String getMaskinportenTokenWithAuthDetails(String targetScopes) {
		MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "maskinporten");
		formData.add("target", isNotBlank(targetScopes) ? targetScopes : maskinportenProperties.scopes());

		formData.add("authorization_details", serializeAuthorizationDetails());

		return requireNonNull(restClient.post()
				.accept(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.body(NaisTexasToken.class)).accessToken();
	}

	private String serializeAuthorizationDetails() {
		try {
			return objectMapper.writeValueAsString(authorizationDetails(altinn3Properties.externalRef()));
		} catch (JsonProcessingException e) {
			throw new JsonSerializeException("Failed to serialize authorization details", e);
		}
	}
}
