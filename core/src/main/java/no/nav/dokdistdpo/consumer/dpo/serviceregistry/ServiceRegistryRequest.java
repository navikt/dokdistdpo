package no.nav.dokdistdpo.consumer.dpo.serviceregistry;

import lombok.Builder;

@Builder
public record ServiceRegistryRequest(
		String mottakerId,
		String processIdentifier) {
}
