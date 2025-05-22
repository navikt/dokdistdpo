package no.nav.dokdistdpo.azure;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("azure")
public record AzureProperties(
		@NotBlank
		String appClientId,
		@NotBlank
		String appClientSecret,
		@NotBlank
		String openidConfigTokenEndpoint) {
}
