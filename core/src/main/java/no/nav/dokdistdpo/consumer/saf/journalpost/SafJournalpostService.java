package no.nav.dokdistdpo.consumer.saf.journalpost;

public interface SafJournalpostService<T extends Journalpost> {
	T hentJournalpost(String journalpostId);
}
