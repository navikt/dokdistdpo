package no.nav.dokdistdpo.consumer.saf.journalpost;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CompactSafJournalpost(
		String journalfortAvNavn,
		String journalposttype,
		String avsenderMottakerNavn,
		LocalDateTime datoJournalfoert
) implements Journalpost {
}
