package no.nav.dokdistdpo.exception.functional;

import org.springframework.http.ProblemDetail;

public class DokdistadminFunctionalException extends DokdistdpoFunctionalException {

	private final ProblemDetail problemDetail;

	public DokdistadminFunctionalException(String message, ProblemDetail problemDetail) {
		super(message);
		this.problemDetail = problemDetail;
	}

}
