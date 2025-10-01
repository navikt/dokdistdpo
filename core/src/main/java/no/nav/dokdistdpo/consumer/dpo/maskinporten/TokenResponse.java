package no.nav.dokdistdpo.consumer.dpo.maskinporten;

public record TokenResponse(
		String accessToken,
		Integer expiresIn,
		String scope) {
}
