package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to;

public record AltinnReason(
		Integer id,
		String message,
		String userId,
		String localized) {
}
