package no.nav.dokdistdpo.consumer.dpo.altinn2;

import jakarta.activation.DataHandler;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.mapper.InputStreamDataSource;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.service.AltinnBrokerServiceExternal;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.service.AltinnBrokerServiceStreamed;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.ReceiptTo;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.UploadManifest;
import no.nav.dokdistdpo.consumer.dpo.packaging.DpoMessagePackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;

@Slf4j
@Component
public class Altinn2EformidlingClient {

	private final AppCertificate appCertificate;
	private final DpoMessagePackager dpoMessagePackager;
	private final AltinnBrokerServiceExternal altinnBrokerServiceExternal;
	private final AltinnBrokerServiceStreamed altinnBrokerServiceStreamed;

	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");
	private static final String FILE_NAME = "sbd.zip";

	public Altinn2EformidlingClient(AppCertificate appCertificate,
									DpoMessagePackager dpoMessagePackager,
									AltinnBrokerServiceExternal altinnBrokerServiceExternal,
									AltinnBrokerServiceStreamed altinnBrokerServiceStreamed) {
		this.appCertificate = appCertificate;
		this.dpoMessagePackager = dpoMessagePackager;
		this.altinnBrokerServiceExternal = altinnBrokerServiceExternal;
		this.altinnBrokerServiceStreamed = altinnBrokerServiceStreamed;
	}

	public void send(AltinnDpoRequest altinnDpoRequest) {
		AltinnDpoRequest.Forsendelse forsendelse = altinnDpoRequest.forsendelse();

		log.info("Hentet mottakerNavn={} for {}. conversationId={}, bestillingsId={}", forsendelse.organisasjonsnavn(),
				forsendelse.mottakerId(), forsendelse.konversjonsId(), forsendelse.bestillingsId());

		final InputStream sbdZip = dpoMessagePackager.packageMessage(altinnDpoRequest,
				appCertificate, altinnDpoRequest.dpoMottakerInfo().x509Certificate());

		secureLog.info("Distribuerer forsendelse med konversasjonId={} til Altinn", forsendelse.konversjonsId());

		final UploadManifest uploadManifest = mapUploadManifest(altinnDpoRequest);

		log.info("Initialiserer Altinn broker med manifest={}, conversationId={}, bestillingsId={}", uploadManifest,
				altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());
		final String fileReference = altinnBrokerServiceExternal.initiateBrokerService(uploadManifest);
		log.info("Altinn broker Initialisert OK. fileReference={}, conversationId={}, bestillingsId={}", fileReference,
				altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());
		secureLog.info("Altinn broker Initialisert OK. fileReference={}", fileReference);

		log.info("Laster opp til Altinn fileReference={}, conversationId={}, bestillingsId={}", fileReference,
				altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());
		ReceiptTo receiptTo = altinnBrokerServiceStreamed.uploadFileToAltinn(fileReference, FILE_NAME, new DataHandler(InputStreamDataSource.of(sbdZip)));
		log.info("Lastet opp OK. receipt={}, conversationId={}, bestillingsId={}", receiptTo,
				altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());
		secureLog.info("Lastet opp OK. receipt={}", receiptTo);
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

}
