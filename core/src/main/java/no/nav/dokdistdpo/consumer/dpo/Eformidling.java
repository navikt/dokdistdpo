package no.nav.dokdistdpo.consumer.dpo;

import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.DownloadResponse;
import no.nav.dokdistdpo.consumer.dpo.serviceregistry.ServiceRegistryRequest;

import java.util.List;

public interface Eformidling {
	void send(AltinnDpoRequest altinnDpoRequest);

	List<DownloadResponse> hent(ServiceRegistryRequest serviceRegistryRequest);

	void bekreft(String filreferanse);
}
