package no.nav.dokdistdpo.consumer.dpo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.Arkivmelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.AvtaltMelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.exception.functional.DokumentpakkingException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class DpoMessagePackager {

	public static final String DPO_SBD = "sbd.json";
	public static final String DPO_ASIC = "asic.zip";

	private final ObjectMapper objectMapper;
	private final DpoContentPackager dpoContentPackager;

	public DpoMessagePackager(ObjectMapper dpoObjectMapper,
							  DpoContentPackager dpoContentPackager) {
		this.objectMapper = dpoObjectMapper;
		this.dpoContentPackager = dpoContentPackager;
	}

	/**
	 * @param appCertificate     Navs virksomhetsertifikat
	 * @param mottakerSertifikat dpo mottakersertifikat
	 * @return
	 */
	public InputStream packageMessage(AltinnDpoRequest altinnDpoRequest,
									  AppCertificate appCertificate,
									  X509Certificate mottakerSertifikat) {

		InputStream content = dpoContentPackager.packageContent(altinnDpoRequest, appCertificate, mottakerSertifikat);
		final ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
		writeZip(altinnDpoRequest.businessDocument(), content, zipFile);
		final byte[] zippedBytes = zipFile.toByteArray();
		log.info("Laget dpo dokumentpakke zip. filstørrelse={}, conversationId={}, bestillingsId={}", zippedBytes.length,
				altinnDpoRequest.forsendelse().konversjonsId(), altinnDpoRequest.forsendelse().bestillingsId());

		return new ByteArrayInputStream(zippedBytes);
	}

	private void writeZip(StandardBusinessDocument konvolutt, InputStream innhold, OutputStream outputStream) {
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

			if (konvolutt.getAny() instanceof AvtaltMelding || konvolutt.getAny() instanceof Arkivmelding) {
				zipOutputStream.putNextEntry(new ZipEntry(DPO_SBD));
				objectMapper.writeValue(zipOutputStream, konvolutt);
				zipOutputStream.closeEntry();
			}

			zipOutputStream.putNextEntry(new ZipEntry(DPO_ASIC));
			IOUtils.copy(innhold, zipOutputStream);
			zipOutputStream.closeEntry();
			zipOutputStream.finish();

		} catch (IOException e) {
			throw new DokumentpakkingException("Klarte ikke lage sbd.zip", e);
		}
	}
}
