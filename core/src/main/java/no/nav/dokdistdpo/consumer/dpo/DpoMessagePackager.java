package no.nav.dokdistdpo.consumer.dpo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.Arkivmelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.AvtaltMelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.exception.functional.DokumentpakkingException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
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
	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");


	private final ObjectMapper objectMapper;
	private final DpoContentPackager dpoContentPackager;

	public DpoMessagePackager(ObjectMapper dpoObjectMapper,
							  DpoContentPackager dpoContentPackager) {
		this.objectMapper = dpoObjectMapper;
		this.dpoContentPackager = dpoContentPackager;
		objectMapper.registerModule(new JavaTimeModule());
	}

	/**
	 * @param appCertificate     Navs virksomhetsertifikat
	 * @param mottakerSertifikat DPO mottaker virksomhetsertifikat
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
				OutputStream nonClosingStream = new FilterOutputStream(zipOutputStream) {
					@Override
					public void close() {
						// Noop — for å forhindre at datastrømmen avsluttes for tidlig
					}
				};
				String sdbd = objectMapper.writeValueAsString(konvolutt);
				secureLog.info("SBD payload: {}", sdbd);
				nonClosingStream.write(sdbd.getBytes());
				zipOutputStream.closeEntry();
				nonClosingStream.close();
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
