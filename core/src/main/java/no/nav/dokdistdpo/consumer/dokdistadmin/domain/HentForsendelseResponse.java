package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record HentForsendelseResponse(
		Long forsendelseId,
		String bestillingsId,
		String konversasjonId,
		String forsendelseStatus,
		String tema,
		String bestillendeFagsystem,
		String dokumentProdApp,
		String forsendelseTittel,
		byte[] forsendelseMetadata,
		String forsendelseMetadataType,
		String distribusjonstype,
		String distribusjonstidspunkt,
		Mottaker mottaker,
		Postadresse postadresse,
		ArkivInformasjon arkivInformasjon,
		List<Dokument> dokumenter) {
}
