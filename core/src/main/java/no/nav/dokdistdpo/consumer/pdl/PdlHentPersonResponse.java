package no.nav.dokdistdpo.consumer.pdl;

import java.util.List;

public record PdlHentPersonResponse(
		PdlHentPerson data,
		List<PdlError> errors) {

	public record PdlHentPerson(
			HentPerson hentPerson
	) {}

	public record HentPerson(
			List<PersonNavn> navn,
			List<Folkeregisteridentifikator> folkeregisteridentifikator
	) {}

	public record PersonNavn(
			String fornavn,
			String mellomnavn,
			String etternavn,
			String forkortetNavn
	) {}

	public record Folkeregisteridentifikator(
			String identifikasjonsnummer,
			String type,
			String status) {}

	public record PdlError(
			String message,
			PdlErrorExtensionTo extension) {}

	public record PdlErrorExtensionTo(
			String code,
			ErrorDetails details,
			String classification
	) {}

	public record ErrorDetails(
			String type,
			String cause,
			String policy) {}

}
