package no.nav.dokdistdpo.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("nais")
public record NaisTexasProperties(
		@NotBlank
		String tokenEndpoint) {
}
