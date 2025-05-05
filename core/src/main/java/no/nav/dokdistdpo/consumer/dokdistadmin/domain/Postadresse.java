package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

@Builder
public record Postadresse(
		String adresselinje1,
		String adresselinje2,
		String adresselinje3,
		String postnummer,
		String poststed,
		String landkode) {
}
