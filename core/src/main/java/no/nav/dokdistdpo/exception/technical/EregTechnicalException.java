package no.nav.dokdistdpo.exception.technical;

import org.springframework.http.ProblemDetail;

public class EregTechnicalException extends DokdistdpoTechnicalException {
	private final ProblemDetail problemDetail;

	public EregTechnicalException(String message, ProblemDetail problemDetail) {
		super(message);
		this.problemDetail = problemDetail;
	}
}
