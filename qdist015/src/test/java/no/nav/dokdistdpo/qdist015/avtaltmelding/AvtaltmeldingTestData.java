package no.nav.dokdistdpo.qdist015.avtaltmelding;

import no.nav.dokdistdpo.consumer.pdl.HentPersonInfo;
import no.nav.dokdistdpo.consumer.saf.SafJournalpost;
import no.nav.dokdistdpo.consumer.saf.journalpost.CompactSafJournalpost;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.VARIANTFORMAT_ARKIV;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.VARIANTFORMAT_PRODUKSJON;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.VARIANTFORMAT_SLADDET;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.FERDIGSTILT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.UTGAAENDE;

public final class AvtaltmeldingTestData {

	static final String BESTILLINGS_ID = "bestillingsId";
	static final String JOURNALPOST_ID = "987654321";
	static final String ARKIV_SAKNUMMER = "111111";
	static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-03-15T12:15:30.01Z"), ZoneId.of("Europe/Oslo"));
	static final LocalDateTime FIXED_LOCAL_DATE_TIME = LocalDateTime.now(FIXED_CLOCK);
	static final LocalDateTime DATO_OPPRETTET_SAK = FIXED_LOCAL_DATE_TIME;
	static final LocalDateTime DATO_OPPRETTET_JOURNALPOST = FIXED_LOCAL_DATE_TIME.minusDays(1);
	static final LocalDateTime DATO_JOURNALFOERT = FIXED_LOCAL_DATE_TIME.minusDays(2);
	static final String OPPRETTET_AV_NAVN = "Sak Sakbehandlersen";
	static final String BRUKER_ID_FNR = "20026900000";
	static final String BRUKER_TYPE_FNR = "FNR";
	static final String BRUKER_ID_ORGNR = "974761084";
	static final String BRUKER_TYPE_ORGNR = "ORGNR";
	static final String BRUKER_ID_AKTOER_ID = "111113333333";
	static final String BRUKER_TYPE_AKTOER_ID = "AKTOERID";
	static final String TITTEL = "Klage på saksbehandling";
	static final String JOURNALFOERT_AV_NAVN = "Sak Sakbehandlersen";
	static final String TEMA_NAVN = "Dagpenger";
	static final String TEMA = "DAG";

	static final String DOKUMENT_INFO_ID_HOVEDDOK = "1234567";
	static final String TITTEL_HOVEDDOK = "Klage på saksbehandling";

	static final String DOKUMENT_INFO_ID_VEDLEGG = "7654321";
	static final String TITTEL_VEDLEGG = "Dokumentasjon til klage";
	static final String ORIGINAL_JPID_VEDLEGG = "1111111111";

	static final String DOKUMENT_INFO_ID_VEDLEGG_2 = "9876543";
	static final String EREG_NAVN = "Bedrift AS";
	static final String PDL_NAVN = "Bjarne Betjent";

	static final String AVSENDER_MOTTAKER_NAVN_ORIG_JP = "avsenderMottakerNavnOrigJp";
	static final String JOURNALFOERT_AV_NAVN_ORIG_JP = "journalfoertAvNavnOrigJp";
	static final LocalDateTime DATO_JOURNALFOERT_ORIG_JP = FIXED_LOCAL_DATE_TIME.minusDays(5);

	static final String FILTYPE_PNG = "PNG";
	static final String FILTYPE_JPEG = "JPEG";
	static final String FILTYPE_PDF = "PDF";

	static SafJournalpost.SafJournalpostBuilder createSafJournalpostBuilder() {
		return SafJournalpost.builder()
				.journalpostId(JOURNALPOST_ID)
				.sak(SafJournalpost.Sak.builder()
						.arkivsaksnummer(ARKIV_SAKNUMMER)
						.datoOpprettet(DATO_OPPRETTET_SAK)
						.build())
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.bruker(SafJournalpost.Bruker.builder()
						.id(BRUKER_ID_FNR)
						.type(BRUKER_TYPE_FNR)
						.build())
				.datoOpprettet(DATO_OPPRETTET_JOURNALPOST)
				.tittel(TITTEL)
				.journalfortAvNavn(JOURNALFOERT_AV_NAVN)
				.temanavn(TEMA_NAVN)
				.journalposttype(UTGAAENDE)
				.relevanteDatoer(singletonList(SafJournalpost.RelevantDato.builder()
						.datotype(SafJournalpost.Datotype.DATO_JOURNALFOERT.name())
						.dato(DATO_JOURNALFOERT)
						.build()))
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(), createVedleggBuilder().build()
				));
	}

	static SafJournalpost.SafJournalpostBuilder createJournalpostBuilderNoJournalFoertAv() {
		return SafJournalpost.builder()
				.journalpostId(JOURNALPOST_ID)
				.sak(SafJournalpost.Sak.builder()
						.arkivsaksnummer(ARKIV_SAKNUMMER)
						.datoOpprettet(DATO_OPPRETTET_SAK)
						.build())
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.bruker(SafJournalpost.Bruker.builder()
						.id(BRUKER_ID_FNR)
						.type(BRUKER_TYPE_FNR)
						.build())
				.datoOpprettet(DATO_OPPRETTET_JOURNALPOST)
				.tittel(TITTEL)
				.temanavn(TEMA_NAVN)
				.journalposttype(UTGAAENDE)
				.relevanteDatoer(singletonList(SafJournalpost.RelevantDato.builder()
						.datotype(SafJournalpost.Datotype.DATO_JOURNALFOERT.name())
						.dato(DATO_JOURNALFOERT)
						.build()))
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(), createVedleggBuilder().build()
				));
	}

	static SafJournalpost.DokumentInfo.DokumentInfoBuilder createHoveddokumentBuilder() {
		return SafJournalpost.DokumentInfo.builder()
				.dokumentInfoId(DOKUMENT_INFO_ID_HOVEDDOK)
				.dokumentstatus(FERDIGSTILT)
				.tittel(TITTEL_HOVEDDOK)
				.originalJournalpostId(null)
				.dokumentvarianter(Arrays.asList(SafJournalpost.Dokumentvariant.builder()
								.variantformat(VARIANTFORMAT_ARKIV)
								.filtype(FILTYPE_PNG)
								.build(),
						SafJournalpost.Dokumentvariant.builder()
								.variantformat(VARIANTFORMAT_SLADDET)
								.filtype(FILTYPE_PDF)
								.build()));
	}

	static SafJournalpost.DokumentInfo.DokumentInfoBuilder createVedleggBuilderUtenDato() {
		return SafJournalpost.DokumentInfo.builder()
				.dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG)
				.dokumentstatus(FERDIGSTILT)
				.tittel(TITTEL_VEDLEGG)
				.originalJournalpostId(null)
				.dokumentvarianter(Arrays.asList(SafJournalpost.Dokumentvariant.builder()
								.variantformat(VARIANTFORMAT_ARKIV)
								.filtype(FILTYPE_JPEG)
								.build(),
						SafJournalpost.Dokumentvariant.builder()
								.variantformat(VARIANTFORMAT_PRODUKSJON)
								.filtype(FILTYPE_PDF)
								.build()));
	}

	static SafJournalpost.DokumentInfo.DokumentInfoBuilder createVedleggBuilder() {
		return SafJournalpost.DokumentInfo.builder()
				.dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG)
				.dokumentstatus(FERDIGSTILT)
				.tittel(TITTEL_VEDLEGG)
				.originalJournalpostId(null)
				.dokumentvarianter(Arrays.asList(SafJournalpost.Dokumentvariant.builder()
								.variantformat(VARIANTFORMAT_ARKIV)
								.filtype(FILTYPE_JPEG)
								.build(),
						SafJournalpost.Dokumentvariant.builder()
								.variantformat(VARIANTFORMAT_PRODUKSJON)
								.filtype(FILTYPE_PDF)
								.build()));
	}

	static HentPersonInfo creatHentPersonInfo() {
		return new HentPersonInfo
				(BRUKER_ID_FNR, PDL_NAVN);
	}

	static CompactSafJournalpost createCompactSafJournalpost() {
		return CompactSafJournalpost.builder()
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
				.journalposttype(UTGAAENDE)
				.journalfortAvNavn(JOURNALFOERT_AV_NAVN_ORIG_JP)
				.datoJournalfoert(DATO_JOURNALFOERT_ORIG_JP)
				.build();
	}

	static CompactSafJournalpost createCompactSafJournalpostNoJournalFoertAv() {
		return CompactSafJournalpost.builder()
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
				.datoJournalfoert(DATO_JOURNALFOERT_ORIG_JP)
				.build();
	}

	static CompactSafJournalpost.CompactSafJournalpostBuilder createCompactSafJournalpostBuilder() {
		return CompactSafJournalpost.builder()
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
				.journalposttype(UTGAAENDE)
				.journalfortAvNavn(JOURNALFOERT_AV_NAVN_ORIG_JP)
				.datoJournalfoert(DATO_JOURNALFOERT_ORIG_JP);
	}
}
