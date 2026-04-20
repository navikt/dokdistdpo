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
		Altinn2Properties altinn2,
		@Valid
		Altinn3Properties altinn3,
		@Valid
		DpoUserProperties dpo,
		@Valid
		MqGatewayProperties mqGateway,
		@Valid
		JuridiskloggConfig juridisklogg,
		@Valid
		Qdist015 qdist015) {

	public record Serviceuser(String username, String password) {
	}

	public record ServiceRegistryConfig(
			@NotBlank String url) {
	}

	public record Endpoints(AzureEndpoint dokdistadmin) {
	}

	public record AzureEndpoint(@NotBlank String url,
								@NotBlank String scope) {
	}

	public record Altinn2Properties(
			Altinn2BrokerProperties brokerserviceexternal,
			Altinn2BrokerProperties brokerserviceexternalstreamed,
			@NotBlank String serviceCode,
			@NotBlank String serviceEditionCode
	) {
	}

	public record Altinn3Properties(
			@NotBlank
			String url,
			@NotBlank
			String externalRef,
			@NotBlank
			String apiSubscriptionKey,
			boolean enabled
	) {
	}

	public record Altinn2BrokerProperties(
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
			String password,
			@NotBlank
			String clientid,
			@NotBlank
			String scope) {
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
			boolean autostartup) {
	}
}
