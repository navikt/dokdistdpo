package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record HentForsendelseResponse(
		Long forsendelseId,
		String bestillingsId,
		String konversasjonId,
		String forsendelseStatus,
		String modus,
		String tema,
		String forsendelseTittel,
		String forsendelseMetadata,
		String forsendelseMetadataType,
		String bestillendeFagsystem,
		Mottaker mottaker,
		ArkivInformasjon arkivInformasjon,
		Postadresse postadresse,
		List<Dokument> dokumenter) {
}
