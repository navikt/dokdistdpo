package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice;

import jakarta.activation.DataHandler;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.DpoMessagePackager;
import no.nav.dokdistdpo.consumer.dpo.DpoMessageUnpacker;
import no.nav.dokdistdpo.consumer.dpo.Eformidling;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.AltinnDokument;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.mapper.InputStreamDataSource;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.service.AltinnBrokerServiceExternal;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.service.AltinnBrokerServiceStreamed;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.DownloadResponse;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.MessageFromAltinn;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to.ReceiptTo;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to.SearchCriteria;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to.ServiceCode;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.to.UploadManifest;
import no.nav.dokdistdpo.consumer.dpo.serviceregistry.DpoMottakerInfo;
import no.nav.dokdistdpo.consumer.dpo.serviceregistry.DpoMottakerInfoService;
import no.nav.dokdistdpo.consumer.dpo.serviceregistry.ServiceRegistryRequest;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

import static no.altinn.brokerserviceexternal.BrokerServiceAvailableFileStatus.UPLOADED;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;

@Slf4j
@Component
public class AltinnEformidling implements Eformidling {

	private final AppCertificate appCertificate;
	private final DpoMessagePackager dpoMessagePackager;
	private final AltinnBrokerServiceExternal altinnBrokerServiceExternal;
	private final AltinnBrokerServiceStreamed altinnBrokerServiceStreamed;
	private final DpoMottakerInfoService dpoMottakerInfoService;
	private final DpoMessageUnpacker dpoMessageUnpacker;

	private static final String FILE_NAME = "sbd.zip";

	public AltinnEformidling(AppCertificate appCertificate,
							 DpoMessagePackager dpoMessagePackager,
							 AltinnBrokerServiceExternal altinnBrokerServiceExternal,
							 AltinnBrokerServiceStreamed altinnBrokerServiceStreamed,
							 DpoMottakerInfoService dpoMottakerInfoService,
							 DpoMessageUnpacker dpoMessageUnpacker) {
		this.appCertificate = appCertificate;
		this.dpoMessagePackager = dpoMessagePackager;
		this.altinnBrokerServiceExternal = altinnBrokerServiceExternal;
		this.altinnBrokerServiceStreamed = altinnBrokerServiceStreamed;
		this.dpoMottakerInfoService = dpoMottakerInfoService;
		this.dpoMessageUnpacker = dpoMessageUnpacker;
	}

	@Override
	public void send(AltinnDpoRequest altinnDpoRequest) {
		AltinnDpoRequest.Forsendelse forsendelse = altinnDpoRequest.forsendelse();

		log.info("Hentet mottakerNavn={} for {}. conversationId={}, bestillingsId={}", forsendelse.organisasjonsnavn(),
				forsendelse.mottakerId(), forsendelse.konversjonsId(), forsendelse.bestillingsId());

		final InputStream sbdZip = dpoMessagePackager.packageMessage(altinnDpoRequest,
				appCertificate, altinnDpoRequest.dpoMottakerInfo().x509Certificate());

		final UploadManifest uploadManifest = mapUploadManifest(altinnDpoRequest);

		log.info("Initialiserer Altinn broker med manifest={}, conversationId={}, bestillingsId={}", uploadManifest,
				altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());
		final String fileReference = altinnBrokerServiceExternal.initiateBrokerService(uploadManifest);
		log.info("Altinn broker Initialisert OK. fileReference={}, conversationId={}, bestillingsId={}", fileReference,
				altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());

		log.info("Laster opp til Altinn fileReference={}, conversationId={}, bestillingsId={}", fileReference,
				altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());
		ReceiptTo receiptTo = altinnBrokerServiceStreamed.uploadFileToAltinn(fileReference, FILE_NAME, new DataHandler(InputStreamDataSource.of(sbdZip)));
		log.info("Lastet opp OK. receipt={}, conversationId={}, bestillingsId={}", receiptTo,
				altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());
	}

	UploadManifest mapUploadManifest(final AltinnDpoRequest altinnDpoRequest) {
		return UploadManifest.builder()
				.mottakerId(altinnDpoRequest.forsendelse().mottakerId())
				.avsender(NAV_ORGNUMMER)
				.serviceCode(altinnDpoRequest.dpoMottakerInfo().serviceCode())
				.serviceEditionCode(altinnDpoRequest.dpoMottakerInfo().serviceEditionCode())
				.fileZipName(FILE_NAME)
				.senderReference(altinnDpoRequest.forsendelse().konversjonsId())
				.build();
	}

	@Override
	public List<DownloadResponse> hent(ServiceRegistryRequest serviceRegistryRequest) {
		ServiceCode serviceCode = getServiceCode(serviceRegistryRequest);

		List<String> filreferanser = altinnBrokerServiceExternal.getAvailableFiles(serviceCode, getSearchCriteria());
		log.info("Hentet {} filreferanser fra Altinn, referanser={}", filreferanser.size(), filreferanser);

		log.info("Henteer kvitteringsmeldinger fra Altinn");
		List<MessageFromAltinn> messagesFromAltinn = altinnBrokerServiceStreamed.downloadFilesFromAltinn(filreferanser);
		log.info("Hentet {} meldinger fra Altinn, referanser={}", messagesFromAltinn.size(), messagesFromAltinn.stream()
				.map(MessageFromAltinn::filreferanse)
				.toList());

		log.info("Pakkes ut meldinger fra Altinn");
		List<AltinnDokument> altinnDokuments = dpoMessageUnpacker.unpackMessage(messagesFromAltinn);

		log.info("Pakket ut {} meldinger fra Altinn: {}",
				altinnDokuments.size(),
				altinnDokuments.stream()
						.map(altinndokument ->
								String.format("referanse=%s:MessageChannel=%s", altinndokument.fileReference(), altinndokument.dpoKvitteringMelding().getMessageChannelName()))
						.toList());

		List<DownloadResponse> downloadResponses = getDownloadResponses(altinnDokuments);
		log.info("Meldinger fra Altinn", downloadResponses);

		return downloadResponses;
	}

	@Override
	public void bekreft(String filreferanse) {
		altinnBrokerServiceExternal.confirmDownloaded(filreferanse);
	}

	private ServiceCode getServiceCode(ServiceRegistryRequest serviceRegistryRequest) {
		DpoMottakerInfo dpoMottakerInfo = dpoMottakerInfoService.hentMottakerInfo(serviceRegistryRequest);
		return new ServiceCode(dpoMottakerInfo.serviceCode(), Integer.parseInt(dpoMottakerInfo.serviceEditionCode()));
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
