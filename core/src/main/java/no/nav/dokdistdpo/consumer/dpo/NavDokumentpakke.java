package no.nav.dokdistdpo.consumer.dpo;

import lombok.Builder;

import java.util.List;

@Builder
public record NavDokumentpakke(
		NavDokument navDokument,
		List<NavDokument> navDokumenter) {
}
