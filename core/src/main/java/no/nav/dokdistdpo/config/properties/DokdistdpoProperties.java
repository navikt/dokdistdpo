package no.nav.dokdistdpo.config.properties;

import jakarta.validation.Valid;
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
		MqGatewayProperties mqGateway) {

	public record Serviceuser(
			@NotBlank String username,
			@NotBlank String password) {}

	public record ServiceRegistryConfig(
			@NotBlank String url) {
	}

	public record Endpoints(
			AzureEndpoint dokdistadmin,
			AzureEndpoint pdl,
			AzureEndpoint saf,
			AzureEndpoint ereg) {
	}

	public record AzureEndpoint(@NotBlank String url,
								@NotBlank String scope) {
	}

	public record MqGatewayProperties(
			@NotBlank String hostname,
			@NotBlank String managerName,
			@Positive int port,
			@Valid String channelName) {
	}
}
