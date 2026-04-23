package no.nav.dokdistdpo.consumer.dpo.altinn2;

import java.io.InputStream;

public record MessageFromAltinn2(
		String filreferanse,
		InputStream inputStream) {
}
