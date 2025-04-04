package no.nav.dokdistdpo.config.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("maskinporten")
public record MaskinportenProperties(@NotEmpty
									 String issuer,
									 @NotEmpty
									 String clientId,
									 @NotEmpty
									 String scopes,
									 @NotEmpty
									 String tokenEndpoint,
									 @NotEmpty
									 String clientJwk) {
}
