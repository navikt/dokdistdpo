package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from;

import lombok.Builder;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.xml.BrokerServiceManifest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.DpoKvitteringMelding;

@Builder
public record AltinnDokument(
		String fileReference,
		BrokerServiceManifest manifest,
		DpoKvitteringMelding dpoKvitteringMelding) {
}
