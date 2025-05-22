package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Mottaker;
import no.nav.dokdistdpo.exception.functional.DokdistdpoIllegalArgumentException;
import no.nav.dokdistdpo.exception.functional.InvalidForsendelseStatusException;

import static java.lang.String.format;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.FORSENDELSE_STATUS_KLAR_FOR_DIST;
import static no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType.DPO_ARKIVMELDING;
import static no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType.DPO_AVTALEMELDING;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.assertNotEmpty;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.assertNotNull;

public class HentForsendelseValidator {

	public HentForsendelseValidator() {
	}

	public void assertDokdistForsendelse(HentForsendelseResponse response) {
		assertMottaker(response.mottaker());
		validateForsendelseStatus(response.forsendelseStatus());
		assertNotNull("hentForsendelseResponse", response);
		assertNotEmpty("forsendelse.forsendelseMetadata", response.forsendelseMetadata());
		assertNotEmpty("forsendelse.forsendelseMetadataType", response.forsendelseMetadataType());


		if (!isAvtaltmeldingOrArkivmelding(response)) {
			throw new DokdistdpoIllegalArgumentException("Ugyldig ForsendelseMetadataType: Verken avtalt eller forsendelseMetadata er angitt, forsendelseMetadataType=" + response.forsendelseMetadataType());
		}
	}

	public void assertMottaker(Mottaker mottaker) {
		assertNotEmpty("forsendelse.mottaker.mottakerId", mottaker.mottakerId());
		assertNotEmpty("forsendelse.mottaker.mottakerNavn", mottaker.mottakerNavn());
		assertNotNull("forsendelse.mottaker.mottakerType", mottaker.mottakerType());
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
}
