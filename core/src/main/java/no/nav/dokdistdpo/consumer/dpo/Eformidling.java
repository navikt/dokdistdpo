package no.nav.dokdistdpo.consumer.dpo;

import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.DownloadResponse;

import java.util.List;

public interface Eformidling {
	void send(AltinnDpoRequest altinnDpoRequest);

	List<DownloadResponse> hent();

	void bekreft(String filreferanse);
}
