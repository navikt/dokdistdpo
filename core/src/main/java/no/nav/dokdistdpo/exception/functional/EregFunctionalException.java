package no.nav.dokdistdpo.exception.functional;

import org.springframework.http.ProblemDetail;

public class EregFunctionalException extends DokdistdpoFunctionalException {

	private final ProblemDetail problemDetail;

	public EregFunctionalException(String message, ProblemDetail problemDetail) {
		super(message);
		this.problemDetail = problemDetail;
	}
}
