package no.nav.dokdistdpo.sdist008;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentEformidlingforsendelserResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentEformidlingforsendelserResponse.Forsendelse;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.DownloadResponse;
import no.nav.dokdistdpo.sdist008.domain.FormidlingFilstatus;

import java.util.Set;
import java.util.stream.Stream;

import static no.nav.dokdistdpo.sdist008.domain.FormidlingFilstatus.OPPRETTET;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.BEKREFTET;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.EKSPEDERT;
import static no.nav.dokdistdpo.sdist008.domain.ForsendelseStatus.OVERSENDT;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class StatusovergangValidator {

	static Set<String> OVERSENDT_BEKREFTET_STATUS = Set.of(OVERSENDT.name(), BEKREFTET.name());

	public static void validerForsendelseOgDpoKvitteringStatus(Forsendelse forsendelse, String forsendelseStatus, String kvitteringStatus) {
		if (isBlank(kvitteringStatus)) {
			log.error("forsendelse med forsendelseId={} mottatt kvittering med kvitteringStatus=null", forsendelse.forsendelseId());
			return;
		}

		if (isUlovligStatusovergang(forsendelseStatus, kvitteringStatus) || isUkjentKvitteringStatus(forsendelseStatus, kvitteringStatus)) {
			log.warn("Uløvlig statusovergang for forsendelseId={} med forsendelseStatus={} og kvitteringStatus={}", forsendelse.forsendelseId(), forsendelseStatus, kvitteringStatus);
			return;
		}
	}

	public static boolean isKonversasjonIdMatch(Forsendelse forsendelse, DownloadResponse downloadResponse) {
		return downloadResponse.conversationId().equals(forsendelse.konversasjonId());
	}

	public static boolean isForsendelseEkspedert(Forsendelse forsendelse) {
		return EKSPEDERT.name().equals(forsendelse.forsendelseStatus());
	}

	private static boolean isUkjentKvitteringStatus(String forsendelseStatus, String kvitteringStatus) {
		return OVERSENDT_BEKREFTET_STATUS.contains(forsendelseStatus) && Stream.of(FormidlingFilstatus.values())
				.noneMatch(k -> k.name().equals(kvitteringStatus));
	}

	private static boolean isUlovligStatusovergang(String forsendelseStatus, String kvitteringStatus) {
		return BEKREFTET.name().equals(forsendelseStatus) && OPPRETTET.name().equals(kvitteringStatus);
	}


	public static void loggMelding(HentEformidlingforsendelserResponse.Forsendelse forsendelse, DownloadResponse downloadResponse) {
		if (isForsendelseEkspedert(forsendelse)) {
			log.warn("sdist008 forsendelse={} er allerede ekspedert. Ingen handling foretas.", forsendelse);
		}
		log.warn("sdist008 mottatt kvittering med konversasjonsId={} som ikke samsvarer med forsendelse.konversasjonsIder={}. Ingen handling foretas.",
				downloadResponse.conversationId(), forsendelse);

	}

	private StatusovergangValidator() {
	}
}
