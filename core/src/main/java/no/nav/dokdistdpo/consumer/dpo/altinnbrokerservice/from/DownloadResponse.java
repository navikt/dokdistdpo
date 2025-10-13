package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from;

import lombok.Builder;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.DpoKvitteringMelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.KvitteringStatus;

@Builder
public record DownloadResponse(String processIdentifier,
		String documentType,
		String conversationId,
		String fileReference,
		String sendersReference,
		String sendtDate,
		KvitteringStatus kvitteringStatus ) {

	public static DownloadResponse from(AltinnDokument altinnDokument) {
		DpoKvitteringMelding dpoKvitteringMelding = altinnDokument.dpoKvitteringMelding();
		return DownloadResponse.builder()
				.processIdentifier(dpoKvitteringMelding.getProcess())
				.documentType(dpoKvitteringMelding.getStandardBusinessDocumentHeader().getDocumentType())
				.conversationId(dpoKvitteringMelding.getConversationId())
				.fileReference(altinnDokument.fileReference())
				.sendersReference(altinnDokument.manifest().getSendersReference())
				.kvitteringStatus(dpoKvitteringMelding.getStatus())
				.build();
	}
}
