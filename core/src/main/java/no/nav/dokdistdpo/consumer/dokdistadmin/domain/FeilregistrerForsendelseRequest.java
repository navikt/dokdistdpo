package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FeilregistrerForsendelseRequest(
		Long forsendelseId,
		String feilTypeCode,
		String part,
		LocalDateTime tidspunkt,
		String detaljer,
		String resendingDistribusjonId
) {
}
