package no.nav.dokdistdpo.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("nais")
public record NaisTexasProperties(
		@NotBlank
		String tokenEndpoint) {
}
