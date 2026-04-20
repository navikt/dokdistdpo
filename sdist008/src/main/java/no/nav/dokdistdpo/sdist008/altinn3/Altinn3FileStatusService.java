package no.nav.dokdistdpo.sdist008.altinn3;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dpo.altinn3.Altinn3BrokerClient;
import no.nav.dokdistdpo.consumer.dpo.altinn3.Altinn3MessageUnpacker;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.AltinnDokument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.DownloadResponse;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.MessageFromAltinn;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@Component
public class Altinn3FileStatusService {

	private final Altinn3BrokerClient altinn3BrokerClient;
	private final Altinn3MessageUnpacker altinn3MessageUnpacker;

	public Altinn3FileStatusService(Altinn3BrokerClient altinn3BrokerClient,
									Altinn3MessageUnpacker altinn3MessageUnpacker) {
		this.altinn3BrokerClient = altinn3BrokerClient;
		this.altinn3MessageUnpacker = altinn3MessageUnpacker;
	}

	public List<DownloadResponse> getAltinn3DpoFileStatuses() {
		List<AltinnDokument> altinnDokuments = altinn3MessageUnpacker.unpackMessageFromAltinn(fileTransferStatusFraAltinn3());

		log.info("Pakket ut {} meldinger fra Altinn: {}",
				altinnDokuments.size(),
				altinnDokuments.stream()
						.map(altinndokument ->
								String.format("[referanse=%s: messageChannel=%s]", altinndokument.fileReference(), altinndokument.dpoKvitteringMelding().getMessageChannelName()))
						.toList());

		List<DownloadResponse> downloadResponses = mapFromSbd(altinnDokuments);
		log.info("Meldinger fra Altinn:  {}", downloadResponses);

		return downloadResponses;
	}

	private List<DownloadResponse> mapFromSbd(List<AltinnDokument> altinnDokuments) {
		return altinnDokuments.stream().map(DownloadResponse::from).toList();
	}

	private List<MessageFromAltinn> fileTransferStatusFraAltinn3() {
		return altinn3BrokerClient.getPublishedFileTransferIder().stream()
				.map(this::mapMessageFromAltinn)
				.toList();
	}

	private MessageFromAltinn mapMessageFromAltinn(String fileTransferId) {
		byte[] streamstatus = altinn3BrokerClient.downloadFilStatus(fileTransferId);
		return new MessageFromAltinn(fileTransferId, new ByteArrayInputStream(streamstatus));
	}
}
