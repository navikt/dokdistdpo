package no.nav.dokdistdpo.qdist015.itest.config;

import no.nav.dokdistdpo.CoreConfig;
import no.nav.dokdistdpo.azure.AzureProperties;
import no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig;
import no.nav.dokdistdpo.certificate.AppCertificateConfig;
import no.nav.dokdistdpo.certificate.KeyStoreProperties;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.config.properties.DokdistmellomlagerProperties;
import no.nav.dokdistdpo.config.properties.MaskinportenProperties;
import no.nav.dokdistdpo.config.properties.NaisTexasProperties;
import no.nav.dokdistdpo.consumer.gcloudstorage.EncryptedBucketStorage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.resilience.annotation.EnableResilientMethods;

import static org.mockito.Mockito.mock;

@EnableResilientMethods
@Profile("itest")
@Configuration
@EnableConfigurationProperties({
		DokdistdpoProperties.class,
		DokdistmellomlagerProperties.class,
		AzureProperties.class,
		KeyStoreProperties.class,
		MaskinportenProperties.class,
		NaisTexasProperties.class
})
@Import({
		CoreConfig.class,
		JmsItestConfig.class,
		OAuthEnabledRestClientConfig.class,
		AppCertificateConfig.class,
		CacheTestConfig.class
})
@ComponentScan(basePackages = "no.nav.dokdistdpo")
public class ApplicationTestConfig {

	@Bean
	public EncryptedBucketStorage encryptedBucketStorage() {
		return mock(EncryptedBucketStorage.class);
	}
}
