package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from;

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
							   String messageChannel,
							   KvitteringStatus kvitteringStatus) {

	public static DownloadResponse from(AltinnDokument altinnDokument) {
		DpoKvitteringMelding dpoKvitteringMelding = altinnDokument.dpoKvitteringMelding();
		return DownloadResponse.builder()
				.processIdentifier(dpoKvitteringMelding.getProcess())
				.documentType(dpoKvitteringMelding.getStandardBusinessDocumentHeader().getDocumentType())
				.conversationId(dpoKvitteringMelding.getConversationId())
				.fileReference(altinnDokument.fileReference())
				.sendersReference(altinnDokument.manifest().getSendersReference())
				.messageChannel(altinnDokument.dpoKvitteringMelding().getMessageChannelName())
				.kvitteringStatus(dpoKvitteringMelding.getStatus())
				.build();
	}
}
