package no.nav.dokdistdpo.consumer.token.naistexas;

import no.nav.dokdistdpo.config.properties.NaisTexasProperties;
import no.nav.dokdistdpo.consumer.dpo.maskinporten.Consumer;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_ORG_AUTHORITY;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.asIso6523;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Component
public class NaisTexasTokenConsumer {

	private final RestClient restClient;

	public NaisTexasTokenConsumer(RestClient.Builder restClientBuilder,
								  NaisTexasProperties naistexasProperties) {
		this.restClient = restClientBuilder
				.baseUrl(naistexasProperties.tokenEndpoint())
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
	}

	public String getMaskinportenToken(String targetScope) {
		MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "maskinporten");
		formData.add("target", targetScope);
		Map<String, Object> authorizationDetail = new HashMap<>();
		authorizationDetail.put("type", "urn:altinn:systemuser");
		authorizationDetail.put("systemuser_org", Consumer.builder()
				.authority(ISO6523_ORG_AUTHORITY)
				.id(asIso6523(NAV_ORGNUMMER))
				.build());
		formData.add("authorization_details", List.of(authorizationDetail));

		return requireNonNull(restClient.post()
				.accept(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.body(NaisTexasToken.class)).accessToken();
	}
}
