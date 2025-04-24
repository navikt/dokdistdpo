package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

public record OppdaterForsendelseRequest(
		String forsendelseId,
		String forsendelseStatus) {
}
