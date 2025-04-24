package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.asice;

import no.nav.dokdistdpo.exception.functional.DokumentpakkingException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.RSAESOAEPparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Objects;

import static java.security.Security.addProvider;
import static java.security.Security.getProvider;
import static org.bouncycastle.asn1.nist.NISTObjectIdentifiers.id_sha256;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_RSAES_OAEP;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_mgf1;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_pSpecified;
import static org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

@Component
public class CmsUtil {

	private final AlgorithmIdentifier keyEncryptionScheme;
	private final ASN1ObjectIdentifier cmsEncryptionAlgorithm;

	public CmsUtil() {
		if (Objects.isNull(getProvider(PROVIDER_NAME))) {
			addProvider(new BouncyCastleProvider());
		}
		this.keyEncryptionScheme = createRsaesOaepIdentifier();
		this.cmsEncryptionAlgorithm = CMSAlgorithm.AES256_CBC;
	}

	public void createCMSStreamed(InputStream inputStream, OutputStream outputStream, X509Certificate certificate) {
		try {
			JceKeyTransRecipientInfoGenerator recipientInfoGenerator = createRecipientInfoGenerator(certificate);

			CMSEnvelopedDataGenerator envelopedDataGenerator = new CMSEnvelopedDataGenerator();
			envelopedDataGenerator.addRecipientInfoGenerator(recipientInfoGenerator);

			CMSEnvelopedDataStreamGenerator envelopedDataStreamGenerator = new CMSEnvelopedDataStreamGenerator();
			envelopedDataStreamGenerator.addRecipientInfoGenerator(recipientInfoGenerator);

			OutputEncryptor contentEnctyptor = new JceCMSContentEncryptorBuilder(cmsEncryptionAlgorithm).build();

			try (OutputStream open = envelopedDataStreamGenerator.open(outputStream, contentEnctyptor)) {
				IOUtils.copyLarge(inputStream, open);
			}

		} catch (CertificateEncodingException | CMSException e) {
			throw new RuntimeException("Kunne ikke generere Cryptographic Message for dokumentpakke", e);
		} catch (IOException e) {
			throw new DokumentpakkingException("Klarte ikke kryptere dokumentpakke", e);
		}
	}

	private JceKeyTransRecipientInfoGenerator createRecipientInfoGenerator(X509Certificate certificate)
			throws CertificateEncodingException {
		return keyEncryptionScheme == null
				? new JceKeyTransRecipientInfoGenerator(certificate)
				: new JceKeyTransRecipientInfoGenerator(certificate, keyEncryptionScheme);
	}

	private AlgorithmIdentifier createRsaesOaepIdentifier() {
		AlgorithmIdentifier hash = new AlgorithmIdentifier(id_sha256, DERNull.INSTANCE);
		AlgorithmIdentifier mask = new AlgorithmIdentifier(id_mgf1, hash);
		AlgorithmIdentifier pSource = new AlgorithmIdentifier(id_pSpecified, new DEROctetString(new byte[0]));
		ASN1Encodable parameters = new RSAESOAEPparams(hash, mask, pSource);
		return new AlgorithmIdentifier(id_RSAES_OAEP, parameters);
	}
}
