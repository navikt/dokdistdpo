package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.DokdistAdminConsumer;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.FeilregistrerForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OpprettForsendelseRequest;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static java.lang.String.valueOf;
import static java.time.LocalDateTime.now;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.FORSENDELSE_STATUS_KLAR_FOR_DIST;
import static no.nav.dokdistdpo.qdist015.Qdist015Service.PROPERTY_BESTILLINGS_ID;

@Component
public class SendTilPrintService {

	private static final String DETALJER = "Mottaker er ikke registrert som DPO-mottaker i service registry.";
	public static final String SEND_TIL_PRINT = "direct:sendTilPrint";


	private final DokdistAdminConsumer dokdistAdminConsumer;
	private final OpprettForsendelseMapper opprettForsendelseMapper;
	private final ProducerTemplate producerTemplate;

	public SendTilPrintService(DokdistAdminConsumer dokdistAdminConsumer, ProducerTemplate producerTemplate) {
		this.dokdistAdminConsumer = dokdistAdminConsumer;
		this.producerTemplate = producerTemplate;
		this.opprettForsendelseMapper = new OpprettForsendelseMapper();
	}

	public DistribuerTilKanal sendTilPrint(HentForsendelseResponse hentForsendelseResponse, Exchange exchange) {
		String nyBestillingsId = UUID.randomUUID().toString();
		exchange.setProperty(PROPERTY_BESTILLINGS_ID, nyBestillingsId);
		OpprettForsendelseRequest opprettForsendelse = opprettForsendelseMapper.map(hentForsendelseResponse, nyBestillingsId);

		Long nyForsendelseId = dokdistAdminConsumer.opprettForsendelse(opprettForsendelse);

		opprettFeilregistrerForsendelse(hentForsendelseResponse.forsendelseId(), opprettForsendelse.bestillingsId());

		dokdistAdminConsumer.oppdaterForsendelse(
				OppdaterForsendelseRequest.builder()
						.forsendelseId(nyForsendelseId)
						.forsendelseStatus(FORSENDELSE_STATUS_KLAR_FOR_DIST)
						.build());

		DistribuerTilKanal distribuerTilKanal = new DistribuerTilKanal().useForsendelseId(valueOf(nyForsendelseId));
		producerTemplate.sendBody(SEND_TIL_PRINT,
				distribuerTilKanal);

		return distribuerTilKanal;
	}

	private void opprettFeilregistrerForsendelse(Long oldForsendelseId, String nyBestillingId) {
		dokdistAdminConsumer.feilregistrerForsendelse(FeilregistrerForsendelseRequest.builder()
				.forsendelseId(oldForsendelseId)
				.feilTypeCode("MELDINGSFEIL")
				.tidspunkt(now())
				.detaljer(DETALJER)
				.resendingDistribusjonId(nyBestillingId)
				.build());
	}
}
