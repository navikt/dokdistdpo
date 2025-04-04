package no.nav.dokdistdpo.consumer.dokdistadmin;

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
		Postadresse postadresse,
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
	public record Postadresse(
			String adresselinje1,
			String adresselinje2,
			String adresselinje3,
			String postnummer,
			String poststed,
			String landkode) {
	}

	@Builder
	public record Dokument(
			String tilknyttetSom,
			String dokumentObjektReferanse,
			String arkivDokumentInfoId,
			String dokumenttypeId) {
	}
}
