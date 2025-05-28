package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record OpprettForsendelseRequest(
		String bestillingsId,
		String distribusjonsKanal,
		String bestillendeFagsystem,
		String tema,
		String forsendelseTittel,
		String dokumentProdApp,
		Mottaker mottaker,
		List<Dokument> dokumenter,
		String originalDistribusjonId,
		String distribusjonstype,
		String distribusjonstidspunkt,
		ArkivInformasjon arkivInformasjon,
		Postadresse postadresse) {
}
