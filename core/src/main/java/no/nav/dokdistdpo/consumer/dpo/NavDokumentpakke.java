package no.nav.dokdistdpo.consumer.dpo;

import lombok.Builder;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType;

import java.util.List;

@Builder
public record NavDokumentpakke(
		NavDokument navDokument,
		List<NavDokument> navDokumenter) {
}
