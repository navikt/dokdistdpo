package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.DokdistAdminConsumer;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpo.consumer.dpo.AltinnDpoRequest;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.FORSENDELSE_STATUS_OVERSENDT;

@Component
public class OppdaterForsendelse {

	private final DokdistAdminConsumer dokdistAdminConsumer;

	public OppdaterForsendelse(DokdistAdminConsumer dokdistAdminConsumer) {
		this.dokdistAdminConsumer = dokdistAdminConsumer;
	}

	@Handler
	public void oppdaterForsendelse(@Body AltinnDpoRequest altinnDpoRequest) {
		dokdistAdminConsumer.oppdaterForsendelse(OppdaterForsendelseRequest.builder()
				.forsendelseId(Long.valueOf(altinnDpoRequest.forsendelseId()))
				.forsendelseStatus(FORSENDELSE_STATUS_OVERSENDT)
				.build());

	}
}
