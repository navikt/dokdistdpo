package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record HentForsendelseResponse(
		String bestillingsId,
		String konversasjonId,
		String forsendelseStatus,
		String modus,
		String tema,
		String forsendelseTittel,
		String forsendelseMetadata,
		String forsendelseMetadataType,
		Mottaker mottaker,
		ArkivInformasjon arkivInformasjon,
		List<Dokument> dokumenter) {

	@Builder
	public record ArkivInformasjon(String arkivId) {
	}
}
