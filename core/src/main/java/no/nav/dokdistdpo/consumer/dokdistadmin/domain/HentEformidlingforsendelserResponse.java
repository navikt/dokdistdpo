package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import java.util.List;


public record HentEformidlingforsendelserResponse(List<Forsendelse> forsendelser) {

	public record Forsendelse(
			String forsendelseId,
			String forsendelseStatus,
			String distribusjonKanal,
			String konversasjonId, Mottaker mottaker) {
	}
}
