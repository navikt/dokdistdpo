package no.nav.dokdistdpo.qdist015;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

	private byte[] sbdToByteArray(StandardBusinessDocument standardBusinessDocument) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			mapper.writeValue(baos, standardBusinessDocument);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new LagreJuridiskLoggFunctionalException(e.getMessage(), e);
		}
	}
}
