package no.nav.dokdistdpo.certificate;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AppCertificateConfig {

	@Bean
	AppCertificate appCertificate(KeyStoreProperties keyStoreProperties) {
		return new AppCertificate(keyStoreProperties, loadKeyStoreCredentialsJson(keyStoreProperties.credentials()));
	}

	private static KeyStoreCredentials loadKeyStoreCredentialsJson(String credentials) {
		Path credentialsJsonPath = Paths.get(credentials);
		if (!Files.exists(credentialsJsonPath)) {
			throw new IllegalArgumentException("credentials med path=" + credentials + " finnes ikke");
		}
		try {
			JsonMapper jsonMapper = JsonMapper.builder().build();
			return jsonMapper.readValue(credentialsJsonPath.toFile(), KeyStoreCredentials.class);
		} catch (JacksonException _) {
			// Rethrower ikke exception for å ikke risikere at innhold dumpes til loggen
			throw new IllegalArgumentException("Klarte ikke lese credentials json");
		}
	}
}
