package no.nav.dokdistdpo.consumer.gcloudstorage;

public record ForsendelseDokumentFraGStorage(
		byte[] pdf,
		String dokumentObjektReferanse,
		String dokumentInfoId
) {
}
