package no.nav.dokdistdpo.sdist008;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentEformidlingforsendelserResponse.Forsendelse;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.DownloadResponse;
import no.nav.dokdistdpo.sdist008.domain.DpoKvitteringStatus;

import java.util.Set;
import java.util.stream.Stream;

import static no.nav.dokdistdpo.sdist008.domain.DpoKvitteringStatus.OPPRETTET;
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

	public static boolean isForsendelseIkkeEkspedert(Forsendelse forsendelse, DownloadResponse downloadResponse) {
		return downloadResponse.conversationId().equals(forsendelse.konversasjonId()) &&
				!EKSPEDERT.name().equals(forsendelse.forsendelseStatus());
	}

	private static boolean isUkjentKvitteringStatus(String forsendelseStatus, String kvitteringStatus) {
		return OVERSENDT_BEKREFTET_STATUS.contains(forsendelseStatus) && Stream.of(DpoKvitteringStatus.values())
				.noneMatch(k -> k.name().equals(kvitteringStatus));
	}

	private static boolean isUlovligStatusovergang(String forsendelseStatus, String kvitteringStatus) {
		return BEKREFTET.name().equals(forsendelseStatus) && OPPRETTET.name().equals(kvitteringStatus);
	}

	private StatusovergangValidator() {
	}
}
