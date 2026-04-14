package no.nav.dokdistdpo.consumer.dpo.altinn3;

import lombok.extern.slf4j.Slf4j;
import no.altinn.services.altinn3.domain.FileTransferInitalizeExt;
import no.altinn.services.altinn3.domain.FileTransferInitializeResponseExt;
import no.altinn.services.altinn3.domain.FileTransferUploadResponseExt;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.packaging.DpoMessagePackager;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

		final byte[] sbdZip = packageMessageAsBytes(altinnDpoRequest);

		FileTransferInitalizeExt fileTransferInitalizeExt = altinn3BrokerMapper.mapInitiateFileTransfer(altinnDpoRequest, sbdZip);

		FileTransferInitializeResponseExt fileTransferInitializeResponse = altinn3BrokerClient.intiateFileTransfer(fileTransferInitalizeExt);
		log.info("Altinn3 broker initialisert OK. fileTransferId={}", fileTransferInitializeResponse.getFileTransferId());

		FileTransferUploadResponseExt fileTransferUploadResponse = altinn3BrokerClient.uploadFileTransfer(
				fileTransferInitializeResponse.getFileTransferId(),
				sbdZip);
		log.info("Lastet opp attachment til Altinn3 med fileTransferId={}", fileTransferUploadResponse.getFileTransferId());
	}

	private byte[] packageMessageAsBytes(AltinnDpoRequest altinnDpoRequest) {
		try (InputStream sbdZip = dpoMessagePackager.packageMessage(
				altinnDpoRequest,
				appCertificate,
				altinnDpoRequest.dpoMottakerInfo().x509Certificate())) {
			return sbdZip.readAllBytes();
		} catch (IOException e) {
			throw new DokdistdpoTechnicalException("Kunne ikke pakke melding for Altinn3", e);
		}
	}

}
