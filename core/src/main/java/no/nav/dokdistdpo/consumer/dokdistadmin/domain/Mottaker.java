package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

@Builder
public record Mottaker(String mottakerId,
					   String mottakerNavn,
					   MottakerType mottakerType) {

	public enum MottakerType {
		PERSON, ORGANISASJON, SAMHANDLER_HPR
	}
}
