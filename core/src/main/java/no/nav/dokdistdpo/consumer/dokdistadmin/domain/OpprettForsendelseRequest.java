package no.nav.dokdistdpo.consumer.dokdistadmin.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record OpprettForsendelseRequest(
		String bestillingsId,
		String distribusjonsKanal,
		String distribusjonstype,
		String distribusjonstidspunkt,
		String bestillendeFagsystem,
		String tema,
		String forsendelseTittel,
		String batchId,
		String dokumentProdApp,
		String originalDistribusjonId,
		Mottaker mottaker,
		ArkivInformasjon arkivInformasjon,
		Postadresse postadresse,
		List<Dokument> dokumenter

) {

}
