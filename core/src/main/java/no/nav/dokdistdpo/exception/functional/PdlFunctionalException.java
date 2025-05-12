package no.nav.dokdistdpo.exception.functional;

import org.springframework.http.ProblemDetail;

public class PdlFunctionalException extends DokdistdpoFunctionalException {

	private final ProblemDetail problemDetail;

	public PdlFunctionalException(String message, ProblemDetail problemDetail) {
		super(message);
		this.problemDetail = problemDetail;
	}
}
