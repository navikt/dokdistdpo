package no.nav.dokdistdpo.consumer.dokdistadmin;

import lombok.Builder;

import java.util.List;

@Builder
public record HentForsendelseResponse(
		String bestillingsId,
		String konversasjonId,
		String forsendelseStatus,
		String tema,
		String forsendelseTittel,
		String forsendelseMetadata,
		String forsendelseMetadataType,
		Mottaker mottaker,
		ArkivInformasjon arkivInformasjon,
		List<Dokument> dokumenter) {

	@Builder
	public record Mottaker(String mottakerId,
						   String mottakerNavn,
						   String mottakerType) {
	}

	@Builder
	public record ArkivInformasjon(String arkivId) {
	}

	@Builder
	public record Dokument(
			String tilknyttetSom,
			String dokumentObjektReferanse,
			String arkivDokumentInfoId,
			String dokumenttypeId) {
	}
}
