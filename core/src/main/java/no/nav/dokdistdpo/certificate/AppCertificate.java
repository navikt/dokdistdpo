package no.nav.dokdistdpo.certificate;

import no.nav.dokdistdpo.exception.functional.KeystoreProviderException;
import org.springframework.stereotype.Component;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.X509Certificate;

import static java.lang.String.format;

@Component
public class AppCertificate {

	private static final String ERR_MISSING_PRIVATE_KEY_OR_PASS = "Problem accessing PrivateKey with alias \"%s\" inadequate access or Password is wrong";
	private static final String ERR_MISSING_PRIVATE_KEY = "No PrivateKey with alias \"%s\" found in the KeyStore";
	private static final String ERR_MISSING_CERTIFICATE = "No AppCertificate with alias \"%s\" found in the KeyStore";
	private static final String ERR_GENERAL = "Unexpected problem occurred when operating KeyStore";

	private final KeyStoreProperties properties;
	private final KeyStore keyStore;

	public AppCertificate(KeyStoreProperties properties) {
		this.properties = properties;
		try {
			this.keyStore = KeystoreProvider.loadKeyStoreData(properties);
		} catch (KeystoreProviderException e) {
			throw new IllegalStateException(e);
		}
	}

	public PrivateKey loadPrivateKey() {
		String alias = properties.alias();
		try {
			char[] password = properties.password().toCharArray();

			PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
			if (privateKey == null) {
				throw new IllegalStateException(format(ERR_MISSING_PRIVATE_KEY, alias));
			}
			return privateKey;
		} catch (KeyStoreException | NoSuchAlgorithmException e) {
			throw new IllegalStateException(ERR_GENERAL, e);
		} catch (UnrecoverableEntryException e) {
			throw new IllegalStateException(format(ERR_MISSING_PRIVATE_KEY_OR_PASS, alias), e);
		}
	}

	public X509Certificate getX509Certificate() {
		String alias = properties.alias();

		try {
			X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
			if (certificate == null) {
				throw new IllegalStateException(format(ERR_MISSING_CERTIFICATE, alias));
			}
			return certificate;
		} catch (KeyStoreException e) {
			throw new IllegalStateException(ERR_GENERAL, e);
		}
	}
}
