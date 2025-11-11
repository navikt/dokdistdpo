package no.nav.dokdistdpo.consumer.dpo;

import lombok.Builder;

import java.io.InputStream;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_XML;

@Builder
public record NavDokument(String filnavn,
						  String mimeType,
						  InputStream innhold) {

	public static final String MIMETYPE_XML = "application/xml";
	public static final String MIMETYPE_PDF = "application/pdf";

	public static NavDokument fromDpoMelding(final InputStream contents) {
		return NavDokument.builder()
				.filnavn(ARKIVMELDING_XML)
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
}
