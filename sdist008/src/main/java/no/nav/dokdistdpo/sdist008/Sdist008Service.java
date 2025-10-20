package no.nav.dokdistdpo.sdist008;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentEformidlingforsendelserResponse.Forsendelse;
import no.nav.dokdistdpo.consumer.dpo.AltinnEformidlingKvitteringClient;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.DownloadResponse;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.KvitteringStatus;
import no.nav.dokdistdpo.sdist008.domain.DpoKvitteringStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.BEKREFTET;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.EKSPEDERT;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.FEILET;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.OVERSENDT;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class Sdist008Service {

	private static final String HENT_KVITTEING = "sdist008 hentet dpo kvittering med kvitteringStatus={}";
	private final AltinnEformidlingKvitteringClient eformidling;
	private final DokdistadminService dokdistadminService;

	public Sdist008Service(AltinnEformidlingKvitteringClient eformidling,
						   DokdistadminService dokdistadminService) {
		this.eformidling = eformidling;
		this.dokdistadminService = dokdistadminService;
	}

	public void hentKvitteringOgOppdaterForsendelseStatus() {
		ForsendelseStatusEndringer forsendelseStatusEndringer = new ForsendelseStatusEndringer();

		List<Forsendelse> forsendelser = dokdistadminService.hentUekspederteDpoForsendelser();
		Map<String, Forsendelse> uekspederteDpoForsendelse = dokdistadminService.mapUekspederteDpoForsendelse(forsendelser);
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
							log.warn("DPO kvitteringe finnnes ikke i oversikten overn uekspederte DPO forsendelser. konversasjonsId={}, downloadResponse={}", konversasjonId, downloadResponse);
						}
					});
			log.info("Sdist008 har oppdatert status på dpo forsendelser: {}", forsendelseStatusEndringer);
		}
	}

	private void behandleForsendelse(Forsendelse forsendelse, DownloadResponse downloadResponse, ForsendelseStatusEndringer endringer) {
		try {
			if (validerForsendelse(forsendelse, downloadResponse)) {
				log.info("Sdist008 behandler forsendelse={}", forsendelse);
				KvitteringStatus kvitteringStatus = downloadResponse.kvitteringStatus();

				if (kvitteringStatus == null) {
					log.info("dpo kvittering har kvitteringStatus=null. Bekrefter denne likevel. downloadResponse={}", downloadResponse);
				} else {
					kontrollerForsendelseStatus(kvitteringStatus.status(), forsendelse, endringer);
				}
				eformidling.bekreftMottattKvittering(downloadResponse.fileReference());

			} else {
				log.warn("sdist001 mottatt kvittering med konversasjonsId={} som ikke samsvarer med forsendelse={}. Ingen handling foretas.",
						downloadResponse.conversationId(), forsendelse);
			}
		} catch (Exception e) {
			log.error("sdist001 avvik har oppstått ved behandling av forsendelse={}. Ingen statusoppdatering er gjort. Feilmelding={}",
					forsendelse, e.getMessage(), e);
		}
	}

	private void kontrollerForsendelseStatus(String kvitteringStatus, Forsendelse forsendelse, ForsendelseStatusEndringer endringer) {
		String forsendelseStatus = forsendelse.forsendelseStatus();

		if (OVERSENDT.name().equals(forsendelseStatus) && !BEKREFTET.name().equals(forsendelseStatus)) {
			log.warn("sdist008 forsendelse={} ble feilaktig returnert av hentEformidlingForsendelser.", forsendelse);
			return;
		}

		if (kvitteringStatus == null) {
			log.error("forsendelse={} mottatt kvittering med kvitteringStatus=null", forsendelse);
			return;
		}
		mapFraDpoOgOppdaterForsendelseStatus(kvitteringStatus, forsendelse, endringer);
	}

	private void mapFraDpoOgOppdaterForsendelseStatus(String kvitteringStatus, Forsendelse forsendelse, ForsendelseStatusEndringer forsendelseStatusEndringer) {

		Long forsendelseId = Long.valueOf(forsendelse.forsendelseId());
		DpoKvitteringStatus dpoKvitteringStatus = DpoKvitteringStatus.valueOf(kvitteringStatus);

		switch (dpoKvitteringStatus) {
			case SENDT -> {
				log.info("sdist008 hentet DPO-kvitteringer med kvitteringStatus={}. Forsendelser med forsendelseIder: ({}) oppdateres til BEKREFTET", kvitteringStatus, forsendelse);
				dokdistadminService.oppdaterForsendelse(forsendelseId, BEKREFTET.name());
				forsendelseStatusEndringer.bekreftet.add(forsendelseId);
			}
			case MOTTATT ->
					log.info(HENT_KVITTEING + "Ingen handling foretas. forsendelse={}", kvitteringStatus, forsendelse);
			case LEVERT, LEST -> {
				log.info(HENT_KVITTEING + "Forsendelse med ({}) oppdateres til EKSPEDERT", kvitteringStatus, forsendelse);
				dokdistadminService.oppdatereForsendelseTilEkspedert(forsendelseId, kvitteringStatus);
				forsendelseStatusEndringer.ekspedert.add(forsendelseId);
			}
			case FAIL ->
					log.info("kvitteringen feilet med kvitteringStatus={}. forsendelse={}", kvitteringStatus, forsendelse);
			case LEVETID_UTLOPT -> {
				log.info("sdist008 avvik har oppstått med kvitteringStatus={}. Forsendelse med ({}) settes til FEILET", kvitteringStatus, forsendelse);
				dokdistadminService.oppdaterForsendelse(forsendelseId, FEILET.name());
				forsendelseStatusEndringer.feilet.add(forsendelseId);
			}
		}
	}

	private boolean validerForsendelse(Forsendelse forsendelse, DownloadResponse downloadResponse) {
		return downloadResponse.conversationId().equals(forsendelse.konversasjonId()) &&
				!EKSPEDERT.name().equals(forsendelse.forsendelseStatus());
	}
}
