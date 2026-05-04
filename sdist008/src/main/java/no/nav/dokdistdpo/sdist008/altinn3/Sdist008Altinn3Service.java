package no.nav.dokdistdpo.sdist008.altinn3;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentEformidlingforsendelserResponse;
import no.nav.dokdistdpo.consumer.dpo.altinn3.Altinn3BrokerClient;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.KvitteringStatus;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.DownloadResponse;
import no.nav.dokdistdpo.sdist008.DokdistForsendelseService;
import no.nav.dokdistdpo.sdist008.ForsendelseStatusEndringer;
import no.nav.dokdistdpo.sdist008.domain.FormidlingFilstatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static no.nav.dokdistdpo.sdist008.StatusovergangValidator.erKlarForBehandling;
import static no.nav.dokdistdpo.sdist008.StatusovergangValidator.loggIngenHandling;
import static no.nav.dokdistdpo.sdist008.StatusovergangValidator.validerForsendelseOgDpoKvitteringStatus;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.BEKREFTET;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.FEILET;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class Sdist008Altinn3Service {

	private static final String HENT_KVITTERING = "sdist008 hentet filstatus status med status={}. ";
	private static final String AVSLUTTET_BEHANDLING = "Sdist008 hentet filstatus fra altinn3 med status={}, jobben avsluttes uten videre handling for forsendelse:{}.";
	private static final String FEIL_VED_BEHANDLING = "sdist008 avvik har oppstått ved behandling av forsendelse={}. Ingen statusoppdatering er gjort. Feilmelding={}";

	private final Altinn3BrokerClient altinn3BrokerClient;
	private final Altinn3FileStatusService altinn3FileStatusService;
	private final DokdistForsendelseService dokdistForsendelseService;

	public Sdist008Altinn3Service(Altinn3BrokerClient altinn3BrokerClient,
								  Altinn3FileStatusService altinn3FileStatusService,
								  DokdistForsendelseService dokdistForsendelseService) {
		this.altinn3BrokerClient = altinn3BrokerClient;
		this.altinn3FileStatusService = altinn3FileStatusService;
		this.dokdistForsendelseService = dokdistForsendelseService;
	}

	public void oppdaterForsendelse() {
		ForsendelseStatusEndringer forsendelseStatusEndringer = new ForsendelseStatusEndringer();

		List<HentEformidlingforsendelserResponse.Forsendelse> forsendelser = dokdistForsendelseService.hentGyldigUekspederteForsendelser();
		Map<String, HentEformidlingforsendelserResponse.Forsendelse> uekspederteDpoForsendelser = dokdistForsendelseService.mapUekspederteForsendelseByKonversasjonId(forsendelser);
		log.info("Sdist008 hentet antall={} uekspederte DPO forsendelser fra dokdistadmin", uekspederteDpoForsendelser.size());

		if (isEmpty(uekspederteDpoForsendelser)) {
			log.info("Ingen uekspederte forsendelser funnet for avstemming. Avstemming av DPO forsendelser avsluttes.");
		} else {
			altinn3FileStatusService.getAltinn3DpoFileStatuses()
					.forEach(downloadResponse -> {
						behandleForsendelseOgFormidlingStatus(downloadResponse, uekspederteDpoForsendelser, forsendelseStatusEndringer);
						log.info("Sdist008 har oppdatert forsendelser med statusEndringer: {}", forsendelseStatusEndringer);
					});
		}
	}

	private void behandleForsendelseOgFormidlingStatus(DownloadResponse downloadResponse,
													   Map<String, HentEformidlingforsendelserResponse.Forsendelse> uekspederteDpoForsendelser,
													   ForsendelseStatusEndringer forsendelseStatusEndringer) {
		String konversasjonId = downloadResponse.conversationId();
		log.info("Hentet formidling filstatus fra Altinn3 med konversasjonsId={}, filstatus={}",
				konversasjonId, downloadResponse.kvitteringStatus());

		HentEformidlingforsendelserResponse.Forsendelse forsendelse = uekspederteDpoForsendelser.get(konversasjonId);
		if (forsendelse == null) {
			log.warn("Kvitteringen fra Altinn3-formidling finnes ikke i oversikten over uekspederte forsendelser. konversasjonsId={}, downloadResponse={}",
					konversasjonId, downloadResponse);
			return;
		}

		behandleForsendelseOgFormidlingStatus(forsendelse, downloadResponse, forsendelseStatusEndringer);
	}

	private void behandleForsendelseOgFormidlingStatus(HentEformidlingforsendelserResponse.Forsendelse forsendelse, DownloadResponse downloadResponse, ForsendelseStatusEndringer endringer) {
		try {
			log.info("Sdist008 behandler forsendelse={}", forsendelse);

			if (erKlarForBehandling(forsendelse, downloadResponse)) {

				KvitteringStatus kvitteringStatus = downloadResponse.kvitteringStatus();
				if (kvitteringStatus == null) {
					log.info("dpo kvittering har kvitteringStatus=null. Bekrefter denne likevel. downloadResponse={}", downloadResponse);
					bekreftNedlasting(downloadResponse.fileReference());
					return;
				}

				String statusKode = kvitteringStatus.getStatus();
				validerForsendelseOgDpoKvitteringStatus(forsendelse, forsendelse.forsendelseStatus(), statusKode);

				Optional<FormidlingFilstatus> dpoKvitteringStatus = parseKvitteringStatus(statusKode, forsendelse);
				dpoKvitteringStatus.ifPresent(status -> mapFraDpoOppdaterForsendelseStatus(status, forsendelse, endringer));

				bekreftNedlasting(downloadResponse.fileReference());
			} else {
				loggIngenHandling(forsendelse, downloadResponse);
			}

		} catch (Exception e) {
			log.error(FEIL_VED_BEHANDLING, forsendelse, e.getMessage(), e);
		}
	}

	private void bekreftNedlasting(String fileReference) {
		altinn3BrokerClient.confirmDownload(fileReference);
	}

	private Optional<FormidlingFilstatus> parseKvitteringStatus(String kvitteringStatus,
																HentEformidlingforsendelserResponse.Forsendelse forsendelse) {
		try {
			return Optional.of(FormidlingFilstatus.valueOf(kvitteringStatus));
		} catch (IllegalArgumentException _) {
			log.warn("Ukjent kvitteringsstatus mottatt. forsendelseId={}, kvitteringStatus={}", forsendelse.forsendelseId(), kvitteringStatus);
			return Optional.empty();
		}
	}

	private void mapFraDpoOppdaterForsendelseStatus(FormidlingFilstatus formidlingFilStatus,
													HentEformidlingforsendelserResponse.Forsendelse forsendelse,
													ForsendelseStatusEndringer forsendelseStatusEndringer) {

		Long forsendelseId = Long.valueOf(forsendelse.forsendelseId());
		String kvitteringStatus = formidlingFilStatus.name();

		switch (formidlingFilStatus) {
			case OPPRETTET, MOTTATT, FAIL -> log.info(AVSLUTTET_BEHANDLING, kvitteringStatus, forsendelse);
			case SENDT -> {
				log.info("sdist008 hentet DPO-kvitteringer med filstatus={}. Forsendelser med forsendelseIder: ({}) oppdateres til BEKREFTET", kvitteringStatus, forsendelse);
				dokdistForsendelseService.oppdaterForsendelse(forsendelseId, BEKREFTET.name());
				forsendelseStatusEndringer.bekreftet().add(forsendelseId);
			}
			case LEVERT, LEST -> {
				log.info(HENT_KVITTERING + "Forsendelse med ({}) oppdateres til EKSPEDERT", kvitteringStatus, forsendelse);
				dokdistForsendelseService.oppdatereForsendelseTilEkspedert(forsendelseId, kvitteringStatus);
				forsendelseStatusEndringer.ekspedert().add(forsendelseId);
			}
			case LEVETID_UTLOPT -> {
				log.info("sdist008 avvik har oppstått med filstatus={}. Forsendelse med ({}) settes til FEILET", kvitteringStatus, forsendelse);
				dokdistForsendelseService.oppdaterForsendelse(forsendelseId, FEILET.name());
				forsendelseStatusEndringer.feilet().add(forsendelseId);
			}
		}
	}
}
