package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ArkivInformasjon;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Dokument;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Mottaker;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OpprettForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Postadresse;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static no.nav.dokdistdpo.qdist015.dokdistforsendelse.ForsendelseValidator.isHoveddokument;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.assertNotNull;
import static org.springframework.util.ObjectUtils.isEmpty;

public class OpprettForsendelseMapper {

	public static final String DISTRIBUSJON_KANAL_PRINT = "PRINT";
	public static final String DOKUMENTTYPE_ID = "U000001";

	private final ForsendelseValidator forsendelseValidator;

	public OpprettForsendelseMapper() {
		this.forsendelseValidator = new ForsendelseValidator();
	}

	public OpprettForsendelseRequest mapToOpprettForsendelse(HentForsendelseResponse hentForsendelseResponse, String nyBestillingsId) {
		forsendelseValidator.assertOpprettForsendelseRequest(hentForsendelseResponse, nyBestillingsId);
		AtomicReference<Integer> rekkefolge = new AtomicReference<>(2);

		return OpprettForsendelseRequest.builder()
				.bestillingsId(nyBestillingsId)
				.distribusjonsKanal(DISTRIBUSJON_KANAL_PRINT)
				.tema(hentForsendelseResponse.tema())
				.forsendelseTittel(hentForsendelseResponse.forsendelseTittel())
				.bestillendeFagsystem(hentForsendelseResponse.bestillendeFagsystem())
				.dokumentProdApp(hentForsendelseResponse.dokumentProdApp())
				.originalDistribusjonId(hentForsendelseResponse.bestillingsId())
				.distribusjonstype(hentForsendelseResponse.distribusjonstype())
				.distribusjonstidspunkt(hentForsendelseResponse.distribusjonstidspunkt())
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
}
