package no.nav.dokdistdpo.qdist015.saf;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.saf.SafJournalpost;
import no.nav.dokdistdpo.consumer.saf.graphql.SafGraphQLConsumer;
import no.nav.dokdistdpo.consumer.saf.graphql.SafGraphQLRequest;
import no.nav.dokdistdpo.consumer.saf.journalpost.CompactSafJournalpost;
import no.nav.dokdistdpo.consumer.saf.journalpost.SafJournalpostService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static no.nav.dokdistdpo.consumer.saf.SafJournalpost.Datotype.DATO_JOURNALFOERT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component("CompactSafJournalpostQueryService")
public class CompactSafJournalpostQueryService implements SafJournalpostService<CompactSafJournalpost> {

	private static final String UKJENT_NAVN = "UKJENT";
	private final SafGraphQLConsumer safGraphQlConsumer;

	public CompactSafJournalpostQueryService(SafGraphQLConsumer safGraphQlConsumer) {
		this.safGraphQlConsumer = safGraphQlConsumer;
	}

	@Override
	public CompactSafJournalpost hentJournalpost(String journalpostId) {
		SafGraphQLRequest safGraphQLRequest = SafGraphQLRequest.builder()
				.operationName("safJournalpost")
				.query(JOURNALPOST_QUERY)
				.variables(Collections.singletonMap("queryJournalpostId", journalpostId))
				.build();
		SafJournalpost safJournalpost = safGraphQlConsumer.hentJournalpost(safGraphQLRequest);

		return CompactSafJournalpost.builder()
				.journalfortAvNavn(safJournalpost.journalfortAvNavn())
				.journalposttype(safJournalpost.journalposttype())
				.avsenderMottakerNavn(getAvsenderMottakerNavn(safJournalpost))
				.datoJournalfoert(getDatoJournalfoert(safJournalpost.relevanteDatoer()))
				.build();
	}

	private String getAvsenderMottakerNavn(SafJournalpost safJournalpost) {
		if (safJournalpost.avsenderMottaker() == null || isBlank(safJournalpost.avsenderMottaker().navn())) {
			log.warn("AvsenderMottakerNavn er null eller tomt i response fra SAF på journalpostId={}", safJournalpost.journalpostId());
			return UKJENT_NAVN;
		}
		return safJournalpost.avsenderMottaker().navn();
	}

	private LocalDateTime getDatoJournalfoert(List<SafJournalpost.RelevantDato> relevantDatoer) {
		return isEmpty(relevantDatoer) ? null :
				relevantDatoer.stream()
						.filter(relevantDato -> DATO_JOURNALFOERT.name().equals(relevantDato.datotype()))
						.map(SafJournalpost.RelevantDato::dato)
						.findAny()
						.orElse(null);

	}

	private static final String JOURNALPOST_QUERY = """
			query safJournalpost($queryJournalpostId: String!) {
			  safJournalpost(journalpostId: $queryJournalpostId) {
			    journalfortAvNavn
			    avsenderMottaker {
			      navn
			    }
			    journalposttype
			    relevanteDatoer {
			      dato
			      datotype
			    }
			  }
			}
			""";
}
