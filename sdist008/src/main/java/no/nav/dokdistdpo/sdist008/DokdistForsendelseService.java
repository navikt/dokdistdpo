package no.nav.dokdistdpo.sdist008;

import no.nav.dokdistdpo.consumer.dokdistadmin.DokdistAdminConsumer;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentEformidlingforsendelserResponse.Forsendelse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OppdaterForsendelseRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.EKSPEDERT;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.FEILET;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.KLAR_FOR_DIST;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.OPPRETTET;

@Component()
public class DokdistForsendelseService {

	private final DokdistAdminConsumer dokdistAdminConsumer;
	private final JuridiskLoggService juridiskLoggService;

	private static final Set<String> UGYLDIG_FORSENDELSE_STATUS = Set.of(OPPRETTET.name(), KLAR_FOR_DIST.name(), FEILET.name(), EKSPEDERT.name());

	public DokdistForsendelseService(DokdistAdminConsumer dokdistAdminConsumer,
									 JuridiskLoggService juridiskLoggService) {
		this.dokdistAdminConsumer = dokdistAdminConsumer;
		this.juridiskLoggService = juridiskLoggService;
	}

	public Map<String, Forsendelse> mapUekspederteForsendelseByKonversasjonId(List<Forsendelse> ukspederteDpoForsendelser) {
		return ukspederteDpoForsendelser.stream()
				.collect(toMap(Forsendelse::konversasjonId, forsendelse -> forsendelse));
	}

	public List<Forsendelse> hentGyldigUekspederteForsendelser() {
		return dokdistAdminConsumer.hentEformidlingForsendelser().forsendelser()
				.stream()
				.filter(this::isValidForsendelse)
				.toList();
	}

	private boolean isValidForsendelse(Forsendelse forsendelse) {
		return !UGYLDIG_FORSENDELSE_STATUS.contains(forsendelse.forsendelseStatus()) && forsendelse.konversasjonId() != null;
	}

	public void oppdatereForsendelseTilEkspedert(Long forsendelseId, String kvitteringStatus) {
		HentForsendelseResponse hentForsendelseResponse = dokdistAdminConsumer.hentForsendelse(forsendelseId);
		juridiskLoggService.lagreJuridisklogg(hentForsendelseResponse, kvitteringStatus);

		dokdistAdminConsumer.oppdaterForsendelse(OppdaterForsendelseRequest.builder()
				.forsendelseId(forsendelseId)
				.forsendelseStatus(EKSPEDERT.name())
				.build());
	}

	public void oppdaterForsendelse(Long forsendelseId, String forsendelseStatus) {
		dokdistAdminConsumer.oppdaterForsendelse(OppdaterForsendelseRequest.builder()
				.forsendelseId(forsendelseId)
				.forsendelseStatus(forsendelseStatus)
				.build());
	}
}
