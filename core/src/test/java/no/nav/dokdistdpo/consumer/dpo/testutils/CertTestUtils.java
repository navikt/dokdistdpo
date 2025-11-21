package no.nav.dokdistdpo.consumer.dpo.testutils;

import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.certificate.KeyStoreCredentials;
import no.nav.dokdistdpo.certificate.KeyStoreProperties;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public final class CertTestUtils {

	public static final String SELF_SIGNED_PKCS12 = "secrets/cert.p12";
	public static final String PKCS_12 = "PKCS12";
	public static final String SELF_SIGNED_PKCS12_ALIAS = "1";
	public static final String SELF_SIGNED_PKCS12_PASSWORD = "navdpo";

	private CertTestUtils() {
		// noop
	}

	public static AppCertificate itestVirksomhetssertifikatProperties() throws IOException {
		return new AppCertificate(
				new KeyStoreProperties("", new ClassPathResource(SELF_SIGNED_PKCS12).getFile().getAbsolutePath()),
				new KeyStoreCredentials(PKCS_12, SELF_SIGNED_PKCS12_ALIAS, SELF_SIGNED_PKCS12_PASSWORD));
	}
}
