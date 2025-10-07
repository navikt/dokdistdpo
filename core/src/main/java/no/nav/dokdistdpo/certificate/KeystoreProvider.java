package no.nav.dokdistdpo.certificate;

import no.nav.dokdistdpo.exception.functional.KeystoreProviderException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Objects;

public class KeystoreProvider {

	public static KeyStore loadKeyStoreData(KeyStoreProperties properties) throws KeystoreProviderException {
		try {
			String type = properties.type();
			char[] password = properties.password().toCharArray();
			Resource path = new FileSystemResource(properties.key());

			KeyStore keyStore = KeyStore.getInstance(type);

			try (var inputStream = path.getInputStream()) {
				keyStore.load(isBase64Empty(properties) ? Base64.getDecoder().wrap(inputStream) : inputStream, password);
			}

			return keyStore;
		} catch (KeyStoreException e) {
			throw new KeystoreProviderException("Unable to load KeyStore", e);
		} catch (IOException e) {
			throw new KeystoreProviderException("Could not open keystore file", e);
		} catch (CertificateException | NoSuchAlgorithmException e) {
			throw new KeystoreProviderException("Failed to load keystore", e);
		}
	}

	private static boolean isBase64Empty(KeyStoreProperties properties) {
		return Objects.requireNonNull(properties.key()).endsWith(".b64");
	}

	private KeystoreProvider() {
	}
}
