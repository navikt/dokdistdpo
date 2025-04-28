package no.nav.dokdistdpo.consumer.dpo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.ArkivmeldingStandardBusinessDocumentMapper;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.AvtaltStandardBusinessDocumentMapper;
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
	private final DpoContentPackager EFormidlingContentPackager;
	private final AvtaltStandardBusinessDocumentMapper avtaltStandardBusinessDocumentMapper;
	private final ArkivmeldingStandardBusinessDocumentMapper arkivmeldingStandardBusinessDocumentMapper;

	public DpoMessagePackager(ObjectMapper dpoObjectMapper,
							  DpoContentPackager EFormidlingContentPackager) {
		this.objectMapper = dpoObjectMapper;
		this.EFormidlingContentPackager = EFormidlingContentPackager;
		this.avtaltStandardBusinessDocumentMapper = new AvtaltStandardBusinessDocumentMapper();
		this.arkivmeldingStandardBusinessDocumentMapper = new ArkivmeldingStandardBusinessDocumentMapper();

	}

	/**
	 *
	 * @param navDokumentpakke
	 * @param dpoMelding er enten en avtaltmelding eller en arkivmelding som pakkes som et objekt innenfor forretningsmeldingen
	 * @param appCertificate Navs virksomhetsertifikat
	 * @param mottakerSertifikat
	 * @return
	 */

	public InputStream packageMessage(NavDokumentpakke navDokumentpakke,
									  String dpoMelding,
									  AppCertificate appCertificate,
									  X509Certificate mottakerSertifikat) {

		var standardBusinessDocument =  getStandardBusinessDocument(navDokumentpakke, dpoMelding);

		InputStream content = EFormidlingContentPackager.packageContent(navDokumentpakke, appCertificate, mottakerSertifikat);
		final ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
		writeZip(standardBusinessDocument, content, zipFile);
		final byte[] zippedBytes = zipFile.toByteArray();
		log.info("Laget dpo dokumentpakke zip. filstørrelse={}, conversationId={}, bestillingsId={}", zippedBytes.length,
				navDokumentpakke.conversationId(), navDokumentpakke.bestillingsId());

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

	private StandardBusinessDocument getStandardBusinessDocument(NavDokumentpakke navDokumentpakke, String dpoMelding) {
		return switch (navDokumentpakke.meldingType()) {
			case DPO_ARKIVMELDING ->  arkivmeldingStandardBusinessDocumentMapper.mapArkivmeldingEnvelope(navDokumentpakke, dpoMelding);
			case DPO_AVTALEMELDING -> avtaltStandardBusinessDocumentMapper.mapAvtaltMeldingEnvelope(navDokumentpakke, dpoMelding);
		};
	}
}
