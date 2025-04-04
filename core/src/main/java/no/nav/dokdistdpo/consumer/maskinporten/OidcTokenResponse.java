package no.nav.dokdistdpo.consumer.maskinporten;

public record OidcTokenResponse(
		String accessToken,
		Integer expiresIn,
		String scope
) {
}
