package no.nav.dokdistdpo.certificate;

import lombok.Getter;
import no.nav.dokdistdpo.exception.functional.KeystoreProviderException;
import no.nav.dokdistdpo.exception.functional.MottakerInfoIkkeFunnetException;
import no.nav.dokdistdpo.exception.technical.CertificateConversionException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
public class AppCertificate {

	private static final String ERR_MISSING_PRIVATE_KEY_OR_PASS = "Feil ved tilgang til PrivateKey med alias \"%s\": tilgang nektet eller feil passord";
	private static final String ERR_MISSING_PRIVATE_KEY = "Ingen PrivateKey med alias \"%s\" ble funnet i KeyStore";
	private static final String ERR_MISSING_CERTIFICATE = "Ingen AppCertificate med alias \"%s\" ble funnet i KeyStore";
	private static final String ERR_GENERAL = "Uventet feil oppstod ved operasjon på KeyStore.";

	private final KeyStoreProperties properties;
	private final KeyStoreCredentials credentials;
	private final KeyStore keyStore;
	private final PrivateKey privateKey;
	private final X509Certificate x509Certificate;

	public AppCertificate(KeyStoreProperties properties, KeyStoreCredentials credentials) {
		this.properties = properties;
		this.credentials = credentials;
		try {
			this.keyStore = loadKeyStoreData();
			this.privateKey = loadPrivateKey();
			this.x509Certificate = loadX509Certificate();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private KeyStore loadKeyStoreData() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
		String type = credentials.type();
		char[] password = credentials.password().toCharArray();
		Resource path = new FileSystemResource(properties.key());

		KeyStore keyStore = KeyStore.getInstance(type);

		try (var inputStream = path.getInputStream()) {
			keyStore.load(isBase64(properties) ? Base64.getDecoder().wrap(inputStream) : inputStream, password);
		}

		return keyStore;
	}

	public PrivateKey loadPrivateKey() {
		String alias = credentials.alias();
		try {
			char[] password = credentials.password().toCharArray();

			PrivateKey key = (PrivateKey) keyStore.getKey(alias, password);
			if (key == null) {
				throw new IllegalStateException(format(ERR_MISSING_PRIVATE_KEY, alias));
			}
			return key;
		} catch (KeyStoreException | NoSuchAlgorithmException e) {
			throw new IllegalStateException(ERR_GENERAL, e);
		} catch (UnrecoverableKeyException e) {
			throw new IllegalStateException(format(ERR_MISSING_PRIVATE_KEY_OR_PASS, alias), e);
		}
	}

	public X509Certificate loadX509Certificate() {
		String alias = credentials.alias();

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

	public static X509Certificate convertToX509Certificate(final String pemCertificate) {
		validatePemCertificate(pemCertificate);
		try (InputStreamReader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(pemCertificate.getBytes()))) {
			try (PEMParser pemParser = new PEMParser(new BufferedReader(inputStreamReader))) {
				final Object certificate = pemParser.readObject();
				if (!(certificate instanceof X509CertificateHolder)) {
					throw new CertificateConversionException("PEM data inneholder ikke et X.509 sertifikat");
				}
				return new JcaX509CertificateConverter()
						.setProvider("BC")
						.getCertificate((X509CertificateHolder) certificate);

			}
		} catch (IOException e) {
			throw new CertificateConversionException("Klarte ikke konvertere PEM til X.509 sertifikat", e);
		} catch (CertificateException e) {
			throw new CertificateConversionException("Klarte ikke lese PEM data.", e);
		}
	}

	private static void validatePemCertificate(final String pemCertificate) {
		if (isBlank(pemCertificate)) {
			throw new MottakerInfoIkkeFunnetException("Fant ikke PEM sertifikat.");
		}
	}

	private static boolean isBase64(KeyStoreProperties properties) {
		return properties.key().endsWith(".b64");
	}
}
