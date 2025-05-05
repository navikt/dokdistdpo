package no.nav.dokdistdpo.consumer.gcloudstorage;

import com.google.cloud.storage.Storage;
import com.google.crypto.tink.Aead;
import no.nav.dokdistdpo.exception.technical.BucketFailedToDownloadTechnicalException;

import java.security.GeneralSecurityException;

public class GCloudEncryptedStorage implements EncryptedBucketStorage {

	private final String bucket;
	private final Storage storage;
	private final Aead aead;

	public GCloudEncryptedStorage(Storage storage, String bucket, Aead aead) {
		this.bucket = bucket;
		this.storage = storage;
		this.aead = aead;
	}

	@Override
	public String downloadObject(String objectName, String associatedData) {
		try {
			byte[] cipherText = storage.readAllBytes(bucket, objectName);
			byte[] plainText = aead.decrypt(cipherText, associatedData.getBytes());
			return new String(plainText);
		} catch (GeneralSecurityException e) {
			throw new BucketFailedToDownloadTechnicalException(String.format("Teknisk feil mot Google Cloud Storage ved henting på objectName=%s. Feilmelding=%s",
					objectName, e.getMessage()), e);
		}
	}
}
