package no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to;

public record AltinnReason(
		Integer id,
		String message,
		String userId,
		String localized) {
}
