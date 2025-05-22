package no.nav.dokdistdpo.consumer.dpo.altinn.to;

public record AltinnReason(
		Integer id,
		String message,
		String userId,
		String localized) {
}
