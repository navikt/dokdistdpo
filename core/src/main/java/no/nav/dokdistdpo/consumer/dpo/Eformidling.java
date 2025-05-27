package no.nav.dokdistdpo.consumer.dpo;

import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;

public interface Eformidling {
	void send(AltinnDpoRequest altinnDpoRequest);
}
