package no.nav.dokdistdpo.certificate;

import lombok.Getter;
import no.nav.dokdistdpo.exception.functional.KeystoreProviderException;
import no.nav.dokdistdpo.exception.functional.MottakerInfoIkkeFunnetException;
import no.nav.dokdistdpo.exception.technical.CertificateConversionException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
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

	public static X509Certificate convertToX509Certificate(final String pemCertificate) {
		validatePemCertificate(pemCertificate);
		try (InputStreamReader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(pemCertificate.getBytes()))) {
			PEMParser pemParser = new PEMParser(new BufferedReader(inputStreamReader));
			final Object certificate = pemParser.readObject();
			if (!(certificate instanceof X509Certificate)) {
				throw new CertificateConversionException("PEM data inneholder ikke et X.509 sertifikat");
			}
			return new JcaX509CertificateConverter()
					.setProvider("BC")
					.getCertificate((X509CertificateHolder) certificate);

		} catch (IOException e) {
			throw new CertificateConversionException("Klarter ikke konvertere PEM til X.509 sertifikat", e);
		} catch (CertificateException e) {
			throw new CertificateConversionException("Klarte ikke lese PEM data.", e);
		}
	}

	private static void validatePemCertificate(final String pemCertificate) {
		if (isBlank(pemCertificate)) {
			throw new MottakerInfoIkkeFunnetException("Fant ikke PEM sertifikat.");
		}
	}
}
