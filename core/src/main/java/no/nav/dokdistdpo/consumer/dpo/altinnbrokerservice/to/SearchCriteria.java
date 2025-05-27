package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to;

import lombok.Builder;
import no.altinn.brokerserviceexternal.BrokerServiceAvailableFileStatus;

import java.time.LocalDateTime;

@Builder
public record SearchCriteria(
		BrokerServiceAvailableFileStatus availableFileStatus,
		LocalDateTime minSentDate,
		LocalDateTime maxSentDate
) {
}
