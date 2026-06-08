package no.nav.dokdistdpo.sdist008;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.juridisk.JuridiskLoggConsumer;
import no.nav.dokdistdpo.consumer.juridisk.LoggmeldingRequest;
import no.nav.dokdistdpo.consumer.juridisk.LoggmeldingResponse;
import no.nav.dokdistdpo.exception.functional.LagreJuridiskLoggFunctionalException;
import no.nav.dokdistdpo.sdist008.domain.DpoStatusOppdatering;
import org.springframework.stereotype.Component;

import static java.time.LocalDateTime.now;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;

@Slf4j
@Component
public class JuridiskLoggService {

	private static final Integer ANTALL_AAR_LAGRES = 10;

	private final JuridiskLoggConsumer juridiskLoggConsumer;
	private final JsonMapper jsonMapper;

	public JuridiskLoggService(JuridiskLoggConsumer juridiskLoggConsumer,
							   JsonMapper jsonMapper) {
		this.juridiskLoggConsumer = juridiskLoggConsumer;
		this.jsonMapper = jsonMapper;
	}

	public void lagreJuridisklogg(HentForsendelseResponse hentForsendelseResponse, String dpoKvitteringStatus) {
		try {
			DpoStatusOppdatering dpoStatusOppdatering = new DpoStatusOppdatering(hentForsendelseResponse.konversasjonId(), dpoKvitteringStatus, now());
			byte[] meldingsInnhold = jsonMapper.writeValueAsBytes(dpoStatusOppdatering);
			LoggmeldingRequest loggmeldingRequest = mapDpoKvitteringToLoggmeldingRequest(hentForsendelseResponse, meldingsInnhold);
			LoggmeldingResponse loggmeldingResponse = juridiskLoggConsumer.lagreJuridisklogg(loggmeldingRequest);
			log.info("Hendelse med konversasjonsId={} logget til juridisk logg med id={}.",
					hentForsendelseResponse.konversasjonId(), loggmeldingResponse.id());
		} catch (JacksonException e) {
			throw new LagreJuridiskLoggFunctionalException("Kunne ikke serialisere DPO statusoppdatering til JSON", e);
		}
	}

	private LoggmeldingRequest mapDpoKvitteringToLoggmeldingRequest(HentForsendelseResponse hentForsendelseResponse, byte[] meldingsInnhold) {
		return LoggmeldingRequest.builder()
				.meldingsId(hentForsendelseResponse.konversasjonId())
				.avsender(hentForsendelseResponse.mottaker().mottakerId())
				.mottaker(NAV_ORGNUMMER)
				.joarkRef(hentForsendelseResponse.arkivInformasjon().arkivId())
				.antallAarLagres(ANTALL_AAR_LAGRES)
				.meldingsInnhold(meldingsInnhold)
				.build();
	}
}
