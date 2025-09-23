package no.nav.dokdistdpo.consumer.dpo.maskinporten;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record TokenResponse(
		String accessToken,
		Integer expiresIn,
		String scope) {
}
