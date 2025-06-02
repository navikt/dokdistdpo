package no.nav.dokdistdpo.consumer.juridisk;

import lombok.Builder;

@Builder
public record LoggmeldingRequest(
		String meldingsId,
		String avsender,
		String mottaker,
		String joarkRef,
		byte[] meldingsInnhold,
		Integer antallAarLagres) {
}
