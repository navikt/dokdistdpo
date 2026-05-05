package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from;

import java.io.InputStream;

public record Altinn3InnsendtFilkvittering(
		String fileReferenceId,
		InputStream content) {
}
