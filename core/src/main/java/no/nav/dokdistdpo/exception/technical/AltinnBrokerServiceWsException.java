package no.nav.dokdistdpo.exception.technical;

import no.nav.dokdistdpo.consumer.dpo.altinn.to.AltinnReason;

public class AltinnBrokerServiceWsException extends DokdistdpoTechnicalException {

	public AltinnBrokerServiceWsException(String message, AltinnReason altinnReson, Throwable cause) {
		super(message + " " + altinnReson, cause);
	}
}
