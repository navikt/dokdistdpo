package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.DokdistAdminConsumer;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.FeilregistrerForsendelseRequest;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Forsendelse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.OpprettForsendelseRequest;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static java.time.LocalDateTime.now;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_BESTILLINGS_ID;

@Component
public class SendTilPrintService {

	private static final String DETALJER = "Mottaker er ikke registrert som DPO-mottaker i service registry.";
	public static final String SEND_TIL_PRINT = "direct:sendTilPrint";

	private final DokdistAdminConsumer dokdistAdminConsumer;
	private final OpprettForsendelseMapper opprettForsendelseMapper;
	private final ProducerTemplate producerTemplate;
	private final ForsendelseValidator forsendelseValidator;

	public SendTilPrintService(DokdistAdminConsumer dokdistAdminConsumer, ProducerTemplate producerTemplate) {
		this.dokdistAdminConsumer = dokdistAdminConsumer;
		this.opprettForsendelseMapper = new OpprettForsendelseMapper();
		this.producerTemplate = producerTemplate;
		this.forsendelseValidator = new ForsendelseValidator();
	}

	@Handler
	public DistribuerTilKanal sendForsendelseTilPrint(HentForsendelseResponse hentForsendelseResponse, Exchange exchange) {
		final String nyBestillingsId = UUID.randomUUID().toString();
		exchange.setProperty(PROPERTY_BESTILLINGS_ID, nyBestillingsId);

		forsendelseValidator.assertOpprettForsendelseRequest(hentForsendelseResponse);
		OpprettForsendelseRequest opprettForsendelse = opprettForsendelseMapper.mapToOpprettForsendelse(hentForsendelseResponse, nyBestillingsId);

		Forsendelse nyForsendelse = dokdistAdminConsumer.opprettForsendelse(opprettForsendelse);

		opprettFeilregistrerForsendelse(hentForsendelseResponse.forsendelseId(), nyBestillingsId);

		DistribuerTilKanal distribuerTilKanal = new DistribuerTilKanal()
				.useForsendelseId(String.valueOf(nyForsendelse.forsendelseId()));

		producerTemplate.sendBody(SEND_TIL_PRINT, distribuerTilKanal);

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
