package no.nav.dokdistdpo.sdist008;

import no.nav.dokdistdpo.consumer.lederelection.LederElectionConsumer;
import no.nav.dokdistdpo.sdist008.altinn3.Sdist008Altinn3Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Sdist008Scheduler {

	private final Sdist008Altinn3Service sdist008Altinn3Service;
	private final LederElectionConsumer lederelectionConsumer;

	public Sdist008Scheduler(Sdist008Altinn3Service sdist008Altinn3Service,
							 LederElectionConsumer lederelectionConsumer) {
		this.sdist008Altinn3Service = sdist008Altinn3Service;
		this.lederelectionConsumer = lederelectionConsumer;
	}

	@Scheduled(fixedDelayString = "${sdist008.intervall:600000}")
	public void ekspederDpoForsendelser() {
		if (lederelectionConsumer.isLeder()) {
			sdist008Altinn3Service.oppdaterForsendelse();
		}
	}
}
