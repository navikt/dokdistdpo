package no.nav.dokdistdpo.consumer.serviceregistry;

import java.security.cert.X509Certificate;

import static no.nav.dokdistdpo.certificate.AppCertificate.convertToX509Certificate;

public record DpoMottakerInfo(String orgnummer,
							  String pemCertificate,
							  X509Certificate x509Certificate,
							  String serviceCode,
							  String serviceEditionCode) {

	public DpoMottakerInfo(String orgnummer,
						   String pemCertificate,
						   String serviceCode,
						   String serviceEditionCode) {
		this(orgnummer, pemCertificate,
				convertToX509Certificate(pemCertificate),
				serviceCode, serviceEditionCode);
	}

}
