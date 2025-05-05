package no.nav.dokdistdpo.consumer.dpo;

import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.asice.AsiceCreator;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.asice.CmsUtil;
import no.nav.dokdistdpo.exception.functional.DokumentpakkingException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;

@Component
public class DpoContentPackager {

	private final AsiceCreator asiceCreator;
	private final CmsUtil cmsUtil;

	public DpoContentPackager() {
		this.asiceCreator = new AsiceCreator();
		this.cmsUtil = new CmsUtil();
	}

	InputStream packageContent(NavDokumentpakke navDokumentpakke,
							   AppCertificate appCertificate,
							   X509Certificate mottakerSertifikat) {

		try (final ByteArrayOutputStream asiceStreamed = asiceCreator.createAsiceStreamed(navDokumentpakke,
				navDokumentpakke.navDokumenter().stream(), appCertificate)) {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			cmsUtil.createCMSStreamed(
					new ByteArrayInputStream(asiceStreamed.toByteArray()),
					outputStream,
					mottakerSertifikat);

			return new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			throw new DokumentpakkingException("Klarte ikke lage asic eller kryptere dokumentpakke", e);
		}

	}
}
