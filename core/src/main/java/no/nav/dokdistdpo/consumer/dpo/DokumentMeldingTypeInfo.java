package no.nav.dokdistdpo.consumer.dpo;

import lombok.Builder;

@Builder
public record DokumentMeldingTypeInfo(
		String typeVersion,
		String type,
		String documentProcessIdentification,
		String processIdentification, Object content) {
}
