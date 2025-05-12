package no.nav.dokdistdpo.consumer.saf;

import lombok.Builder;
import no.nav.dokdistdpo.consumer.saf.journalpost.Journalpost;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.dokdistdpo.consumer.saf.SafJournalpost.Datotype.DATO_JOURNALFOERT;
import static org.springframework.util.CollectionUtils.isEmpty;

@Builder
public record SafJournalpost(
		String journalpostId,
		String opprettetAvNavn,
		LocalDateTime datoOpprettet,
		String tittel,
		String journalfortAvNavn,
		String temanavn,
		String tema,
		String journalposttype,
		String journalfoerendeEnhet,
		Sak sak,
		Bruker bruker,
		AvsenderMottaker avsenderMottaker,
		List<RelevantDato> relevanteDatoer,
		List<DokumentInfo> dokumenter
) implements Journalpost {

	@Builder
	public record Sak(
			String arkivsaksnummer,
			LocalDateTime datoOpprettet
	) {
	}

	@Builder
	public record Bruker(
			String id,
			String type
	) {
	}

	@Builder
	public record AvsenderMottaker(
			String navn
	) {
	}

	@Builder
	public record RelevantDato(
			LocalDateTime dato,
			String datotype
	) {
	}

	public enum Datotype {
		DATO_JOURNALFOERT
	}

	@Builder
	public record DokumentInfo(
			String dokumentInfoId,
			String dokumentstatus,
			String tittel,
			String originalJournalpostId,
			List<Dokumentvariant> dokumentvarianter
	) {
	}

	@Builder
	public record Dokumentvariant(
			String variantformat,
			String filtype
	) {
		public static final String FILTYPE_PNG = "PNG";
		public static final String FILTYPE_JPEG = "JPEG";
		public static final String FILTYPE_PDF = "PDF";
		public static final String FILTYPE_TIFF = "TIFF";
		public static final String FILTYPE_XLSX = "XLSX";
	}

	public SafJournalpost withRelevantDato(List<RelevantDato> relevantDatoer) {
		return new SafJournalpost(this.journalpostId, this.opprettetAvNavn, this.datoOpprettet,
				this.tittel, this.journalfortAvNavn, temanavn, tema, this.journalposttype,
				this.journalfoerendeEnhet, this.sak, this.bruker, this.avsenderMottaker,
				singletonList(getRelevantDatoJournalfoert(relevantDatoer)), this.dokumenter);
	}

	private RelevantDato getRelevantDatoJournalfoert(List<RelevantDato> relevantDatoer) {
		return isEmpty(relevantDatoer) ? null :
				relevantDatoer.stream()
						.filter(r -> DATO_JOURNALFOERT.name().equals(r.datotype()))
						.findAny().orElse(null);
	}

	public LocalDateTime getJournalfoertDato() {
		return relevanteDatoer().stream().filter(r -> DATO_JOURNALFOERT.name().equals(r.datotype()))
				.map(RelevantDato::dato)
				.findAny().orElse(null);
	}
}
