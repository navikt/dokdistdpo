package no.nav.dokdistdpo.exception.technical;

import org.springframework.http.ProblemDetail;

public class PdlTechnicalException extends DokdistdpoTechnicalException {
	private final ProblemDetail problemDetail;
	public PdlTechnicalException(String message, ProblemDetail problemDetail) {
		super(message);
		this.problemDetail = problemDetail;
	}
}
