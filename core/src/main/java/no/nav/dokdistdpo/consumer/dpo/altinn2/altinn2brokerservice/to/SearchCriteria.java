package no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to;

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
