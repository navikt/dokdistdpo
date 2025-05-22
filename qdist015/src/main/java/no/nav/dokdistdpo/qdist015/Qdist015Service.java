package no.nav.dokdistdpo.qdist015;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dokdistadmin.DokdistAdminConsumer;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dpo.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.AltinnEformidling;
import no.nav.dokdistdpo.consumer.dpo.Eformidling;
import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.gcloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpo.consumer.serviceregistry.DpoMottakerInfoService;
import no.nav.dokdistdpo.consumer.serviceregistry.RegistryMottakerInfo;
import no.nav.dokdistdpo.consumer.serviceregistry.ServiceRegistryRequest;
import no.nav.dokdistdpo.exception.functional.KunneIkkeDeserialisereBucketJsonPayloadFunctionalException;
import no.nav.dokdistdpo.qdist015.dokdistforsendelse.HentForsendelseValidator;
import no.nav.dokdistdpo.qdist015.dokdistforsendelse.SendTilPrintService;
import no.nav.dokdistdpo.qdist015.map.MapAltinnRequest;
import no.nav.dokdistdpo.utils.JsonSerializer;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static java.lang.Enum.valueOf;
import static java.lang.String.format;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SCOPE_CONVERSATION_ID_ARKIVMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dpo.NavDokument.fromDpoMelding;
import static no.nav.dokdistdpo.consumer.dpo.NavDokument.fromVedlegg;

@Slf4j
@Component
public class Qdist015Service {

	public static final String PROPERTY_CONVERSATION_ID = "konversajonsId";
	public static final String PROPERTY_BESTILLINGS_ID = "bestillingsId";
	public static final String PROPERTY_FORSENDELSE_ID = "forsendelseId";

	private final DokdistAdminConsumer dokdistAdminConsumer;
	private final HentForsendelseValidator hentForsendelseValidator;
	private final DpoMottakerInfoService dpoMottakerInfoService;
	private final EncryptedBucketStorage encryptedBucketStorage;
	private final MapAltinnRequest mapAltinnRequest;
	private final Eformidling eformidling;
	private final LagreJuridiskloggService juridiskloggService;
	private final SendTilPrintService sendTilPrintService;

	public Qdist015Service(DokdistAdminConsumer dokdistAdminConsumer,
						   DpoMottakerInfoService dpoMottakerInfoService,
						   EncryptedBucketStorage encryptedBucketStorage,
						   AltinnEformidling altinnEformidling,
						   LagreJuridiskloggService juridiskloggService,
						   SendTilPrintService sendTilPrintService) {
		this.dokdistAdminConsumer = dokdistAdminConsumer;
		this.hentForsendelseValidator = new HentForsendelseValidator();
		this.dpoMottakerInfoService = dpoMottakerInfoService;
		this.encryptedBucketStorage = encryptedBucketStorage;
		this.mapAltinnRequest = new MapAltinnRequest();
		this.eformidling = altinnEformidling;
		this.juridiskloggService = juridiskloggService;
		this.sendTilPrintService = sendTilPrintService;
	}

	@Handler
	public AltinnDpoRequest processForsendelse(DistribuerTilKanal distribuerTilKanal, Exchange exchange) {
		final String conversationId = UUID.randomUUID().toString();
		exchange.setProperty(PROPERTY_CONVERSATION_ID, conversationId);

		String forsendelseId = distribuerTilKanal.getForsendelseId();
		HentForsendelseResponse hentForsendelseResponse = dokdistAdminConsumer.hentForsendelse(forsendelseId);

		hentForsendelseValidator.assertDokdistForsendelse(hentForsendelseResponse);
		exchange.setProperty(PROPERTY_FORSENDELSE_ID, forsendelseId);

		RegistryMottakerInfo registryMottakerInfo = dpoMottakerInfoService.hentMottakerInfo(getServiceRegistryRequest(hentForsendelseResponse));

		if (registryMottakerInfo == null) {
			sendTilPrintService.sendTilPrint(hentForsendelseResponse, exchange);
			return null;
		}

		List<DokdistDokumentFromStorage> dokumenterFromStorage = getDokumentFromStorage(hentForsendelseResponse);

		AltinnDpoRequest.Forsendelse forsendelse = MapAltinnRequest.mapForsendelse(conversationId, hentForsendelseResponse);

		StandardBusinessDocument standardBusinessDocument = mapAltinnRequest.getStandardBusinessDocument(forsendelse);
		exchange.setProperty(PROPERTY_BESTILLINGS_ID, hentForsendelseResponse.arkivInformasjon().arkivId());

		AltinnDpoRequest altinnDpoRequest = AltinnDpoRequest.builder()
				.forsendelseId(forsendelseId)
				.forsendelse(forsendelse)
				.registryMottakerInfo(registryMottakerInfo)
				.businessDocument(standardBusinessDocument)
				.navDokumentpakke(NavDokumentpakke.builder()
						.navDokumenter(dokumenterFromStorage.stream()
								.map(dok ->
										fromVedlegg(dok.getDokumentObjektReferanse(), new ByteArrayInputStream(dok.getPdf())))
								.toList())
						.navDokument(fromDpoMelding(hentForsendelseResponse.forsendelseMetadataType(),
								new ByteArrayInputStream(hentForsendelseResponse.forsendelseMetadata().getBytes())))
						.build())
				.build();

		eformidling.send(altinnDpoRequest);
		juridiskloggService.lagreJuridisklogg(altinnDpoRequest);

		return altinnDpoRequest;
	}

	private List<DokdistDokumentFromStorage> getDokumentFromStorage(HentForsendelseResponse response) {
		return response.dokumenter().stream()
				.map(dokument -> {
					String jsonPayload = encryptedBucketStorage.downloadObject(dokument.dokumentObjektReferanse(), response.bestillingsId());
					DokdistDokumentFromStorage dokumentFromStorage = deserializeJsonPayloadToDokument(jsonPayload, dokument.dokumentObjektReferanse());
					dokumentFromStorage.setDokumentInfoId(dokumentFromStorage.getDokumentInfoId());
					return dokumentFromStorage;
				}).toList();
	}

	private ServiceRegistryRequest getServiceRegistryRequest(HentForsendelseResponse hentForsendelseResponse) {
		return ServiceRegistryRequest.builder()
				.mottakerId(hentForsendelseResponse.mottaker().mottakerId())
				.processIdentifier(getProcessIdentifier(hentForsendelseResponse.forsendelseMetadataType()))
				.build();
	}

	private String getProcessIdentifier(String metadataType) {
		ForsendelseMetadataType forsendelseMetadataType = valueOf(ForsendelseMetadataType.class, metadataType);
		return switch (forsendelseMetadataType) {
			case DPO_ARKIVMELDING -> SCOPE_CONVERSATION_ID_ARKIVMELDING_PROCESS_IDENTIFIER;
			case DPO_AVTALEMELDING -> SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER;
		};
	}

	private DokdistDokumentFromStorage deserializeJsonPayloadToDokument(String jsonPayload, String objektReferanse) {
		try {
			DokdistDokumentFromStorage dokumentFromStorage = JsonSerializer.deserialize(jsonPayload, DokdistDokumentFromStorage.class);
			dokumentFromStorage.setDokumentObjektReferanse(objektReferanse);
			return dokumentFromStorage;
		} catch (IllegalStateException e) {
			throw new KunneIkkeDeserialisereBucketJsonPayloadFunctionalException(format("Feil ved deserialisering av JSON-payload for dokument med dokumentobjektreferanse=%s. " +
					"Sørg for at payloaden er gyldig JSON format!", objektReferanse));
		}
	}
}
