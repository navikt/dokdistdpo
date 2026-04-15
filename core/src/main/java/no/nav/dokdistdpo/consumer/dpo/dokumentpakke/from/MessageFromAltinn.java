package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from;

import java.io.InputStream;

public record MessageFromAltinn(
		String filreferanse,
		InputStream inputStream) {
}
