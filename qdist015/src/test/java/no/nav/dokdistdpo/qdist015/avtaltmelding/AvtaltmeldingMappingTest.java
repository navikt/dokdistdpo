package no.nav.dokdistdpo.qdist015.avtaltmelding;

import jakarta.xml.bind.JAXBElement;
import no.arkivverket.standarder.noark5.arkivmelding.avtalt.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.avtalt.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.avtalt.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivmelding.avtalt.Journalpost;
import no.arkivverket.standarder.noark5.arkivmelding.avtalt.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivmelding.avtalt.Mappe;
import no.arkivverket.standarder.noark5.arkivmelding.avtalt.Part;
import no.arkivverket.standarder.noark5.arkivmelding.avtalt.Registrering;
import no.arkivverket.standarder.noark5.arkivmelding.avtalt.Saksmappe;
import no.nav.avtaltmelding.trygderetten.v1.NavMappe;
import no.nav.dokdistdpo.consumer.ereg.EregConsumer;
import no.nav.dokdistdpo.consumer.pdl.PdlConsumer;
import no.nav.dokdistdpo.consumer.saf.SafJournalpost;
import no.nav.dokdistdpo.consumer.saf.journalpost.CompactSafJournalpost;
import no.nav.dokdistdpo.consumer.saf.journalpost.SafJournalpostService;
import no.nav.dokdistdpo.qdist015.saf.CompactSafJournalpostQueryService;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.VARIANTFORMAT_ARKIV;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.VARIANTFORMAT_PRODUKSJON;
import static no.nav.dokdistdpo.consumer.saf.SafJournalpost.Dokumentvariant.FILTYPE_XLSX;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.APP_NAME;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.ARKIVFORMAT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.AVSENDER;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.DOKUMENTASJON;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.DOKUMENTET_ER_FERDIGSTILT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.EKSPEDERT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.HOVEDDOKUMENT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.INNGAAENDE;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.MOTTAKER;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.NAV;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.PRODUKSJONSFORMAT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.REFERANSE_DOKUMENTFIL_FORMAT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.SAKSPART_ROLLE_AMP;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.SAKSPART_ROLLE_DAP;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.UKJENT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.UNDER_BEHANDLING;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.UTGAAENDE;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.UTGAAENDE_DOKUMENT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.VEDLEGG;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformat.PDF;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.ARKIV_SAKNUMMER;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.AVSENDER_MOTTAKER_NAVN_ORIG_JP;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.BESTILLINGS_ID;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.BRUKER_ID_AKTOER_ID;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.BRUKER_ID_FNR;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.BRUKER_ID_ORGNR;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.BRUKER_TYPE_AKTOER_ID;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.BRUKER_TYPE_ORGNR;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.DATO_JOURNALFOERT;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.DATO_JOURNALFOERT_ORIG_JP;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.DATO_OPPRETTET_JOURNALPOST;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.DATO_OPPRETTET_SAK;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.DOKUMENT_INFO_ID_HOVEDDOK;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.DOKUMENT_INFO_ID_VEDLEGG;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.DOKUMENT_INFO_ID_VEDLEGG_2;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.EREG_NAVN;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.FILTYPE_JPEG;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.FILTYPE_PDF;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.FILTYPE_PNG;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.FIXED_LOCAL_DATE_TIME;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.JOURNALFOERT_AV_NAVN;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.JOURNALFOERT_AV_NAVN_ORIG_JP;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.JOURNALPOST_ID;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.OPPRETTET_AV_NAVN;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.ORIGINAL_JPID_VEDLEGG;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.PDL_NAVN;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.TEMA;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.TEMA_NAVN;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.TITTEL;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.TITTEL_HOVEDDOK;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.TITTEL_VEDLEGG;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.creatHentPersonInfo;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.createCompactSafJournalpost;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.createCompactSafJournalpostBuilder;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.createCompactSafJournalpostNoJournalFoertAv;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.createHoveddokumentBuilder;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.createJournalpostBuilderNoJournalFoertAv;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.createSafJournalpostBuilder;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.createVedleggBuilder;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltmeldingTestData.createVedleggBuilderUtenDato;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.TestUtils.convertFromXmlGregorianCalendarToLocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvtaltmeldingMappingTest {

	private EregConsumer eregMock;
	private SafJournalpostService<CompactSafJournalpost> safJournalpostService;
	private AvtaltmeldingMapping avtaltmeldingMapping;
	private PdlConsumer pdlConsumer;

	@BeforeEach
	public void setUp() {
		eregMock = mock(EregConsumer.class);
		pdlConsumer = mock(PdlConsumer.class);
		safJournalpostService = mock(CompactSafJournalpostQueryService.class);
		avtaltmeldingMapping = new AvtaltmeldingMapping(safJournalpostService, eregMock, pdlConsumer);
	}

	@Test
	@DisplayName("Asserts all fields")
	void fullHappyPath() {
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(createSafJournalpostBuilder()
				.tema(TEMA)
				.build(), BESTILLINGS_ID);

		MatcherAssert.assertThat(arkivmeldingJAXBElement, notNullValue());
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		assertArkivmelding(arkivmelding);
	}

	@Test
	@DisplayName("Case when bruker is organisasjon. Should get name from Ereg")
	void happyPathBrukerIsOrganisasjon() {
		when(eregMock.hentOrganisasjonsnavn(any(String.class))).thenReturn(EREG_NAVN);

		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.bruker(SafJournalpost.Bruker.builder()
						.id(BRUKER_ID_ORGNR)
						.type(BRUKER_TYPE_ORGNR)
						.build())
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Part sakspartDAP = saksmappe.getPart().get(1);

		assertEquals(EREG_NAVN, sakspartDAP.getPartNavn());
		assertEquals(SAKSPART_ROLLE_DAP, sakspartDAP.getPartRolle());
		assertEquals(BRUKER_ID_ORGNR, sakspartDAP.getOrganisasjonsnummer().getOrganisasjonsnummer());
		assertNull(sakspartDAP.getFoedselsnummer());
		assertNull(sakspartDAP.getKontaktperson());
	}

	@Test
	@DisplayName("Case when bruker is aktoer. Should get fnr from aktoerregister")
	void happyPathBrukerIsAktoer() {
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());

		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.bruker(SafJournalpost.Bruker.builder()
						.id(BRUKER_ID_AKTOER_ID)
						.type(BRUKER_TYPE_AKTOER_ID)
						.build())
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Part sakspartDAP = saksmappe.getPart().get(1);

		assertEquals(PDL_NAVN, sakspartDAP.getPartNavn());
		assertEquals(SAKSPART_ROLLE_DAP, sakspartDAP.getPartRolle());
		assertNull(sakspartDAP.getKontaktperson());
	}

	@Test
	@DisplayName("Case for satt originalJournalPostId men ukjent datoJournal")
	void shouldMapOpprettetDatoWhenNullDatoJournalISafJournalpostgetJournalfortAndVedleggHasOriginalJpId() {
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.journalposttype(INNGAAENDE)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilderUtenDato().build()))
				.build();
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelse = registreringJp.getDokumentbeskrivelse()
				.get(1);

		assertEquals(TITTEL_VEDLEGG, dokumentbeskrivelse.getTittel());

	}

	@Test
	@DisplayName("Case when journalposttype is inngaaende and vedlegg has original jpId. Should make a custom dokumenttittel")
	void happyPathTestTittelWhenJpIsInngaaendeAndVedleggHasOriginalJpId() {
		when(safJournalpostService.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createCompactSafJournalpost());
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.journalposttype(INNGAAENDE)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelse = registreringJp.getDokumentbeskrivelse()
				.get(1);

		assertEquals(TITTEL_VEDLEGG + ", Til " + AVSENDER_MOTTAKER_NAVN_ORIG_JP, dokumentbeskrivelse.getTittel());

	}

	@Test
	@DisplayName("Case when journalposttype is utgaaende and vedlegg has original jpId. Should make a custom dokumenttittel")
	void happyPathTestTittelWhenJpIsUtgaaendeAndVedleggHasOriginalJpId() {
		when(safJournalpostService.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createCompactSafJournalpost());
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.journalposttype(UTGAAENDE)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelse = registreringJp.getDokumentbeskrivelse()
				.get(1);

		assertThat(dokumentbeskrivelse.getTittel()).isEqualTo(TITTEL_VEDLEGG + ", Til " + AVSENDER_MOTTAKER_NAVN_ORIG_JP);

	}

	@Test
	@DisplayName("Case when journalposttype is notat and vedlegg has original jpId. Should not make a custom dokumenttittel")
	void happyPathTestTittelWhenJpIsNotatAndVedleggHasOriginalJpId() {
		when(safJournalpostService.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createCompactSafJournalpost());
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.journalposttype("Notat")
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelse = registreringJp.getDokumentbeskrivelse()
				.get(1);

		assertThat(dokumentbeskrivelse.getTittel()).isEqualTo(TITTEL_VEDLEGG + ", Til " + AVSENDER_MOTTAKER_NAVN_ORIG_JP);

	}

	@Test
	@DisplayName("Case when vedlegg has original jpId. Should get opprettet dato from original journalpost")
	void happyPathTestOpprettetDatoWhenVedleggHasOriginalJpId() {
		when(safJournalpostService.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createCompactSafJournalpost());
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.get(0);
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = registreringJp.getDokumentbeskrivelse()
				.get(1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentbeskrivelseVedlegg.getDokumentobjekt().getFirst();

		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseHoveddok.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektHoveddok.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseVedlegg.getOpprettetDato()).toString(), DATO_JOURNALFOERT_ORIG_JP.toString());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektVedlegg.getOpprettetDato()).toString(), DATO_JOURNALFOERT_ORIG_JP.toString());

	}

	@Test
	@DisplayName("Case when vedlegg has original jpId. Should get opprettet av from original journalpost")
	void happyPathTestOpprettetAvWhenVedleggHasOriginalJpId() {
		when(safJournalpostService.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createCompactSafJournalpost());
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.get(0);
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = registreringJp.getDokumentbeskrivelse()
				.get(1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentbeskrivelseVedlegg.getDokumentobjekt().getFirst();

		assertEquals(JOURNALFOERT_AV_NAVN, dokumentbeskrivelseHoveddok.getOpprettetAv());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentobjektHoveddok.getOpprettetAv());
		assertEquals(JOURNALFOERT_AV_NAVN_ORIG_JP, dokumentbeskrivelseVedlegg.getOpprettetAv());
		assertEquals(JOURNALFOERT_AV_NAVN_ORIG_JP, dokumentobjektVedlegg.getOpprettetAv());

	}

	@Test
	@DisplayName("Case when dokument does not have variantformat SLADDET (variantformat is ARKIV) and filtype is not PNG or JPEG. Should set variantformat to Produksjonsformat")
	void happyPathTestNoSladdetVariantAndFiltypeNotPNGOrJPEG() {
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.dokumenter(singletonList(createHoveddokumentBuilder()
						.dokumentvarianter(Arrays.asList(SafJournalpost.Dokumentvariant.builder()
										.variantformat(VARIANTFORMAT_ARKIV)
										.filtype(FILTYPE_XLSX)
										.build(),
								SafJournalpost.Dokumentvariant.builder()
										.variantformat(VARIANTFORMAT_PRODUKSJON)
										.filtype(FILTYPE_PDF)
										.build()))
						.build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.getFirst();
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();

		assertEquals(PRODUKSJONSFORMAT, dokumentobjektHoveddok.getVariantformat());
		assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(PRODUKSJONSFORMAT));
	}

	@Test
	@DisplayName("Case when dokument does not have variantformat SLADDET (variantformat is ARKIV) and filtype is PNG. Should set variantformat to Arkivformat")
	void happyPathTestNoSladdetVariantAndFiltypeIsPng() {
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.dokumenter(singletonList(createHoveddokumentBuilder()
						.dokumentvarianter(Arrays.asList(SafJournalpost.Dokumentvariant.builder()
										.variantformat(VARIANTFORMAT_ARKIV)
										.filtype(FILTYPE_PNG)
										.build(),
								SafJournalpost.Dokumentvariant.builder()
										.variantformat(VARIANTFORMAT_PRODUKSJON)
										.filtype(FILTYPE_PDF)
										.build()))
						.build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.getFirst();
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();

		assertEquals(ARKIVFORMAT, dokumentobjektHoveddok.getVariantformat());
		assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(ARKIVFORMAT));
	}

	@Test
	@DisplayName("Case when dokument does not have variantformat SLADDET (variantformat is ARKIV) and filtype is JPEG. Should set variantformat to Arkivformat")
	void happyPathTestNoSladdetVariantAndFiltypeIsJPEG() {
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.dokumenter(singletonList(createHoveddokumentBuilder()
						.dokumentvarianter(Arrays.asList(SafJournalpost.Dokumentvariant.builder()
										.variantformat(VARIANTFORMAT_ARKIV)
										.filtype(FILTYPE_JPEG)
										.build(),
								SafJournalpost.Dokumentvariant.builder()
										.variantformat(VARIANTFORMAT_PRODUKSJON)
										.filtype(FILTYPE_PDF)
										.build()))
						.build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.getFirst();
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();

		assertEquals(ARKIVFORMAT, dokumentobjektHoveddok.getVariantformat());
		assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(ARKIVFORMAT));
	}

	@Test
	@DisplayName("Case when vedlegg has no dokumentstatus set. That vedlegg should be considered FERDIGSTILT.")
	void happyPathVedleggFerdigstiltUtenStatus() {
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().dokumentstatus(null).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		assertEquals(2, arkivmelding.getAntallFiler());
		assertDokumentbeskrivelseOpprettetAv(registreringJp.getDokumentbeskrivelse());
	}

	@Test
	@DisplayName("Case when vedlegg does not have dokumentstatus FERDIGSTILT. That vedlegg should not be mapped.")
	void happyPathIkkeFerdigstiltVedlegg() {
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createSafJournalpostBuilder()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG_2)
								.dokumentstatus("UNDER_REDIGERING")
								.build(),
						createVedleggBuilder().build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		assertEquals(2, arkivmelding.getAntallFiler());
		assertDokumentbeskrivelseOpprettetAv(registreringJp.getDokumentbeskrivelse());
	}

	@Test
	@DisplayName("Case for satt originalJournalPostId men ukjent journalfører")
	void assertUkjentVedsafJournalpostgetJournalfortAvNavnErNull() {
		when(safJournalpostService.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createCompactSafJournalpostNoJournalFoertAv());
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		SafJournalpost safJournalpost = createJournalpostBuilderNoJournalFoertAv()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(safJournalpost, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentBeskrivelseVedlegg = registreringJp.getDokumentbeskrivelse()
				.get(1);
		Dokumentobjekt dokumentVedlegg = dokumentBeskrivelseVedlegg.getDokumentobjekt().getFirst();

		assertEquals(UKJENT, dokumentVedlegg.getOpprettetAv());
	}

	@Test
	@DisplayName("Når sak mangler opprettetDato, sett opprettetDato fra eldste vedlegg sortert etter journalpostens dokumentbeskrivelse.opprettetDato")
	void shouldSetteOpprettetDatoPaaSakFraJournalpostTilhorendeTilEldsteVedlegg() {
		LocalDateTime femDagerSiden = FIXED_LOCAL_DATE_TIME.minusDays(5);
		LocalDateTime treDagerSiden = FIXED_LOCAL_DATE_TIME.minusDays(3);
		when(pdlConsumer.hentPersonInfo(anyString())).thenReturn(creatHentPersonInfo());
		when(safJournalpostService
				.hentJournalpost(DOKUMENT_INFO_ID_VEDLEGG))
				.thenReturn(createCompactSafJournalpostBuilder()
						.datoJournalfoert(treDagerSiden)
						.build()
				);
		when(safJournalpostService
				.hentJournalpost(DOKUMENT_INFO_ID_VEDLEGG_2))
				.thenReturn(createCompactSafJournalpostBuilder()
						.datoJournalfoert(femDagerSiden)
						.build()
				);

		SafJournalpost.Sak sakUtenOpprettetDato = SafJournalpost.Sak.builder()
				.arkivsaksnummer(ARKIV_SAKNUMMER)
				.build();
		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapping.createAvtaltmelding(createSafJournalpostBuilder()
				.sak(sakUtenOpprettetDato)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(DOKUMENT_INFO_ID_VEDLEGG).build(),
						createVedleggBuilder().originalJournalpostId(DOKUMENT_INFO_ID_VEDLEGG_2).build()))
				.build(), BESTILLINGS_ID);

		MatcherAssert.assertThat(arkivmeldingJAXBElement, notNullValue());
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();

		assertEquals(femDagerSiden.toString(),
				convertFromXmlGregorianCalendarToLocalDateTime(arkivmelding.getMappe().getFirst().getOpprettetDato()).toString());
	}

	private void assertArkivmelding(Arkivmelding arkivmelding) {
		assertNotNull(arkivmelding);
		assertEquals(APP_NAME, arkivmelding.getSystem());
		assertEquals(BESTILLINGS_ID, arkivmelding.getMeldingId());
		assertNotNull(arkivmelding.getTidspunkt());
		assertEquals(2, arkivmelding.getAntallFiler());
		assertMappe(arkivmelding.getMappe());
	}

	private void assertMappe(List<Mappe> mappeList) {
		assertTrue(mappeList != null && mappeList.size() == 1);
		assertInstanceOf(Saksmappe.class, mappeList.getFirst());
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();

		assertEquals(TEMA_NAVN, saksmappe.getTittel());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(saksmappe.getOpprettetDato()).toString(), DATO_OPPRETTET_SAK.toString());
		assertEquals(OPPRETTET_AV_NAVN, saksmappe.getOpprettetAv());
		NavMappe navMappe = extractNavMappe(saksmappe.getVirksomhetsspesifikkeMetadata());
		assertEquals(ARKIV_SAKNUMMER, navMappe.getSaksnummer());
		assertRegistrering(saksmappe.getRegistrering());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(saksmappe.getSaksdato()).toString(), DATO_OPPRETTET_SAK.toString());
		assertEquals(NAV, saksmappe.getAdministrativEnhet());
		assertEquals(OPPRETTET_AV_NAVN, saksmappe.getSaksansvarlig());
		assertEquals(UNDER_BEHANDLING, saksmappe.getSaksstatus());
		assertSakspart(saksmappe.getPart());
	}

	private NavMappe extractNavMappe(Object virksomhetsspesifikkeMetadata) {
		JAXBElement<NavMappe> navMappeElement = (JAXBElement) ((JAXBElement) virksomhetsspesifikkeMetadata).getValue();
		return navMappeElement.getValue();
	}

	private void assertRegistrering(List<Registrering> registreringList) {
		assertTrue(registreringList != null && registreringList.size() == 1);
		assertInstanceOf(Journalpost.class, registreringList.getFirst());

		Journalpost registreringJp = (Journalpost) registreringList.getFirst();
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(registreringJp.getOpprettetDato()).toString(), DATO_OPPRETTET_JOURNALPOST.toString());
		assertEquals(OPPRETTET_AV_NAVN, registreringJp.getOpprettetAv());
		assertDokumentbeskrivelseOpprettetAv(registreringJp.getDokumentbeskrivelse());
		assertEquals(TITTEL, registreringJp.getTittel());
		assertEquals(UTGAAENDE_DOKUMENT, registreringJp.getJournalposttype());
		assertEquals(EKSPEDERT, registreringJp.getJournalstatus());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(registreringJp.getJournaldato()).toString(), DATO_JOURNALFOERT.toString());
		assertKorrespondanseparter(registreringJp.getKorrespondansepart());
	}

	private void assertKorrespondanseparter(List<Korrespondansepart> korrespondansepartList) {
		assertTrue(korrespondansepartList != null && korrespondansepartList.size() == 2);

		Korrespondansepart mottaker = korrespondansepartList.getFirst();
		assertEquals(MOTTAKER, mottaker.getKorrespondanseparttype());
		assertEquals(PDL_NAVN, mottaker.getKorrespondansepartNavn());

		Korrespondansepart avsender = korrespondansepartList.get(1);
		assertEquals(AVSENDER, avsender.getKorrespondanseparttype());
		assertEquals(NAV, avsender.getKorrespondansepartNavn());
	}

	private void assertSakspart(List<Part> sakspartList) {
		assertTrue(sakspartList != null && sakspartList.size() == 2);

		Part sakspartAMP = sakspartList.getFirst();
		assertNull(sakspartAMP.getPartID());
		assertThat(sakspartAMP.getPartNavn()).isEqualTo(NAV);
		assertThat(sakspartAMP.getPartRolle()).isEqualTo(SAKSPART_ROLLE_AMP);
		assertEquals(OPPRETTET_AV_NAVN, sakspartAMP.getKontaktperson());
		assertEquals(NAV_ORGNUMMER, sakspartAMP.getOrganisasjonsnummer().getOrganisasjonsnummer());
		assertNull(sakspartAMP.getFoedselsnummer());

		Part sakspartDAP = sakspartList.get(1);
		assertEquals(PDL_NAVN, sakspartDAP.getPartNavn());
		assertEquals(SAKSPART_ROLLE_DAP, sakspartDAP.getPartRolle());
		assertNull(sakspartDAP.getKontaktperson());
		assertEquals(BRUKER_ID_FNR, sakspartDAP.getFoedselsnummer().getFoedselsnummer());
		assertNull(sakspartDAP.getOrganisasjonsnummer());
	}

	private void assertDokumentbeskrivelseOpprettetAv(List<Dokumentbeskrivelse> dokumentbeskrivelseList) {
		assertTrue(dokumentbeskrivelseList != null && dokumentbeskrivelseList.size() == 2);

		assertNotNull(dokumentbeskrivelseList.getFirst());
		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = dokumentbeskrivelseList.getFirst();
		assertEquals(HOVEDDOKUMENT, dokumentbeskrivelseHoveddok.getTilknyttetRegistreringSom());
		assertEquals(BigInteger.ONE, dokumentbeskrivelseHoveddok.getDokumentnummer());
		assertCommonAttributesVedleggDokumentbeskrivelseOpprettetAv(dokumentbeskrivelseHoveddok);
		assertEquals(TITTEL_HOVEDDOK, dokumentbeskrivelseHoveddok.getTittel());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseHoveddok.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());

		assertDokumentobjektHoveddokument(dokumentbeskrivelseHoveddok.getDokumentobjekt());

		assertNotNull(dokumentbeskrivelseList.get(1));
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = dokumentbeskrivelseList.get(1);
		assertEquals(VEDLEGG, dokumentbeskrivelseVedlegg.getTilknyttetRegistreringSom());
		assertEquals(dokumentbeskrivelseVedlegg.getDokumentnummer(), BigInteger.valueOf(2));
		assertCommonAttributesVedleggDokumentbeskrivelseOpprettetAv(dokumentbeskrivelseVedlegg);
		assertEquals(TITTEL_VEDLEGG, dokumentbeskrivelseVedlegg.getTittel());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseVedlegg.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());

		assertDokumentobjektVedlegg(dokumentbeskrivelseVedlegg.getDokumentobjekt());
	}

	private void assertDokumentobjektHoveddokument(List<Dokumentobjekt> dokumentobjektList) {
		assertTrue(dokumentobjektList != null && dokumentobjektList.size() == 1);
		Dokumentobjekt dokumentobjektHoveddok = dokumentobjektList.getFirst();
		assertEquals(BigInteger.ONE, dokumentobjektHoveddok.getVersjonsnummer());
		assertEquals(DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET, dokumentobjektHoveddok.getVariantformat());
		assertEquals(PDF.getFormat(), dokumentobjektHoveddok.getFormat());
		assertEquals(DATO_JOURNALFOERT.toString(), convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektHoveddok.getOpprettetDato()).toString());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentobjektHoveddok.getOpprettetAv());
		assertEquals(format(REFERANSE_DOKUMENTFIL_FORMAT, JOURNALPOST_ID, DOKUMENT_INFO_ID_HOVEDDOK, DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET, PDF.getFilendelse()), dokumentobjektHoveddok.getReferanseDokumentfil());
	}

	private void assertDokumentobjektVedlegg(List<Dokumentobjekt> dokumentobjektList) {
		assertTrue(dokumentobjektList != null && dokumentobjektList.size() == 1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentobjektList.getFirst();
		assertEquals(BigInteger.ONE, dokumentobjektVedlegg.getVersjonsnummer());
		assertEquals(ARKIVFORMAT, dokumentobjektVedlegg.getVariantformat());
		assertEquals(AvtaltFilformat.JPEG.getFormat(), dokumentobjektVedlegg.getFormat());
		assertEquals(DATO_JOURNALFOERT.toString(), convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektVedlegg.getOpprettetDato()).toString());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentobjektVedlegg.getOpprettetAv());
		assertEquals(format(REFERANSE_DOKUMENTFIL_FORMAT, JOURNALPOST_ID, DOKUMENT_INFO_ID_VEDLEGG, ARKIVFORMAT, AvtaltFilformat.JPEG.getFilendelse()), dokumentobjektVedlegg.getReferanseDokumentfil());
	}

	private void assertCommonAttributesVedleggDokumentbeskrivelseOpprettetAv(Dokumentbeskrivelse dokumentbeskrivelse) {
		assertEquals(DOKUMENTASJON, dokumentbeskrivelse.getDokumenttype());
		assertEquals(DOKUMENTET_ER_FERDIGSTILT, dokumentbeskrivelse.getDokumentstatus());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentbeskrivelse.getOpprettetAv());
		assertNotNull(dokumentbeskrivelse.getTilknyttetDato());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentbeskrivelse.getTilknyttetAv());
	}

}