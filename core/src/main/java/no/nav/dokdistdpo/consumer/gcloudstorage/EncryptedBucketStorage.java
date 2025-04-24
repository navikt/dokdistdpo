package no.nav.dokdistdpo.consumer.gcloudstorage;

public interface EncryptedBucketStorage {
	String downloadObject(String objectName, String associatedData);
}
