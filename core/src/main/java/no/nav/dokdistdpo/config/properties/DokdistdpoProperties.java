package no.nav.dokdistdpo.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("dokdistdpo")
public record DokdistdpoProperties(
		@Valid
		Serviceuser serviceuser,
		@Valid
		Endpoints endpoints,
		@Valid
		ServiceRegistryConfig serviceRegistry,
		@Valid
		AltinnProperties altinn,
		@Valid
		DpoUserProperties dpo) {

	public record Serviceuser(String username, String password) {}

	public record ServiceRegistryConfig(
			@NotBlank String url) {
	}

	public record Endpoints(AzureEndpoint dokdistadmin) {
	}

	public record AzureEndpoint(@NotBlank String url,
								@NotBlank String scope) {
	}

	public record AltinnProperties(
			AltinnBrokerProperties brokerserviceexternal,
			AltinnBrokerProperties brokerserviceexternalstreamed
	) {
	}

	public record AltinnBrokerProperties(
			@NotBlank
			String endpointurl,
			@Min(1)
			int readtimeoutms,
			@Min(1)
			int connecttimeoutms
	) {
	}

	public record DpoUserProperties(
			@NotBlank
			String username,
			@NotBlank
			String password) {
	}
}
