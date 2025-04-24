package no.nav.dokdistdpo.consumer.gcloudstorage;

import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.cloud.storage.HttpStorageOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.RegistryConfiguration;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.KmsEnvelopeAeadKeyManager;
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient;
import no.nav.dokdistdpo.config.properties.DokdistmellomlagerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import java.security.GeneralSecurityException;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;

@Profile("nais")
@Configuration
public class GCloudStorageConfig {

	public static final String KEYTEMPLATE = "AES128_GCM";

	@Bean
	@Lazy
	public EncryptedBucketStorage encryptedBucketStorage(DokdistmellomlagerProperties dokdistmellomlagerProperties) throws Exception {

		var kekUri = dokdistmellomlagerProperties.gcpKekUri();

		AeadConfig.register();
		GcpKmsClient.register(Optional.of(kekUri), Optional.empty());

		KeyTemplate keyTemplate = KmsEnvelopeAeadKeyManager.createKeyTemplate(kekUri, KeyTemplates.get(KEYTEMPLATE));
		KeysetHandle keysetHandle = KeysetHandle.generateNew(keyTemplate);
		Aead aead = keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead.class);

		Storage storage = StorageOptions.newBuilder()
				.setProjectId(dokdistmellomlagerProperties.projectid())
				.setTransportOptions(HttpStorageOptions.defaults().getDefaultTransportOptions().toBuilder()
						.setConnectTimeout((int) SECONDS.toMillis(5))
						.setReadTimeout((int) SECONDS.toMillis(20))
						.setHttpTransportFactory(ApacheHttpTransport::new)
						.build()
				)
				.build().getService();

		return new GCloudEncryptedStorage(storage, dokdistmellomlagerProperties.bucket(), aead);
	}

	@Bean
	public Aead aead(DokdistmellomlagerProperties dokdistmellomlagerProperties) throws GeneralSecurityException {
		var kekUri = dokdistmellomlagerProperties.gcpKekUri();

		AeadConfig.register();
		GcpKmsClient.register(Optional.of(kekUri), Optional.empty());

		return new GcpKmsClient().getAead(kekUri);
	}
}
