package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

@Builder
public record ArkivInformasjon(
		String arkivId,
		String arkivSystem) {
}
