package no.nav.dokdistdpo.exception.functional;

public class DokdistdpoFunctionalException extends RuntimeException{

	public DokdistdpoFunctionalException(String message) {
		super(message);
	}

	public DokdistdpoFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
