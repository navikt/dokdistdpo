package no.nav.dokdistdpo.consumer.serviceregistry;

import no.nav.dokdistdpo.exception.functional.MottakerInfoIkkeFunnetException;
import no.nav.dokdistdpo.exception.technical.CertificateConversionException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.apache.commons.lang3.StringUtils.isBlank;

public record MottakerInfo(String orgnummer,
						   String pemCertificate,
						   X509Certificate x509Certificate,
						   String serviceCode,
						   String serviceEditionCode) {

	public MottakerInfo(String orgnummer,
						String pemCertificate,
						String serviceCode,
						String serviceEditionCode) {
		this(orgnummer, pemCertificate,
				CertificateUtils.convertToX509Certificate(pemCertificate),
				serviceCode, serviceEditionCode);
	}

	static class CertificateUtils {
		private static X509Certificate convertToX509Certificate(final String pemCertificate) {
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
}
