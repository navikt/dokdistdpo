package no.nav.dokdistdpo.certificate;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.security.KeyStore;


@Validated
@ConfigurationProperties("virksomhetssertifikat")
public record KeyStoreProperties(
		@NotBlank String type,
		@NotBlank String alias,
		@NotBlank String password,
		@NotBlank Resource path) {

	public KeyStoreProperties {
		type = KeyStore.getDefaultType();
	}
}
