package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ArkivInformasjon;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Dokument;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Mottaker;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OpprettForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Postadresse;
import no.nav.dokdistdpo.exception.functional.DokdistdpoIllegalArgumentException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static no.nav.dokdistdpo.consumer.dokdistadmin.domain.Mottaker.MottakerType.ORGANISASJON;
import static org.junit.jupiter.api.Assertions.*;

class OpprettForsendelseMapperTest {

	private static final String BESTILLINGS_ID = UUID.randomUUID().toString();
	private static final String OLD_BESTILLINGS_ID = UUID.randomUUID().toString();
	private static final String BESTILLENDE_FAGSYSTEM = "FS22";
	private static final String TEMA = "DAG";
	private static final String FORSENDELSE_TITTEL = "Vedtak om dagpenger";
	private static final String ARKIV_SYSTEM = "JOARK";
	private static final String ARKIV_ID = "234567891";
	private static final String MOTTAKER_ID_NAVN = "Bedrift AS";
	private static final String MOTTAKER_ID = "912345681";
	private static final String ADRESSELINJE_1 = "adresselinje1";
	private static final String ADRESSELINJE_2 = "adresselinje2";
	private static final String ADRESSELINJE_3 = "adresselinje3";
	private static final String POSTNUMMER = "postnummer";
	private static final String POSTSTED = "poststed";
	private static final String LAND = "NO";
	private static final String DOKUMENT_PROD_APP = "dokumentProdApp";
	private static final String DOKUMENTTYPE_ID_1 = "U000001";
	private static final String DOKUMENTTYPE_ID_2 = "U000001";
	private static final String OBJEKT_REFERANSE_1 = "objektReferanse1";
	private static final String OBJEKT_REFERANSE_2 = "objektReferanse2";
	private static final String TILKNYTTET_SOM_HOVEDDOK = "HOVEDDOKUMENT";
	private static final String TILKNYTTET_SOM_VEDLEGG = "VEDLEGG";
	private static final String ARKIV_DOKUMENTINFO_ID_1 = "arkivDokumentinfoId1";
	private static final String ARKIV_DOKUMENTINFO_ID_2 = "arkivDokumentinfoId2";

	private final OpprettForsendelseMapper mapper = new OpprettForsendelseMapper();

	@Test
	public void shouldMapForsendelser() {
		OpprettForsendelseRequest request = mapper.mapToOpprettForsendelse(createHentForsendelseResponse(), BESTILLINGS_ID);

		assertEquals(BESTILLINGS_ID, request.bestillingsId());
		assertEquals(FORSENDELSE_TITTEL, request.forsendelseTittel());
		assertEquals(DOKUMENT_PROD_APP, request.dokumentProdApp());
		assertEquals(BESTILLENDE_FAGSYSTEM, request.bestillendeFagsystem());
		assertEquals(ARKIV_ID, request.arkivInformasjon().arkivId());
		assertEquals(MOTTAKER_ID, request.mottaker().mottakerId());
		assertEquals(MOTTAKER_ID_NAVN, request.mottaker().mottakerNavn());
		assertEquals(OLD_BESTILLINGS_ID, request.originalDistribusjonId());
		assertPostadresseTo(request.postadresse());
		assertDokument(request.dokumenter().get(1));
	}

	@Test
	public void shouldMapForsendelserWhenAdresseErNull() {
		HentForsendelseResponse hentForsendelseResponse = createHentForsendelseResponseWithPostadresseNull();
		OpprettForsendelseRequest request = mapper.mapToOpprettForsendelse(hentForsendelseResponse, BESTILLINGS_ID);

		assertEquals(BESTILLINGS_ID, request.bestillingsId());
		assertEquals(FORSENDELSE_TITTEL, request.forsendelseTittel());
		assertEquals(DOKUMENT_PROD_APP, request.dokumentProdApp());
		assertEquals(BESTILLENDE_FAGSYSTEM, request.bestillendeFagsystem());
		assertEquals(ARKIV_ID, request.arkivInformasjon().arkivId());
		assertEquals(MOTTAKER_ID, request.mottaker().mottakerId());
		assertEquals(MOTTAKER_ID_NAVN, request.mottaker().mottakerNavn());
		assertEquals(OLD_BESTILLINGS_ID, request.originalDistribusjonId());
		assertNull(request.postadresse());
		assertDokument(request.dokumenter().get(1));
	}

	@Test
	public void shouldThrowExceptionIfHentForsendelseResponseIsNull() {
		DokdistdpoIllegalArgumentException exception = assertThrows(DokdistdpoIllegalArgumentException.class, () -> mapper.mapToOpprettForsendelse(null, BESTILLINGS_ID));
		assertEquals("hentForsendelseResponse kan ikke være null.", exception.getMessage());
	}

	@Test
	public void shouldThrowExceptionIfBestillingIdIsBlank() {
		DokdistdpoIllegalArgumentException exception = assertThrows(DokdistdpoIllegalArgumentException.class, () -> mapper.mapToOpprettForsendelse(createHentForsendelseResponse(), null));
		assertEquals("nyBestillingsId kan ikke være null eller tomt", exception.getMessage());
	}

	@Test
	public void shouldThrowExceptionIfMottakerIsNull() {
		DokdistdpoIllegalArgumentException exception = assertThrows(DokdistdpoIllegalArgumentException.class, () -> mapper.mapToOpprettForsendelse(createHentForsendelseResponseWithMottakerNull(), BESTILLINGS_ID));
		assertEquals("Mottaker kan ikke være null.", exception.getMessage());
	}

	private void assertPostadresseTo(Postadresse postadresse) {
		assertEquals(ADRESSELINJE_1, postadresse.adresselinje1());
		assertEquals(ADRESSELINJE_2, postadresse.adresselinje2());
		assertEquals(ADRESSELINJE_3, postadresse.adresselinje3());
		assertEquals(POSTNUMMER, postadresse.postnummer());
		assertEquals(POSTSTED, postadresse.poststed());
		assertEquals(LAND, postadresse.landkode());
	}

	private void assertDokument(Dokument dokument) {
		assertEquals(DOKUMENTTYPE_ID_2, dokument.dokumenttypeId());
		assertEquals(OBJEKT_REFERANSE_2, dokument.dokumentObjektReferanse());
		assertEquals(TILKNYTTET_SOM_VEDLEGG, dokument.tilknyttetSom());
		assertEquals(2, dokument.rekkefolge());
		assertEquals(ARKIV_DOKUMENTINFO_ID_2, dokument.arkivDokumentInfoId());
	}

	private HentForsendelseResponse createHentForsendelseResponse() {
		return HentForsendelseResponse.builder()
				.bestillingsId(OLD_BESTILLINGS_ID)
				.tema(TEMA)
				.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
				.forsendelseTittel(FORSENDELSE_TITTEL)
				.dokumentProdApp(DOKUMENT_PROD_APP)
				.arkivInformasjon(ArkivInformasjon.builder()
						.arkivSystem(ARKIV_SYSTEM)
						.arkivId(ARKIV_ID).build())
				.mottaker(createMottakerTo())
				.postadresse(createPostadresse())
				.dokumenter(createDokument())
				.build();
	}

	private HentForsendelseResponse createHentForsendelseResponseWithMottakerNull() {
		return HentForsendelseResponse.builder()
				.bestillingsId(OLD_BESTILLINGS_ID)
				.tema(TEMA)
				.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
				.forsendelseTittel(FORSENDELSE_TITTEL)
				.dokumentProdApp(DOKUMENT_PROD_APP)
				.arkivInformasjon(ArkivInformasjon.builder()
						.arkivId(ARKIV_ID).build())
				.mottaker(null)
				.postadresse(createPostadresse())
				.dokumenter(createDokument())
				.build();
	}

	private HentForsendelseResponse createHentForsendelseResponseWithPostadresseNull() {
		return HentForsendelseResponse.builder()
				.bestillingsId(OLD_BESTILLINGS_ID)
				.tema(TEMA)
				.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
				.forsendelseTittel(FORSENDELSE_TITTEL)
				.dokumentProdApp(DOKUMENT_PROD_APP)
				.arkivInformasjon(ArkivInformasjon.builder()
						.arkivSystem(ARKIV_SYSTEM)
						.arkivId(ARKIV_ID).build())
				.mottaker(createMottakerTo())
				.postadresse(null)
				.dokumenter(createDokument())
				.build();
	}

	private List<Dokument> createDokument() {

		return Arrays.asList(
				Dokument.builder()
						.dokumenttypeId(DOKUMENTTYPE_ID_1)
						.dokumentObjektReferanse(OBJEKT_REFERANSE_1)
						.tilknyttetSom(TILKNYTTET_SOM_HOVEDDOK)
						.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_1)
						.build(),
				Dokument.builder()
						.dokumenttypeId(DOKUMENTTYPE_ID_2)
						.dokumentObjektReferanse(OBJEKT_REFERANSE_2)
						.tilknyttetSom(TILKNYTTET_SOM_VEDLEGG)
						.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_2)
						.build(),
				Dokument.builder()
						.dokumenttypeId("1234")
						.dokumentObjektReferanse(OBJEKT_REFERANSE_1)
						.tilknyttetSom(TILKNYTTET_SOM_VEDLEGG)
						.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_1)
						.build());


	}

	private Postadresse createPostadresse() {
		return Postadresse.builder()
				.adresselinje1(ADRESSELINJE_1)
				.adresselinje2(ADRESSELINJE_2)
				.adresselinje3(ADRESSELINJE_3)
				.postnummer(POSTNUMMER)
				.poststed(POSTSTED)
				.landkode(LAND)
				.build();
	}

	private Mottaker createMottakerTo() {
		return Mottaker.builder()
				.mottakerNavn(MOTTAKER_ID_NAVN)
				.mottakerId(MOTTAKER_ID)
				.mottakerType(ORGANISASJON)
				.build();
	}

}