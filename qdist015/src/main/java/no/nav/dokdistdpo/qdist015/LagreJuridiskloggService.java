package no.nav.dokdistdpo.qdist015;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.juridisk.JuridiskLoggConsumer;
import no.nav.dokdistdpo.consumer.juridisk.LoggmeldingRequest;
import no.nav.dokdistdpo.consumer.juridisk.LoggmeldingResponse;
import no.nav.dokdistdpo.exception.functional.LagreJuridiskLoggFunctionalException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;

@Slf4j
@Component
public class LagreJuridiskloggService {

	private final JuridiskLoggConsumer juridiskLoggConsumer;

	public LagreJuridiskloggService(JuridiskLoggConsumer juridiskLoggConsumer) {
		this.juridiskLoggConsumer = juridiskLoggConsumer;
	}

	public void lagreJuridisklogg(AltinnDpoRequest altinnDpoRequest) {
		LoggmeldingResponse loggmeldingResponse = juridiskLoggConsumer.lagreJuridisklogg(mapToLoggmeldingRequest(altinnDpoRequest));
		log.info("Hendelse med konversasjonsId={} logget til juridisk logg med id={}.",
				altinnDpoRequest.forsendelse().konversjonsId(), loggmeldingResponse.id());
	}

	public LoggmeldingRequest mapToLoggmeldingRequest(AltinnDpoRequest altinnDpoRequest) {
		return LoggmeldingRequest.builder()
				.avsender(NAV_ORGNUMMER)
				.mottaker(altinnDpoRequest.forsendelse().mottakerId())
				.meldingsId(altinnDpoRequest.forsendelse().konversjonsId())
				.joarkRef(altinnDpoRequest.forsendelse().journalpostId())
				.meldingsInnhold(sbdToByteArray(altinnDpoRequest.businessDocument()))
				.build();
	}

	private byte[] sbdToByteArray(StandardBusinessDocument sbd) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(sbd);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new LagreJuridiskLoggFunctionalException(e.getMessage(), e);
		}
	}
}
