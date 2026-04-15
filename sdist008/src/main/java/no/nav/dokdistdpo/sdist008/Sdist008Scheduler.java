package no.nav.dokdistdpo.sdist008;

import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.consumer.lederelection.LederElectionConsumer;
import no.nav.dokdistdpo.sdist008.altinn3.Sdist008Altinn3Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static no.nav.dokdistdpo.utils.MdcUtils.clearMDC;
import static no.nav.dokdistdpo.utils.MdcUtils.generateNewCallId;

@Component
public class Sdist008Scheduler {

	private final Sdist008Service sdist008Service;
	private final Sdist008Altinn3Service sdist008Altinn3Service;
	private final DokdistdpoProperties.Altinn3Properties altinn3Properties;
	private final LederElectionConsumer lederelectionConsumer;

	public Sdist008Scheduler(Sdist008Service sdist008Service,
							 Sdist008Altinn3Service sdist008Altinn3Service,
							 DokdistdpoProperties dokdistdpoProperties,
							 LederElectionConsumer lederelectionConsumer) {
		this.sdist008Service = sdist008Service;
		this.sdist008Altinn3Service = sdist008Altinn3Service;
		this.altinn3Properties = dokdistdpoProperties.altinn3();
		this.lederelectionConsumer = lederelectionConsumer;
	}

	@Scheduled(fixedDelayString = "${sdist008.intervall:600000}")
	public void ekspederDpoForsendelser() {
		if (lederelectionConsumer.isLeder()) {
			generateNewCallId();
			try {
				if (altinn3Properties.enabled()) {
					sdist008Altinn3Service.oppdaterForsendelseMedFilstatusFraAltinn3Formidling();
				} else {
					sdist008Service.hentKvitteringOgOppdaterForsendelseStatus();
				}
			} finally {
				clearMDC();
			}
		}
	}
}
