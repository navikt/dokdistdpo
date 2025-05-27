package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to;

import lombok.Builder;

@Builder
public record ReceiptTo(
		String lastChanged,
		Integer parentReceiptId,
		String receiptHistory,
		Integer receiptId,
		String receiptStatusCode,
		String receiptText,
		String receiptTypeName) {
}
