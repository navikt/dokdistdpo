package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.DokdistAdminConsumer;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OppdaterForsendelseRequest;
import org.springframework.stereotype.Component;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.FORSENDELSE_STATUS_KLAR_FOR_DIST;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.FORSENDELSE_STATUS_OVERSENDT;

@Component
public class OppdaterForsendelseService {

	private final DokdistAdminConsumer dokdistAdminConsumer;

	public OppdaterForsendelseService(DokdistAdminConsumer dokdistAdminConsumer) {
		this.dokdistAdminConsumer = dokdistAdminConsumer;
	}


	public void oppdaterForsendelse(Long forsendelseId) {
		dokdistAdminConsumer.oppdaterForsendelse(OppdaterForsendelseRequest.builder()
				.forsendelseId(forsendelseId)
				.forsendelseStatus(FORSENDELSE_STATUS_OVERSENDT)
				.build());

	}

	public void oppdaterForsendelseMedKlartForDist(Long forsendelseId) {
		dokdistAdminConsumer.oppdaterForsendelse(OppdaterForsendelseRequest.builder()
				.forsendelseId(forsendelseId)
				.forsendelseStatus(FORSENDELSE_STATUS_KLAR_FOR_DIST)
				.build());
	}
}
