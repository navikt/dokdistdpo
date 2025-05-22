package no.nav.dokdistdpo.consumer.serviceregistry;

import lombok.Builder;

@Builder
public record ServiceRegistryRequest(String mottakerId, String processIdentifier) {
}
