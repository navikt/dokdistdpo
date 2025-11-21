package no.nav.dokdistdpo.certificate;

import jakarta.validation.constraints.NotNull;
import no.nav.dok.validators.Exists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("nav.virksomhetssertifikat")
public record KeyStoreProperties(
		@NotNull @Exists String credentials,
		@NotNull @Exists String key) {
}
