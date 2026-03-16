package no.nav.dokdistdpo.consumer.dpo.altinn3;

import lombok.extern.slf4j.Slf4j;
import no.altinn.services.altinn3.domain.FileTransferInitializeResponseExt;
import no.altinn.services.altinn3.domain.FileTransferUploadResponseExt;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.packaging.DpoMessagePackager;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class Altinn3BrokerService {

	private final Altinn3BrokerClient altinn3BrokerClient;
	private final Altinn3BrokerMapper altinn3BrokerMapper;
	private final AppCertificate appCertificate;
	private final DpoMessagePackager dpoMessagePackager;

	public Altinn3BrokerService(Altinn3BrokerClient altinn3BrokerClient,
								AppCertificate appCertificate,
								DpoMessagePackager dpoMessagePackager) {
		this.altinn3BrokerClient = altinn3BrokerClient;
		this.altinn3BrokerMapper = new Altinn3BrokerMapper();
		this.appCertificate = appCertificate;
		this.dpoMessagePackager = dpoMessagePackager;
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public void sendAltinn3(AltinnDpoRequest altinnDpoRequest) {
		AltinnDpoRequest.Forsendelse forsendelse = altinnDpoRequest.forsendelse();

		log.info("Hentet mottakerNavn={} for {}. konversjonsId={}", forsendelse.organisasjonsnavn(),
				forsendelse.mottakerId(), forsendelse.konversjonsId());

		final InputStream sbdZip = dpoMessagePackager.packageMessage(altinnDpoRequest,
				appCertificate, altinnDpoRequest.dpoMottakerInfo().x509Certificate());

		log.info("Initialiserer Altinn3 broker med konversjonsId={}", forsendelse.konversjonsId());
		FileTransferInitializeResponseExt fileTransferInitializeResponse = altinn3BrokerClient.intiateFileTransfer(altinn3BrokerMapper.mapInitiateFileTransfer(altinnDpoRequest, sbdZip));
		log.info("Altinn3 broker initialisert OK. fileTransferId={}, konversjonsId={}", fileTransferInitializeResponse.getFileTransferId(),
				forsendelse.konversjonsId());

		log.info("Laster opp til Altinn3 fileTransferId={}, konversjonsId={}", fileTransferInitializeResponse.getFileTransferId(), forsendelse.konversjonsId());
		FileTransferUploadResponseExt fileTransferUploadResponse = altinn3BrokerClient.uploadFileTransfer(fileTransferInitializeResponse.getFileTransferId(), sbdZip);
		log.info("Lastet opp OK. fileTransferId={}, konversjonsId={}", fileTransferUploadResponse.getFileTransferId(), forsendelse.konversjonsId());
	}
}
