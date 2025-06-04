package no.nav.dokdistdpo.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
		DpoUserProperties dpo,
		@Valid
		MqGatewayProperties mqGateway,
		@Valid
		JuridiskloggConfig juridisklogg,
		@Valid
		Qdist015 qdist015) {

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
			AltinnBrokerProperties brokerserviceexternalstreamed) {
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

	public record MqGatewayProperties(
			@NotBlank String hostname,
			@NotBlank String managerName,
			@Positive int port,
			@NotBlank String channelName) {
	}

	public record JuridiskloggConfig(
			@NotBlank String url) {
	}

	public record Qdist015(
			boolean autostartup) {}
}
