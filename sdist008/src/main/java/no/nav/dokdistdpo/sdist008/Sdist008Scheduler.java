package no.nav.dokdistdpo.sdist008;

import no.nav.dokdistdpo.consumer.lederelection.LederElectionConsumer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static no.nav.dokdistdpo.utils.MdcUtils.clearMDC;
import static no.nav.dokdistdpo.utils.MdcUtils.generateNewCallId;

@Component
public class Sdist008Scheduler {

	private final Sdist008Service sdist008Service;
	private final LederElectionConsumer lederelectionConsumer;

	public Sdist008Scheduler(Sdist008Service sdist008Service,
							 LederElectionConsumer lederelectionConsumer) {
		this.sdist008Service = sdist008Service;
		this.lederelectionConsumer = lederelectionConsumer;
	}

	@Scheduled(fixedDelayString = "${sdist008.intervall:600000}")
	public void ekspederDpoForsendelser() {
		if (lederelectionConsumer.isLeder()) {
			generateNewCallId();
			try {
				sdist008Service.hentKvitteringOgOppdaterForsendelseStatus();
			} finally {
				clearMDC();
			}
		}
	}
}
