package no.nav.dokdistdpo.qdist015.avtaltmelding;

import no.nav.dokdistdpo.consumer.saf.SafJournalpost;
import no.nav.dokdistdpo.exception.functional.AvtaltmeldingFilformatMappingException;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.VARIANTFORMAT_ARKIV;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.VARIANTFORMAT_SLADDET;
import static no.nav.dokdistdpo.consumer.saf.SafJournalpost.Dokumentvariant.FILTYPE_JPEG;
import static no.nav.dokdistdpo.consumer.saf.SafJournalpost.Dokumentvariant.FILTYPE_PDF;
import static no.nav.dokdistdpo.consumer.saf.SafJournalpost.Dokumentvariant.FILTYPE_PNG;
import static no.nav.dokdistdpo.consumer.saf.SafJournalpost.Dokumentvariant.FILTYPE_TIFF;
import static no.nav.dokdistdpo.consumer.saf.SafJournalpost.Dokumentvariant.FILTYPE_XLSX;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformat.JPEG;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformat.PDF;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformat.PNG;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformat.TIFF;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformat.XLSX;

public final class AvtaltFilformatMapper {

	private AvtaltFilformatMapper() {
	}

	public static AvtaltFilformat map(SafJournalpost.DokumentInfo dokumentInfo) {
		String safFiltype = getFiltype(dokumentInfo);

		return switch (safFiltype) {
			case FILTYPE_PDF -> PDF;
			case FILTYPE_JPEG -> JPEG;
			case FILTYPE_PNG -> PNG;
			case FILTYPE_TIFF -> TIFF;
			case FILTYPE_XLSX -> XLSX;
			default ->
					throw new AvtaltmeldingFilformatMappingException("Klarte ikke mappe format, filtype er ikke støttet. journalpost.dokumenter.dokumentvarianter.filtype=" + safFiltype);
		};
	}

	private static String getFiltype(SafJournalpost.DokumentInfo dokumentInfo) {
		if (isDokumentContainsSladdetVariant(dokumentInfo)) {
			return getVariantformatSladdet(dokumentInfo);
		}
		return getVariantformatArkiv(dokumentInfo);
	}

	public static String getVariantformatArkiv(SafJournalpost.DokumentInfo dokumentInfo) {
		return dokumentInfo.dokumentvarianter().stream()
				.filter(dv -> VARIANTFORMAT_ARKIV.equals(dv.variantformat()))
				.findAny()
				.get().filtype();

	}

	public static String getVariantformatSladdet(SafJournalpost.DokumentInfo dokumentInfo) {
		return dokumentInfo.dokumentvarianter().stream()
				.filter(dv -> VARIANTFORMAT_SLADDET.equals(dv.variantformat()))
				.findAny()
				.get().filtype();

	}

	public static boolean isDokumentContainsSladdetVariant(SafJournalpost.DokumentInfo dokumentInfo) {
		return dokumentInfo.dokumentvarianter()
				.stream()
				.anyMatch(dv -> VARIANTFORMAT_SLADDET.equals(dv.variantformat()));
	}
}
