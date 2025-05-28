package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

@Builder
public record Dokument(
		String tilknyttetSom,
		String dokumentObjektReferanse,
		Integer rekkefolge,
		String arkivDokumentInfoId,
		String dokumenttypeId) {
}
