package no.nav.dokdistdpo.sdist008.domain;

import java.time.LocalDateTime;

public record DpoStatusOppdatering(
		String konversasjonId,
		String status,
		LocalDateTime statusTidspunkt) {
}
