package no.nav.dokdistdpo.consumer.dpo.maskinporten;

import tools.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record TokenResponse(
		String accessToken,
		Integer expiresIn,
		String scope) {
}
