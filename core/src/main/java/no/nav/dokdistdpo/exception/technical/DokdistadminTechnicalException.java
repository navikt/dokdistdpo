package no.nav.dokdistdpo.exception.technical;

import org.springframework.http.ProblemDetail;

public class DokdistadminTechnicalException extends DokdistdpoTechnicalException {

	private ProblemDetail problemDetail;

	public DokdistadminTechnicalException(String message, ProblemDetail problemDetail) {
		super(message);
		this.problemDetail = problemDetail;
	}
}
