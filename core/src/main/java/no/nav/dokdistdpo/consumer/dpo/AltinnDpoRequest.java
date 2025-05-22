package no.nav.dokdistdpo.consumer.dpo;

import lombok.Builder;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.serviceregistry.RegistryMottakerInfo;

@Builder
public record AltinnDpoRequest(
		String forsendelseId,
		Forsendelse forsendelse,
		StandardBusinessDocument businessDocument,
		RegistryMottakerInfo registryMottakerInfo,
		NavDokumentpakke navDokumentpakke) {

	@Builder
	public record Forsendelse(
			String journalpostId,
			String konversjonsId,
			String mottakerId,
			String bestillingsId,
			String forsendelseMetadata,
			String meldingType) {
	}
}
