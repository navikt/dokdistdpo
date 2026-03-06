package no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to;

import lombok.Builder;

@Builder
public record ServiceCode(String serviceCode, int serviceEditionCode) {
}
