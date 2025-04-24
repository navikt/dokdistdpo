package no.nav.dokdistdpo.config.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("dokdistmellomlager")
public record DokdistmellomlagerProperties(
		@NotEmpty
		String projectid,
		@NotEmpty
		String bucket,
		@NotEmpty
		String keyring,
		@NotEmpty
		String keyid) {

	public String gcpKekUri() {
		return "gcp-kms://projects/" + projectid + "/locations/europe-north1/keyRings/" + keyring + "/cryptoKeys/" + keyid;
	}
}
