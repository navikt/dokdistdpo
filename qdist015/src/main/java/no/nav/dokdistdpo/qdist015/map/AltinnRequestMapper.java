package no.nav.dokdistdpo.qdist015.map;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest.Forsendelse;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.ArkivmeldingStandardBusinessDocumentMapper;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.AvtaltStandardBusinessDocumentMapper;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import org.springframework.stereotype.Component;

import static java.lang.Enum.valueOf;

@Component
public class AltinnRequestMapper {

	private final ArkivmeldingStandardBusinessDocumentMapper arkivmeldingSbdMapper;
	private final AvtaltStandardBusinessDocumentMapper avtaltmeldingSbdMapper;

	public AltinnRequestMapper() {
		this.arkivmeldingSbdMapper = new ArkivmeldingStandardBusinessDocumentMapper();
		this.avtaltmeldingSbdMapper = new AvtaltStandardBusinessDocumentMapper();
	}

	public Forsendelse mapForsendelse(String konversasjonId, HentForsendelseResponse hentForsendelseResponse) {
		return Forsendelse.builder()
				.bestillingsId(hentForsendelseResponse.bestillingsId())
				.konversjonsId(konversasjonId)
				.journalpostId(hentForsendelseResponse.arkivInformasjon().arkivId())
				.mottakerId(hentForsendelseResponse.mottaker().mottakerId())
				.organisasjonsnavn(hentForsendelseResponse.mottaker().mottakerNavn())
				.forsendelseMetadata(hentForsendelseResponse.forsendelseMetadata() == null ? null : new String(hentForsendelseResponse.forsendelseMetadata()))
				.meldingType(hentForsendelseResponse.forsendelseMetadataType())
				.build();
	}

	public StandardBusinessDocument getStandardBusinessDocument(Forsendelse forsendelse) {
		ForsendelseMetadataType forsendelseMetadataType = valueOf(ForsendelseMetadataType.class, forsendelse.meldingType());
		return switch (forsendelseMetadataType) {
			case DPO_ARKIVMELDING -> arkivmeldingSbdMapper.mapArkivmeldingEnvelope(forsendelse);
			case DPO_AVTALEMELDING -> avtaltmeldingSbdMapper.mapAvtaltMeldingEnvelope(forsendelse);
		};
	}
}
