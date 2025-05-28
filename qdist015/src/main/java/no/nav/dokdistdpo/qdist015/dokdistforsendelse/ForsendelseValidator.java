package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ArkivInformasjon;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Dokument;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Mottaker;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Postadresse;
import no.nav.dokdistdpo.exception.functional.DokdistdpoIllegalArgumentException;
import no.nav.dokdistdpo.exception.functional.InvalidForsendelseStatusException;

import java.util.List;

import static java.lang.String.format;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.FORSENDELSE_STATUS_KLAR_FOR_DIST;
import static no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType.DPO_ARKIVMELDING;
import static no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType.DPO_AVTALEMELDING;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.assertNotEmpty;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.assertNotNull;


public class ForsendelseValidator {

	private static final String HOVEDDOKUMENT = "HOVEDDOKUMENT";

	public void assertHentForsendelse(HentForsendelseResponse response) {
		assertNotNull("hentForsendelseResponse", response);
		assertMottaker(response.mottaker());
		validateForsendelseStatus(response.forsendelseStatus());
		assertNotEmpty("forsendelse.forsendelseMetadata", response.forsendelseMetadata());
		assertNotEmpty("forsendelse.forsendelseMetadataType", response.forsendelseMetadataType());

		if (!isAvtaltmeldingOrArkivmelding(response)) {
			throw new DokdistdpoIllegalArgumentException("Ugyldig ForsendelseMetadataType: Verken avtalt eller forsendelseMetadata er angitt, forsendelseMetadataType=" + response.forsendelseMetadataType());
		}
	}

	public static boolean isHoveddokument(String tilknyttetSom) {
		return HOVEDDOKUMENT.equals(tilknyttetSom);
	}

	public void assertOpprettForsendelseRequest(HentForsendelseResponse hentForsendelse) {
		assertBaisFields(hentForsendelse);
		assertNotNull("Mottaker", hentForsendelse.mottaker());
		assertMottaker(hentForsendelse.mottaker());
		assertNotNull("arkivinformasjon", hentForsendelse.arkivInformasjon());
		assertArkivInformasjon(hentForsendelse.arkivInformasjon());
		assertNotNull("postadresse", hentForsendelse.postadresse());
		assertLandkodeIfPresent(hentForsendelse.postadresse());
		assertThatAtLeastOneDocumentIsPresent(hentForsendelse.dokumenter());
		hentForsendelse.dokumenter().forEach(this::assertDokument);
	}

	public void validateForsendelseStatus(String forsendelseStatus) {
		if (!FORSENDELSE_STATUS_KLAR_FOR_DIST.equals(forsendelseStatus)) {
			throw new InvalidForsendelseStatusException(format("ForsendelseStatus må være %s, fant forsendelseStatus=%s",
					FORSENDELSE_STATUS_KLAR_FOR_DIST, forsendelseStatus));
		}
	}

	public boolean isAvtaltmeldingOrArkivmelding(HentForsendelseResponse response) {
		return DPO_AVTALEMELDING.name().equals(response.forsendelseMetadataType()) || DPO_ARKIVMELDING.name().equals(response.forsendelseMetadataType());
	}

	private void assertBaisFields(HentForsendelseResponse hentForsendelse) {
		assertNotNull("hentForsendelseResponse", hentForsendelse);
		assertNotEmpty("bestillingsId", hentForsendelse.bestillingsId());
		assertNotEmpty("bestillendeFagsystem", hentForsendelse.bestillendeFagsystem());
		assertNotEmpty("tema", hentForsendelse.tema());
		assertNotEmpty("forsendelsetittel", hentForsendelse.forsendelseTittel());
	}

	private void assertMottaker(Mottaker mottaker) {
		assertNotEmpty("mottaker.mottakerId", mottaker.mottakerId());
		assertNotEmpty("mottaker.mottakerNavn", mottaker.mottakerNavn());
		assertNotNull("mottaker.mottakerType", mottaker.mottakerType());
	}

	private void assertLandkodeIfPresent(Postadresse postadresse) {
		assertNotEmpty("postadresse.landkode", postadresse.landkode());
	}

	private void assertArkivInformasjon(ArkivInformasjon arkivInformasjon) {
		assertNotNull("arkivinformasjon.arkivSystem", arkivInformasjon.arkivSystem());
		assertNotNull("arkivinformasjon.arkivId", arkivInformasjon.arkivId());
	}

	private void assertDokument(Dokument dokument) {
		assertNotNull("dokumenter.dokument.tilknyttetSom", dokument.tilknyttetSom());
		assertNotNull("dokumenter.dokument.dokumentObjektReferanse", dokument.dokumentObjektReferanse());
		assertNotNull("dokumenter.dokument.arkivdokumentInfoId", dokument.arkivDokumentInfoId());
	}

	private void assertThatAtLeastOneDocumentIsPresent(List<Dokument> dokumentList) {
		if (dokumentList == null || dokumentList.isEmpty()) {
			throw new DokdistdpoIllegalArgumentException("Ugyldig input: Feltet dokumenter må være en liste som inneholder minst ett dokumnet");
		}
	}
}
