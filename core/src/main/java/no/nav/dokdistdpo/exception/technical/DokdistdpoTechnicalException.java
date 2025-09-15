package no.nav.dokdistdpo.exception.technical;

public class DokdistdpoTechnicalException extends RuntimeException {
	public DokdistdpoTechnicalException(String message) {
		super(message);
	}

	public DokdistdpoTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
