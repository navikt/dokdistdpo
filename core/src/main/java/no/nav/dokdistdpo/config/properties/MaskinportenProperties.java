package no.nav.dokdistdpo.config.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("maskinporten")
public record MaskinportenProperties(@NotEmpty
									 String issuer,
									 @NotEmpty
									 String tokenEndpoint) {
}
