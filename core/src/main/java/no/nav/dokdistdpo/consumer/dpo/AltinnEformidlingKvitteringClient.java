package no.nav.dokdistdpo.consumer.dpo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.AltinnDokument;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.DownloadResponse;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.MessageFromAltinn;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.service.AltinnBrokerServiceExternal;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.service.AltinnBrokerServiceStreamed;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to.SearchCriteria;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to.ServiceCode;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.altinn.brokerserviceexternal.BrokerServiceAvailableFileStatus.UPLOADED;

@Slf4j
@Component
public class AltinnEformidlingKvitteringClient {

	private final AltinnBrokerServiceExternal altinnBrokerServiceExternal;
	private final AltinnBrokerServiceStreamed altinnBrokerServiceStreamed;
	private final DpoMessageUnpacker dpoMessageUnpacker;

	private static final String DPO_SERVICE_CODE = "4192";
	private static final int DPO_SERVICE_EDITION_CODE = 270815;

	public AltinnEformidlingKvitteringClient(AltinnBrokerServiceExternal altinnBrokerServiceExternal,
											 AltinnBrokerServiceStreamed altinnBrokerServiceStreamed,
											 DpoMessageUnpacker dpoMessageUnpacker) {
		this.altinnBrokerServiceExternal = altinnBrokerServiceExternal;
		this.altinnBrokerServiceStreamed = altinnBrokerServiceStreamed;
		this.dpoMessageUnpacker = dpoMessageUnpacker;
	}

	public List<DownloadResponse> hentKvitteringer() {
		ServiceCode serviceCode = getServiceCode();

		log.info("Henter filreferanser til meldinger fra Altinns formidlingstjeneste på serviceCode={}", serviceCode);
		List<String> filreferanser = altinnBrokerServiceExternal.getAvailableFiles(serviceCode, getSearchCriteria());
		log.info("Hentet {} filreferanser fra Altinn, referanser={}", filreferanser.size(), filreferanser);

		log.info("Henter kvitteringsmeldinger fra Altinn");
		List<MessageFromAltinn> messagesFromAltinn = altinnBrokerServiceStreamed.downloadFilesFromAltinn(filreferanser);
		log.info("Hentet {} meldinger fra Altinn, referanser={}", messagesFromAltinn.size(), messagesFromAltinn.stream()
				.map(MessageFromAltinn::filreferanse)
				.toList());

		log.info("Pakkes ut meldinger fra Altinn");
		List<AltinnDokument> altinnDokuments = dpoMessageUnpacker.unpackMessageFromAltinn(messagesFromAltinn);

		log.info("Pakket ut {} meldinger fra Altinn: {}",
				altinnDokuments.size(),
				altinnDokuments.stream()
						.map(altinndokument ->
								String.format("referanse=%s:MessageChannel=%s", altinndokument.fileReference(), altinndokument.dpoKvitteringMelding().getMessageChannelName()))
						.toList());

		List<DownloadResponse> downloadResponses = getDownloadResponses(altinnDokuments);
		log.info("Meldinger fra Altinn:  {}", downloadResponses);

		return downloadResponses;
	}

	public void bekreftMottattKvittering(String filreferanse) {
		altinnBrokerServiceExternal.confirmDownloaded(filreferanse);
	}

	private ServiceCode getServiceCode() {
		return new ServiceCode(DPO_SERVICE_CODE, DPO_SERVICE_EDITION_CODE);
	}

	private SearchCriteria getSearchCriteria() {
		return SearchCriteria.builder()
				.availableFileStatus(UPLOADED)
				.build();
	}

	private List<DownloadResponse> getDownloadResponses(List<AltinnDokument> altinnDokuments) {
		return altinnDokuments.stream().map(DownloadResponse::from).toList();
	}

}
