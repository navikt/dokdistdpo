package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

@Builder
public record OppdaterForsendelseRequest(
		Long forsendelseId,
		String forsendelseStatus,
		String konversasjonId) {
}
