package no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to;

import lombok.Builder;

@Builder
public record UploadManifest(
		String mottakerId,
		String avsender,
		String serviceCode,
		String serviceEditionCode,
		String fileZipName,
		String senderReference
) {
}
