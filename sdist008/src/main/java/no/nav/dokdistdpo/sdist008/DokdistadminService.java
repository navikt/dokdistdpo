package no.nav.dokdistdpo.sdist008;

import no.nav.dokdistdpo.consumer.dokdistadmin.DokdistAdminConsumer;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentEformidlingforsendelserResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OppdaterForsendelseRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.EKSPEDERT;

@Component
public class DokdistadminService {

	private final DokdistAdminConsumer dokdistAdminConsumer;
	private final JuridiskLoggService juridiskLoggService;

	public DokdistadminService(DokdistAdminConsumer dokdistAdminConsumer,
							   JuridiskLoggService juridiskLoggService) {
		this.dokdistAdminConsumer = dokdistAdminConsumer;
		this.juridiskLoggService = juridiskLoggService;
	}

	public Map<String, HentEformidlingforsendelserResponse.Forsendelse> mapUekspederteDpoForsendelse(List<HentEformidlingforsendelserResponse.Forsendelse> ukspederteDpoForsendelser) {
		return ukspederteDpoForsendelser.stream()
				.collect(toMap(HentEformidlingforsendelserResponse.Forsendelse::konversasjonId, forsendelse -> forsendelse));
	}

	public List<HentEformidlingforsendelserResponse.Forsendelse> hentUekspederteDpoForsendelser() {
		return dokdistAdminConsumer.hentEformidlingForsendelser().forsendelser()
				.stream()
				.filter(forsendelse -> !EKSPEDERT.name().equals(forsendelse.forsendelseStatus()) && forsendelse.konversasjonId() != null)
				.toList();
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
