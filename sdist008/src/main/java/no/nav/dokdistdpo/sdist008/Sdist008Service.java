package no.nav.dokdistdpo.sdist008;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentEformidlingforsendelserResponse.Forsendelse;
import no.nav.dokdistdpo.consumer.dpo.altinn2.AltinnEformidlingKvitteringClient;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.DownloadResponse;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.KvitteringStatus;
import no.nav.dokdistdpo.sdist008.domain.DpoKvitteringStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static no.nav.dokdistdpo.sdist008.StatusovergangValidator.isForsendelseIkkeEkspedert;
import static no.nav.dokdistdpo.sdist008.StatusovergangValidator.validerForsendelseOgDpoKvitteringStatus;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.BEKREFTET;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.FEILET;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class Sdist008Service {

	private static final String HENT_KVITTERING = "sdist008 hentet dpo kvittering med kvitteringStatus={}. ";
	private static final String AVSLUTTET_BEHANDLING = "Sdist008 hentet kvittering med kvitteringStatus={}, jobben avsluttes uten videre handling for forsendelse:{}.";

	private final AltinnEformidlingKvitteringClient eformidling;
	private final DokdistForsendelseService dokdistForsendelseService;

	public Sdist008Service(AltinnEformidlingKvitteringClient eformidling,
						   DokdistForsendelseService dokdistForsendelseService) {
		this.eformidling = eformidling;
		this.dokdistForsendelseService = dokdistForsendelseService;
	}

	public void hentKvitteringOgOppdaterForsendelseStatus() {
		ForsendelseStatusEndringer forsendelseStatusEndringer = new ForsendelseStatusEndringer();

		List<Forsendelse> forsendelser = dokdistForsendelseService.hentUekspederteDpoForsendelser();
		Map<String, Forsendelse> uekspederteDpoForsendelse = dokdistForsendelseService.mapUekspederteDpoForsendelse(forsendelser);
		log.info("Sdist008 hentet antall={} uekspederte DPO forsendelser fra dokdistadmin", uekspederteDpoForsendelse.size());

		if (isEmpty(uekspederteDpoForsendelse)) {
			log.info("Ingen uekspederte forsendelser funnet for avstemming. Avstemming av DPO forsendelser avsluttes.");
		} else {
			eformidling.hentKvitteringer()
					.forEach(downloadResponse -> {
						String konversasjonId = downloadResponse.conversationId();
						log.info("Hentet dpo downloadResponse fra Altinn med konversasjonsId={}, sendersReference= {}, kvitteringStatus={}", konversasjonId, downloadResponse.sendersReference(), downloadResponse.kvitteringStatus());

						if (uekspederteDpoForsendelse.containsKey(konversasjonId)) {
							Forsendelse forsendelse = uekspederteDpoForsendelse.get(konversasjonId);
							behandleForsendelse(forsendelse, downloadResponse, forsendelseStatusEndringer);
						} else {
							log.warn("DPO kvitteringen finnnes ikke i oversikten over uekspederte DPO forsendelser. konversasjonsId={}, downloadResponse={}", konversasjonId, downloadResponse);
						}
					});
			log.info("Sdist008 har oppdatert status på dpo forsendelser: {}", forsendelseStatusEndringer);
		}
	}

	private void behandleForsendelse(Forsendelse forsendelse, DownloadResponse downloadResponse, ForsendelseStatusEndringer endringer) {
		try {
			if (isForsendelseIkkeEkspedert(forsendelse, downloadResponse)) {
				log.info("Sdist008 behandler forsendelse={}", forsendelse);
				KvitteringStatus kvitteringStatus = downloadResponse.kvitteringStatus();

				if (kvitteringStatus == null) {
					log.info("dpo kvittering har kvitteringStatus=null. Bekrefter denne likevel. downloadResponse={}", downloadResponse);
				} else {
					validerForsendelseOgDpoKvitteringStatus(forsendelse, forsendelse.forsendelseStatus(), kvitteringStatus.getStatus());
					mapFraDpoOgOppdaterForsendelseStatus(kvitteringStatus.getStatus(), forsendelse, endringer);
				}
				eformidling.bekreftMottattKvittering(downloadResponse.fileReference());

			} else {
				log.warn("sdist008 mottatt kvittering med konversasjonsId={} som ikke samsvarer med forsendelse={}. Ingen handling foretas.",
						downloadResponse.conversationId(), forsendelse);
			}
		} catch (Exception e) {
			log.error("sdist008 avvik har oppstått ved behandling av forsendelse={}. Ingen statusoppdatering er gjort. Feilmelding={}",
					forsendelse, e.getMessage(), e);
		}
	}

	private void mapFraDpoOgOppdaterForsendelseStatus(String kvitteringStatus, Forsendelse forsendelse, ForsendelseStatusEndringer forsendelseStatusEndringer) {

		Long forsendelseId = Long.valueOf(forsendelse.forsendelseId());
		DpoKvitteringStatus dpoKvitteringStatus = DpoKvitteringStatus.valueOf(kvitteringStatus);

		switch (dpoKvitteringStatus) {
			case OPPRETTET, MOTTATT, FAIL -> log.info(AVSLUTTET_BEHANDLING, kvitteringStatus, forsendelse);
			case SENDT -> {
				log.info("sdist008 hentet DPO-kvitteringer med kvitteringStatus={}. Forsendelser med forsendelseIder: ({}) oppdateres til BEKREFTET", kvitteringStatus, forsendelse);
				dokdistForsendelseService.oppdaterForsendelse(forsendelseId, BEKREFTET.name());
				forsendelseStatusEndringer.bekreftet().add(forsendelseId);
			}
			case LEVERT, LEST -> {
				log.info(HENT_KVITTERING + "Forsendelse med ({}) oppdateres til EKSPEDERT", kvitteringStatus, forsendelse);
				dokdistForsendelseService.oppdatereForsendelseTilEkspedert(forsendelseId, kvitteringStatus);
				forsendelseStatusEndringer.ekspedert().add(forsendelseId);
			}
			case LEVETID_UTLOPT -> {
				log.info("sdist008 avvik har oppstått med kvitteringStatus={}. Forsendelse med ({}) settes til FEILET", kvitteringStatus, forsendelse);
				dokdistForsendelseService.oppdaterForsendelse(forsendelseId, FEILET.name());
				forsendelseStatusEndringer.feilet().add(forsendelseId);
			}
		}
	}

}
