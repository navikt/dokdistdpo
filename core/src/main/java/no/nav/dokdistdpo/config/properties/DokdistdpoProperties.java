package no.nav.dokdistdpo.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("dokdistdpo")
public record DokdistdpoProperties(
		@Valid
		Endpoints endpoints,
		@Valid
		ServiceRegistryConfig serviceRegistry) {

	public record ServiceRegistryConfig(
			@NotBlank String url) {
	}

	public record Endpoints(AzureEndpoint dokdistadmin) {
	}

	public record AzureEndpoint(@NotBlank String url,
								@NotBlank String scope) {
	}
}
