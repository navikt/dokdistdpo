package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ArkivInformasjon;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Dokument;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Mottaker;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OpprettForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Postadresse;
import no.nav.dokdistdpo.exception.functional.DokdistdpoIllegalArgumentException;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static no.nav.dokdistdpo.utils.DokdistdpoUtils.assertNotEmpty;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.assertNotNull;
import static org.springframework.util.ObjectUtils.isEmpty;

public class OpprettForsendelseMapper {

	private static final String DISTRIBUSJON_KANAL_PRINT = "PRINT";
	private static final String DOKUMENTTYPE_ID = "U000001";
	private static final String HOVEDDOKUMENT = "HOVEDDOKUMENT";

	public OpprettForsendelseRequest map(HentForsendelseResponse hentForsendelseResponse, String nyBestillingsId) {
		assertThatAllRequiredFieldsArePresent(hentForsendelseResponse);
		AtomicReference<Integer> rekkefolge = new AtomicReference<>(2);

		return OpprettForsendelseRequest.builder()
				.bestillingsId(nyBestillingsId)
				.distribusjonsKanal(DISTRIBUSJON_KANAL_PRINT)
				.tema(hentForsendelseResponse.tema())
				.forsendelseTittel(hentForsendelseResponse.forsendelseTittel())
				.bestillendeFagsystem(hentForsendelseResponse.bestillendeFagsystem())
				.originalDistribusjonId(hentForsendelseResponse.bestillingsId())
				.mottaker(mapMottakerTo(hentForsendelseResponse.mottaker()))
				.arkivInformasjon(mapArkivInformasjonTo(hentForsendelseResponse.arkivInformasjon()))
				.postadresse(mapPostadresse(hentForsendelseResponse.postadresse()))
				.dokumenter(hentForsendelseResponse.dokumenter().stream()
						.map(dokumentTo -> {
							if (isHoveddokument(dokumentTo.tilknyttetSom())) {
								return mapDokument(dokumentTo, 1);
							} else {
								Dokument dok = mapDokument(dokumentTo, rekkefolge.get());
								rekkefolge.getAndSet(rekkefolge.get() + 1);
								return dok;
							}
						})
						.collect(Collectors.toList()))
				.build();
	}

	private Dokument mapDokument(Dokument dokument, Integer rekkefolge) {
		return Dokument.builder()
				.tilknyttetSom(dokument.tilknyttetSom())
				.dokumentObjektReferanse(dokument.dokumentObjektReferanse())
				.arkivDokumentInfoId(dokument.arkivDokumentInfoId())
				.rekkefolge(rekkefolge)
				.dokumenttypeId(DOKUMENTTYPE_ID)
				.build();
	}

	private Postadresse mapPostadresse(Postadresse postadresse) {
		return isEmpty(postadresse) ? null : Postadresse.builder()
				.adresselinje1(postadresse.adresselinje1())
				.adresselinje2(postadresse.adresselinje2())
				.adresselinje3(postadresse.adresselinje3())
				.postnummer(postadresse.postnummer())
				.poststed(postadresse.poststed())
				.landkode(postadresse.landkode())
				.build();
	}

	private ArkivInformasjon mapArkivInformasjonTo(ArkivInformasjon arkivInformasjon) {
		return ArkivInformasjon.builder()
				.arkivSystem(arkivInformasjon.arkivSystem())
				.arkivId(arkivInformasjon.arkivId())
				.build();
	}

	private Mottaker mapMottakerTo(Mottaker mottaker) {
		assertNotNull("Mottaker", mottaker);
		return Mottaker.builder()
				.mottakerId(mottaker.mottakerId())
				.mottakerNavn(mottaker.mottakerNavn())
				.mottakerType(mottaker.mottakerType())
				.build();
	}

	private boolean isHoveddokument(String tilknyttetSom) {
		return HOVEDDOKUMENT.equals(tilknyttetSom);
	}

	private void assertThatAllRequiredFieldsArePresent(HentForsendelseResponse hentForsendelse) {
		assertNotNull("hentForsendelseResponse", hentForsendelse);
		assertNotEmpty("bestillingsId", hentForsendelse.bestillingsId());
		assertNotEmpty("bestillendeFagsystem", hentForsendelse.bestillendeFagsystem());
		assertNotEmpty("tema", hentForsendelse.tema());
		assertNotEmpty("forsendelsetittel", hentForsendelse.forsendelseTittel());
		assertNotNull("Mottaker", hentForsendelse.mottaker());
		assertNotEmpty("mottaker.mottakerId", hentForsendelse.mottaker().mottakerId());
		assertNotEmpty("mottaker.mottakerNavn", hentForsendelse.mottaker().mottakerNavn());
		assertNotNull("mottaker.mottakerType", hentForsendelse.mottaker().mottakerType());

		if (hentForsendelse.arkivInformasjon() != null) {
			assertNotNull("arkivinformasjon.arkivSystem", hentForsendelse.arkivInformasjon().arkivSystem());
			assertNotNull("arkivinformasjon.arkivId", hentForsendelse.arkivInformasjon().arkivId());
		}

		if (hentForsendelse.postadresse() != null) {
			assertNotNull("postadresse.landkode", hentForsendelse.postadresse().landkode());
		}

		assertThatAtLeastOneDocumentIsPresent(hentForsendelse.dokumenter());
		hentForsendelse.dokumenter().forEach(dokumentTo ->
				assertDokument(dokumentTo, hentForsendelse.arkivInformasjon()));
	}

	private void assertDokument(Dokument dokument, ArkivInformasjon arkivInformasjon) {
		assertNotNull("dokumenter.dokument.tilknyttetSom", dokument.tilknyttetSom());
		assertNotNull("dokumenter.dokument.dokumentObjektReferanse", dokument.dokumentObjektReferanse());
		assertNotNull("dokumenter.dokument.dokumenttypeId", dokument.dokumenttypeId());

		if (arkivInformasjon != null) {
			assertNotNull("dokumenter.dokument.arkivdokumentInfoId", dokument.arkivDokumentInfoId());
		}
	}

	private void assertThatAtLeastOneDocumentIsPresent(List<Dokument> dokumentList) {
		if (dokumentList == null || dokumentList.isEmpty()) {
			throw new DokdistdpoIllegalArgumentException("Ugyldig input: Feltet dokumenter må være en liste som inneholder minst ett dokumnet");
		}
	}
}
