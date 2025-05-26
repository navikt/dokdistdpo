package no.nav.dokdistdpo.consumer.dpo;

import lombok.Builder;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType;

import java.io.InputStream;

import static java.lang.Enum.valueOf;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_XML;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_XML;

@Builder
public record NavDokument(String filnavn,
						  String mimeType,
						  InputStream innhold) {

	public static final String MIMETYPE_XML = "application/xml";
	public static final String MIMETYPE_PDF = "application/pdf";

	public static NavDokument fromDpoMelding(String forsendelseMetadataType, final InputStream contents) {
		return NavDokument.builder()
				.filnavn(getFilnavn(forsendelseMetadataType))
				.mimeType(MIMETYPE_XML)
				.innhold(contents)
				.build();
	}

	public static NavDokument fromVedlegg(final String filnavn, final InputStream contents) {
		return NavDokument.builder()
				.filnavn(filnavn)
				.mimeType(MIMETYPE_PDF)
				.innhold(contents)
				.build();
	}

	private static String getFilnavn(String metadataType) {
		ForsendelseMetadataType forsendelseMetadataType = valueOf(ForsendelseMetadataType.class, metadataType);
		return switch (forsendelseMetadataType) {
			case DPO_ARKIVMELDING -> ARKIVMELDING_XML;
			case DPO_AVTALEMELDING -> AVTALTMELDING_XML;
		};
	}
}
