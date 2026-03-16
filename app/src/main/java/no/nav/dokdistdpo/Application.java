package no.nav.dokdistdpo;

import no.nav.dokdistdpo.azure.AzureProperties;
import no.nav.dokdistdpo.certificate.KeyStoreProperties;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.config.properties.DokdistmellomlagerProperties;
import no.nav.dokdistdpo.config.properties.MaskinportenProperties;
import no.nav.dokdistdpo.config.properties.NaisTexasProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties({
		AzureProperties.class,
		DokdistdpoProperties.class,
		KeyStoreProperties.class,
		MaskinportenProperties.class,
		DokdistmellomlagerProperties.class,
		NaisTexasProperties.class
})
@EnableScheduling
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}