package no.nav.dokdistdpo.consumer.saf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

public record SafJournalpostResponse(DataJournalpost data, List<Error> error) implements Serializable {

	public record DataJournalpost(
			SafJournalpost safJournalpost) {
	}

	public SafJournalpost getJournalpost() {
		return data == null ? null : data.safJournalpost();
	}


	@JsonIgnoreProperties({"locations", "path"})
	public record Error(
			String message,
			Extension extensions) {
	}

	public record Extension(
			String code,
			String classification) {
	}
}
