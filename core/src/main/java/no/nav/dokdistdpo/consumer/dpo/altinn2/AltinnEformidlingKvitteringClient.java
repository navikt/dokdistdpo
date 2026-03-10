package no.nav.dokdistdpo.consumer.dpo.altinn2;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.AltinnDokument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.DownloadResponse;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.MessageFromAltinn2;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.service.AltinnBrokerServiceExternal;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.service.AltinnBrokerServiceStreamed;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.SearchCriteria;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.ServiceCode;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.DpoKvitteringMelding;
import no.nav.dokdistdpo.consumer.dpo.packaging.DpoMessageUnpacker;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static no.altinn.brokerserviceexternal.BrokerServiceAvailableFileStatus.UPLOADED;

@Slf4j
@Component
public class AltinnEformidlingKvitteringClient {

	private final AltinnBrokerServiceExternal altinnBrokerServiceExternal;
	private final AltinnBrokerServiceStreamed altinnBrokerServiceStreamed;
	private final DpoMessageUnpacker dpoMessageUnpacker;
	private final DokdistdpoProperties.Altinn2Properties altinn2Properties;

	public AltinnEformidlingKvitteringClient(AltinnBrokerServiceExternal altinnBrokerServiceExternal,
											 AltinnBrokerServiceStreamed altinnBrokerServiceStreamed,
											 DpoMessageUnpacker dpoMessageUnpacker,
											 DokdistdpoProperties dokdistdpoProperties) {
		this.altinnBrokerServiceExternal = altinnBrokerServiceExternal;
		this.altinnBrokerServiceStreamed = altinnBrokerServiceStreamed;
		this.dpoMessageUnpacker = dpoMessageUnpacker;
		this.altinn2Properties = dokdistdpoProperties.altinn2();
	}

	public List<DownloadResponse> hentKvitteringer() {
		ServiceCode serviceCode = getServiceCode();

		log.info("Henter filreferanser til meldinger fra Altinns formidlingstjeneste på serviceCode={}", serviceCode);
		List<String> filreferanser = altinnBrokerServiceExternal.getAvailableFiles(serviceCode, getSearchCriteria());
		log.info("Hentet {} filreferanser fra Altinn, referanser={}", filreferanser.size(), filreferanser);

		log.info("Henter kvitteringsmeldinger fra Altinn");
		List<MessageFromAltinn2> messagesFromAltinn = altinnBrokerServiceStreamed.downloadFilesFromAltinn(filreferanser);
		log.info("Hentet {} meldinger fra Altinn, referanser={}", messagesFromAltinn.size(), messagesFromAltinn.stream()
				.map(MessageFromAltinn2::filreferanse)
				.toList());

		log.info("Pakkes ut meldinger fra Altinn");
		List<AltinnDokument> altinnDokuments = dpoMessageUnpacker.unpackMessageFromAltinn(messagesFromAltinn);

		log.info("Pakket ut {} meldinger fra Altinn: {}",
				altinnDokuments.size(),
				altinnDokuments.stream()
						.map(altinndokument ->
								String.format("[referanse=%s: messageChannel=%s]", altinndokument.fileReference(), altinndokument.dpoKvitteringMelding().getMessageChannelName()))
						.toList());

		hentetKvitteringerLog(altinnDokuments);

		List<DownloadResponse> downloadResponses = getDownloadResponses(altinnDokuments);
		log.info("Meldinger fra Altinn:  {}", downloadResponses);

		return downloadResponses;
	}

	public void bekreftMottattKvittering(String filreferanse) {
		altinnBrokerServiceExternal.confirmDownloaded(filreferanse);
	}

	private ServiceCode getServiceCode() {
		return new ServiceCode(altinn2Properties.serviceCode(), parseInt(altinn2Properties.serviceEditionCode()));
	}

	private SearchCriteria getSearchCriteria() {
		return SearchCriteria.builder()
				.availableFileStatus(UPLOADED)
				.build();
	}

	private List<DownloadResponse> getDownloadResponses(List<AltinnDokument> altinnDokuments) {
		return altinnDokuments.stream().map(DownloadResponse::from).toList();
	}

	private void hentetKvitteringerLog(List<AltinnDokument> altinnDokuments) {
		Map<String, List<String>> groupedByStatus = altinnDokuments.stream()
				.map(AltinnDokument::dpoKvitteringMelding)
				.collect(Collectors.groupingBy(
						DpoKvitteringMelding::getKvitteringStatus,
						Collectors.mapping(DpoKvitteringMelding::getConversationId,
								Collectors.toList())
				));

		groupedByStatus.forEach((status, conversationIds) ->
				log.info("Hentet {} kvitteringer med status={} og conversationIds={}", conversationIds.size(), status, conversationIds));
	}
}
