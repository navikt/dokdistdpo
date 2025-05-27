package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to;

import lombok.Builder;

@Builder
public record ServiceCode(String serviceCode, int serviceEditionCode) {
}
