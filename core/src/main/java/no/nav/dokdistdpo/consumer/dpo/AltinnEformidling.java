package no.nav.dokdistdpo.consumer.dpo;

import jakarta.activation.DataHandler;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.altinn.mapper.InputStreamDataSource;
import no.nav.dokdistdpo.consumer.dpo.altinn.service.AltinnBrokerServiceExternal;
import no.nav.dokdistdpo.consumer.dpo.altinn.service.AltinnBrokerServiceStreamed;
import no.nav.dokdistdpo.consumer.dpo.altinn.to.ReceiptTo;
import no.nav.dokdistdpo.consumer.dpo.altinn.to.UploadManifest;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;

@Slf4j
@Component
public class AltinnEformidling implements Eformidling {

	private final AppCertificate appCertificate;
	private final DpoMessagePackager dpoMessagePackager;
	private final AltinnBrokerServiceExternal altinnBrokerServiceExternal;
	private final AltinnBrokerServiceStreamed altinnBrokerServiceStreamed;

	private static final String FILE_NAME = "sbd.zip";

	public AltinnEformidling(AppCertificate appCertificate,
							 DpoMessagePackager dpoMessagePackager,
							 AltinnBrokerServiceExternal altinnBrokerServiceExternal,
							 AltinnBrokerServiceStreamed altinnBrokerServiceStreamed) {
		this.appCertificate = appCertificate;
		this.dpoMessagePackager = dpoMessagePackager;
		this.altinnBrokerServiceExternal = altinnBrokerServiceExternal;
		this.altinnBrokerServiceStreamed = altinnBrokerServiceStreamed;
	}

	@Override
	public void send(AltinnDpoRequest altinnDpoRequest) {

		log.info("Hentet mottakerInfo={} for {}. conversationId={}, bestillingsId={}", altinnDpoRequest.registryMottakerInfo(),
				altinnDpoRequest.forsendelse().organisasjonsnavn(), altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());

		final InputStream sbdZip = dpoMessagePackager.packageMessage(altinnDpoRequest,
				appCertificate, altinnDpoRequest.registryMottakerInfo().x509Certificate());

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
				.avsender(NAV_ORGNUMMER)
				.serviceCode(altinnDpoRequest.registryMottakerInfo().serviceCode())
				.serviceEditionCode(altinnDpoRequest.registryMottakerInfo().serviceEditionCode())
				.fileZipName(FILE_NAME)
				.senderReference(altinnDpoRequest.forsendelse().konversjonsId())
				.build();
	}
}
