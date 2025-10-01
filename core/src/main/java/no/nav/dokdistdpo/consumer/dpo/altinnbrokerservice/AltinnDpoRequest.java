package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice;

import lombok.Builder;
import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.dpo.serviceregistry.DpoMottakerInfo;

@Builder
public record AltinnDpoRequest(
		String forsendelseId,
		Forsendelse forsendelse,
		StandardBusinessDocument businessDocument,
		DpoMottakerInfo dpoMottakerInfo,
		NavDokumentpakke navDokumentpakke) {

	@Builder
	public record Forsendelse(
			String journalpostId,
			String konversjonsId,
			String mottakerId,
			String organisasjonsnavn,
			String bestillingsId,
			String forsendelseMetadata,
			String meldingType) {
	}
}
