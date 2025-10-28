package no.nav.dokdistdpo.qdist015;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dpo.AltinnEformidlingClient;
import no.nav.dokdistdpo.consumer.dpo.NavDokument;
import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.dpo.serviceregistry.DpoMottakerInfo;
import no.nav.dokdistdpo.qdist015.dokdistforsendelse.DokdistadminService;
import no.nav.dokdistdpo.qdist015.dokdistforsendelse.OppdaterForsendelseService;
import no.nav.dokdistdpo.qdist015.dokdistforsendelse.SendTilPrintService;
import no.nav.dokdistdpo.qdist015.map.AltinnRequestMapper;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_FORSENDELSE_ID;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_KONVERSASJON_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class Qdist015Service {

	private final GStorageDokumentService gStorageDokumentService;
	private final SendTilPrintService sendTilPrintService;
	private final AltinnRequestMapper altinnRequestMapper;
	private final DokdistadminService dokdistadminService;
	private final AltinnEformidlingClient eformidling;
	private final OppdaterForsendelseService oppdaterForsendelseService;
	private final LagreJuridiskloggService juridiskloggService;

	public Qdist015Service(GStorageDokumentService gStorageDokumentService,
						   SendTilPrintService sendTilPrintService,
						   AltinnRequestMapper altinnRequestMapper,
						   DokdistadminService dokdistadminService,
						   AltinnEformidlingClient eformidling,
						   LagreJuridiskloggService juridiskloggService,
						   OppdaterForsendelseService oppdaterForsendelseService) {
		this.gStorageDokumentService = gStorageDokumentService;
		this.sendTilPrintService = sendTilPrintService;
		this.altinnRequestMapper = altinnRequestMapper;
		this.dokdistadminService = dokdistadminService;
		this.eformidling = eformidling;
		this.juridiskloggService = juridiskloggService;
		this.oppdaterForsendelseService = oppdaterForsendelseService;
	}

	@Handler
	public void processForsendelse(DistribuerTilKanal distribuerTilKanal, Exchange exchange) {
		final Long forsendelseId = Long.valueOf(distribuerTilKanal.getForsendelseId());
		exchange.setProperty(PROPERTY_FORSENDELSE_ID, forsendelseId);

		HentForsendelseResponse hentForsendelseResponse = dokdistadminService.hentForsendelse(forsendelseId);
		final String konversajonId = getKonversasjonsId(hentForsendelseResponse);

		exchange.setProperty(PROPERTY_KONVERSASJON_ID, konversajonId);
		exchange.setProperty(PROPERTY_BESTILLINGS_ID, hentForsendelseResponse.bestillingsId());

		DpoMottakerInfo dpoMottakerInfo = dokdistadminService.hentServiceRegistryMottakerInfo(hentForsendelseResponse);
		AltinnDpoRequest.Forsendelse forsendelse = altinnRequestMapper.mapForsendelse(konversajonId, hentForsendelseResponse);

		if (dpoMottakerInfo == null) {
			sendTilPrintService.sendForsendelseTilPrint(hentForsendelseResponse, exchange);
			return;
		}

		StandardBusinessDocument standardBusinessDocument = altinnRequestMapper.getStandardBusinessDocument(forsendelse);

		AltinnDpoRequest altinnDpoRequest = AltinnDpoRequest.builder()
				.forsendelseId(distribuerTilKanal.getForsendelseId())
				.forsendelse(forsendelse)
				.businessDocument(standardBusinessDocument)
				.dpoMottakerInfo(dpoMottakerInfo)
				.navDokumentpakke(mapToNavDokumentpakke(hentForsendelseResponse))
				.build();

		eformidling.send(altinnDpoRequest);

		oppdaterForsendelseService.oppdaterForsendelse(forsendelseId);
		juridiskloggService.lagreJuridisklogg(altinnDpoRequest);
	}

	private NavDokumentpakke mapToNavDokumentpakke(HentForsendelseResponse forsendelse) {
		List<GCSForsendelseDokument> gcsForsendelseDokumenter = gStorageDokumentService.hentDokumentFromGCStorage(forsendelse);

		return NavDokumentpakke.builder()
				.navDokument(NavDokument.fromDpoMelding(
						forsendelse.forsendelseMetadataType(), new ByteArrayInputStream(forsendelse.forsendelseMetadata())
				))
				.navDokumenter(gcsForsendelseDokumenter.stream()
						.map(dok ->
								NavDokument.fromVedlegg(dok.getDokumentObjektReferanse(), new ByteArrayInputStream(dok.getPdf())))
						.toList())
				.build();
	}

	private String getKonversasjonsId(HentForsendelseResponse hentForsendelseResponse) {
		return isBlank(hentForsendelseResponse.konversasjonId()) ? dokdistadminService.generereOgOppdaterKonversasjonsId(hentForsendelseResponse)
				: hentForsendelseResponse.konversasjonId();
	}
}
