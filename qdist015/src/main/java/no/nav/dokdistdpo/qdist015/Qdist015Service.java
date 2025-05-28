package no.nav.dokdistdpo.qdist015;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dpo.Eformidling;
import no.nav.dokdistdpo.consumer.dpo.NavDokument;
import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.serviceregistry.DpoMottakerInfo;
import no.nav.dokdistdpo.qdist015.dokdistforsendelse.DokdistadminService;
import no.nav.dokdistdpo.qdist015.dokdistforsendelse.SendTilPrintService;
import no.nav.dokdistdpo.qdist015.map.AltinnRequestMapper;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_FORSENDELSE_ID;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_KONVERSAJON_ID;

@Component
public class Qdist015Service {

	private final GStorageDokumentService gStorageDokumentService;
	private final SendTilPrintService sendTilPrintService;
	private final AltinnRequestMapper altinnRequestMapper;
	private final DokdistadminService dokdistadminService;
	private final Eformidling eformidling;
	private final LagreJuridiskloggService juridiskloggService;

	public Qdist015Service(GStorageDokumentService gStorageDokumentService,
						   SendTilPrintService sendTilPrintService,
						   AltinnRequestMapper altinnRequestMapper,
						   DokdistadminService dokdistadminService,
						   Eformidling eformidling,
						   LagreJuridiskloggService juridiskloggService) {
		this.gStorageDokumentService = gStorageDokumentService;
		this.sendTilPrintService = sendTilPrintService;
		this.altinnRequestMapper = altinnRequestMapper;
		this.dokdistadminService = dokdistadminService;
		this.eformidling = eformidling;
		this.juridiskloggService = juridiskloggService;
	}

	@Handler
	public AltinnDpoRequest processForsendelse(DistribuerTilKanal distribuerTilKanal, Exchange exchange) {
		final String konversajonId = UUID.randomUUID().toString();
		exchange.setProperty(PROPERTY_KONVERSAJON_ID, konversajonId);

		HentForsendelseResponse hentForsendelseResponse = dokdistadminService.hentForsendelse(distribuerTilKanal.getForsendelseId());
		exchange.setProperty(PROPERTY_FORSENDELSE_ID, distribuerTilKanal.getForsendelseId());
		exchange.setProperty(PROPERTY_BESTILLINGS_ID, hentForsendelseResponse.bestillingsId());

		DpoMottakerInfo dpoMottakerInfo = dokdistadminService.hentServiceRegistryMottakerInfo(hentForsendelseResponse);

		if (dpoMottakerInfo == null) {
			sendTilPrintService.sendForsendelseTilPrint(hentForsendelseResponse, exchange);
			return null;
		}

		AltinnDpoRequest.Forsendelse forsendelse = altinnRequestMapper.mapForsendelse(konversajonId, hentForsendelseResponse);

		StandardBusinessDocument standardBusinessDocument = altinnRequestMapper.getStandardBusinessDocument(forsendelse);

		AltinnDpoRequest altinnDpoRequest = AltinnDpoRequest.builder()
				.forsendelseId(distribuerTilKanal.getForsendelseId())
				.forsendelse(forsendelse)
				.businessDocument(standardBusinessDocument)
				.navDokumentpakke(mapToNavDokumentpakke(hentForsendelseResponse))
				.build();

		eformidling.send(altinnDpoRequest);

		juridiskloggService.lagreJuridisklogg(altinnDpoRequest);

		return altinnDpoRequest;
	}

	private NavDokumentpakke mapToNavDokumentpakke(HentForsendelseResponse forsendelse) {
		List<GCSForsendelseDokument> gcsForsendelseDokumenter = gStorageDokumentService.hentDokumentFromGCStorage(forsendelse);

		return NavDokumentpakke.builder()
				.navDokument(NavDokument.fromDpoMelding(
						forsendelse.forsendelseMetadataType(), new ByteArrayInputStream(forsendelse.forsendelseMetadata().getBytes())
				))
				.navDokumenter(gcsForsendelseDokumenter.stream()
						.map(dok ->
								NavDokument.fromVedlegg(dok.getDokumentObjektReferanse(), new ByteArrayInputStream(dok.getPdf())))
						.toList())
				.build();
	}
}
