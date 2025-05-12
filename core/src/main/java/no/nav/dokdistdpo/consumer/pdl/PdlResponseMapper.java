package no.nav.dokdistdpo.consumer.pdl;

import no.nav.dokdistdpo.exception.functional.DokdistdpoIllegalArgumentException;

import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.strip;
import static org.springframework.util.CollectionUtils.isEmpty;

public class PdlResponseMapper {

	private PdlResponseMapper() {
	}

	public static HentPersonInfo mapPdlResponse(PdlHentPersonResponse response) {
		if (isNull(response) || isNull(response.data()) || isNull(response.data().hentPerson())) {
			throw new DokdistdpoIllegalArgumentException("Fant ikke brukeren i PDL");
		}

		PdlHentPersonResponse.HentPerson hentPerson = response.data().hentPerson();

		return new HentPersonInfo(getId(hentPerson), getFulltnavn(hentPerson));
	}

	private static String getId(PdlHentPersonResponse.HentPerson hentPerson) {
		return hentPerson.folkeregisteridentifikator().stream()
				.filter(Objects::nonNull)
				.map(PdlHentPersonResponse.Folkeregisteridentifikator::identifikasjonsnummer)
				.findFirst()
				.orElseThrow(() -> new DokdistdpoIllegalArgumentException("Folkeregisteridentifikator ikke funnet"));
	}

	private static String getFulltnavn(PdlHentPersonResponse.HentPerson hentPerson) {
		if (isNull(hentPerson) || isEmpty(hentPerson.navn())) {
			throw new DokdistdpoIllegalArgumentException("Brukernavn kan ikke være null");
		}

		return hentPerson.navn().stream()
				.filter(PdlResponseMapper::isNotBlankForOgEtternavn)
				.map(personNavn ->
						isBlank(personNavn.mellomnavn()) ? strip(personNavn.fornavn() + " " + personNavn.etternavn()) :
						strip(personNavn.fornavn() + " " + personNavn.mellomnavn() + " " + personNavn.etternavn()))
				.findFirst()
				.orElseThrow(() -> new DokdistdpoIllegalArgumentException("Fornavn og etternavn kan ikke være null"));
	}

	private static boolean isNotBlankForOgEtternavn(PdlHentPersonResponse.PersonNavn personNavn) {
		return isNoneBlank(personNavn.fornavn()) && isNoneBlank(personNavn.etternavn());
	}

}
