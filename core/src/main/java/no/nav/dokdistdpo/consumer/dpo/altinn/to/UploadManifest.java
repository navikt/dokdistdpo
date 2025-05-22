package no.nav.dokdistdpo.consumer.dpo.altinn.to;

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
