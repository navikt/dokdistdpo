package no.nav.dokdistdpo.consumer.dpo.altinn.to;

import lombok.Builder;

@Builder
public record ServiceCode(String serviceCode, int serviceEditionCode) {
}
