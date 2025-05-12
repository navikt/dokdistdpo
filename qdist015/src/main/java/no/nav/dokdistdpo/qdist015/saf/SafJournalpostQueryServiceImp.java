package no.nav.dokdistdpo.qdist015.saf;

import no.nav.dokdistdpo.consumer.saf.SafJournalpost;
import no.nav.dokdistdpo.consumer.saf.graphql.SafGraphQLConsumer;
import no.nav.dokdistdpo.consumer.saf.graphql.SafGraphQLRequest;
import no.nav.dokdistdpo.consumer.saf.journalpost.SafJournalpostService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("SafJournalpostQueryServiceImp")
public class SafJournalpostQueryServiceImp implements SafJournalpostService<SafJournalpost> {

	private final SafGraphQLConsumer safGraphQLConsumer;
	private final SafJournalpostValidator safJournalpostValidator;

	public SafJournalpostQueryServiceImp(SafGraphQLConsumer safGraphQLConsumer,
										 SafJournalpostValidator safJournalpostValidator) {
		this.safGraphQLConsumer = safGraphQLConsumer;
		this.safJournalpostValidator = safJournalpostValidator;
	}

	@Override
	public SafJournalpost hentJournalpost(String journalpostId) {
		var safGraphQLRequest = SafGraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(Map.of("queryJournalpostId", journalpostId))
				.build();

		SafJournalpost safJournalpost = safGraphQLConsumer.hentJournalpost(safGraphQLRequest);

		safJournalpostValidator.asssertJournalpost(safJournalpost, journalpostId);

		return safJournalpost.withRelevantDato(safJournalpost.relevanteDatoer());
	}

	private static final String JOURNALPOST_QUERY = """
			query journalpost($queryJournalpostId: String!) {
			  journalpost(journalpostId: $queryJournalpostId) {
			    journalpostId
			    sak {
			      arkivsaksnummer
			      datoOpprettet
			    }
			    opprettetAvNavn
			    journalposttype
			    bruker {
			      id
			      type
			    }
			    datoOpprettet
			    tittel
			    journalfortAvNavn
			    temanavn
			    tema
			    journalfoerendeEnhet
			    relevanteDatoer {
			      dato
			      datotype
			    }
			    dokumenter {
			      dokumentInfoId
			      dokumentstatus
			      tittel
			      originalJournalpostId
			      dokumentvarianter {
			        variantformat
			        filtype
			      }
			    }
			  }
			}
			""";
}
