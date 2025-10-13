package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from;

import java.io.InputStream;

public record MessageFromAltinn(
		String filreferanse,
		InputStream inputStream) {
}
