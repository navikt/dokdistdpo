package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from;

import lombok.Builder;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.DpoKvitteringMelding;

@Builder
public record AltinnDokument(
		String fileReference,
		BrokerServiceManifest manifest,
		DpoKvitteringMelding dpoKvitteringMelding) {
}
