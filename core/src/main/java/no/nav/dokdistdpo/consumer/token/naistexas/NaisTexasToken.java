package no.nav.dokdistdpo.consumer.token.naistexas;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaisTexasToken(
		@JsonProperty("access_token")
		String accessToken) {
}
