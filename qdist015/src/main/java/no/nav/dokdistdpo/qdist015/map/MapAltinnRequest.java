package no.nav.dokdistdpo.qdist015.map;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dpo.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.ArkivmeldingStandardBusinessDocumentMapper;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.AvtaltmeldingStandardBusinessDocumentMapper;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.serviceregistry.RegistryMottakerInfo;
import no.nav.dokdistdpo.qdist015.DokdistDokumentFromStorage;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;

import static java.lang.Enum.valueOf;
import static no.nav.dokdistdpo.consumer.dpo.NavDokument.fromDpoMelding;
import static no.nav.dokdistdpo.consumer.dpo.NavDokument.fromVedlegg;

@Component
public class MapAltinnRequest {

	private final ArkivmeldingStandardBusinessDocumentMapper arkivmeldingSbdMapper;
	private final AvtaltmeldingStandardBusinessDocumentMapper avtaltmeldingSbdMapper;

	public MapAltinnRequest() {
		this.arkivmeldingSbdMapper = new ArkivmeldingStandardBusinessDocumentMapper();
		this.avtaltmeldingSbdMapper = new AvtaltmeldingStandardBusinessDocumentMapper();
	}

	public static AltinnDpoRequest map(List<DokdistDokumentFromStorage> dokdistDokumenter, RegistryMottakerInfo registryMottakerInfo,
									   HentForsendelseResponse hentForsendelse, String konversajonsId) {
		return AltinnDpoRequest.builder()
				.forsendelse(mapForsendelse(konversajonsId, hentForsendelse))
				.registryMottakerInfo(registryMottakerInfo)
				.navDokumentpakke(NavDokumentpakke.builder()
						.navDokumenter(dokdistDokumenter.stream()
								.map(dok ->
										fromVedlegg(dok.getDokumentObjektReferanse(), new ByteArrayInputStream(dok.getPdf())))
								.toList())
						.navDokument(fromDpoMelding(hentForsendelse.forsendelseMetadataType(),
								new ByteArrayInputStream(hentForsendelse.forsendelseMetadata().getBytes())))
						.build())
				.build();
	}

	public static AltinnDpoRequest.Forsendelse mapForsendelse(String konversjonsId, HentForsendelseResponse hentForsendelseResponse) {
		return AltinnDpoRequest.Forsendelse.builder()
				.mottakerId(hentForsendelseResponse.mottaker().mottakerId())
				.bestillingsId(hentForsendelseResponse.bestillingsId())
				.forsendelseMetadata(hentForsendelseResponse.forsendelseMetadata())
				.meldingType(hentForsendelseResponse.forsendelseMetadataType())
				.konversjonsId(konversjonsId).build();
	}

	public StandardBusinessDocument getStandardBusinessDocument(AltinnDpoRequest.Forsendelse forsendelse) {
		ForsendelseMetadataType forsendelseMetadataType = valueOf(ForsendelseMetadataType.class, forsendelse.meldingType());
		return switch (forsendelseMetadataType) {
			case DPO_ARKIVMELDING -> arkivmeldingSbdMapper.mapArkivmeldingEnvelope(forsendelse);
			case DPO_AVTALEMELDING -> avtaltmeldingSbdMapper.mapAvtaltMeldingEnvelope(forsendelse);
		};
	}
}
