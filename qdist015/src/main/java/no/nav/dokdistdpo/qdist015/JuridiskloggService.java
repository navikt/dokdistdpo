package no.nav.dokdistdpo.qdist015;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dpo.altinn3.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.StandardBusinessDocument;
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
public class JuridiskloggService {

	private final JuridiskLoggConsumer juridiskLoggConsumer;
	private final JsonMapper jsonMapper;

	public JuridiskloggService(JuridiskLoggConsumer juridiskLoggConsumer) {
		this.juridiskLoggConsumer = juridiskLoggConsumer;
		this.jsonMapper = JsonMapper.builder()
				.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
				.build();
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
			jsonMapper.writeValue(baos, standardBusinessDocument);
			return baos.toByteArray();
		} catch (IOException | JacksonException e) {
			throw new LagreJuridiskLoggFunctionalException(e.getMessage(), e);
		}
	}
}
