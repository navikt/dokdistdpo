package no.nav.dokdistdpo.consumer.dpo.packaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.AltinnDokument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.xml.BrokerServiceManifest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.MessageFromAltinn2;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.DpoKvitteringMelding;
import no.nav.dokdistdpo.exception.functional.DokumentUnpackingException;
import no.nav.dokdistdpo.utils.AutoCloseableTempFile;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.Objects.nonNull;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.MANIFEST_XML;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SBD_JSON;

@Slf4j
@Component
public class DpoMessageUnpacker {

	private static final String TEMPFILE_EXCEPTION = "Feil ved innlesing/kopiering av inputStream til temporær fil";
	private static final String UNMARSHALLING_EXCEPTION = "Feil ved unmarshalling av fil med filreferanse: ";
	private static final String MESSAGE_CHANNEL = "dokdistdpo";

	private final ObjectMapper objectMapper;

	public DpoMessageUnpacker(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public List<AltinnDokument> unpackMessageFromAltinn(List<MessageFromAltinn2> messageFromAltinn2s) {
		return messageFromAltinn2s.stream()
				.map(this::unpack)
				.filter(altinnDokument -> nonNull(altinnDokument.dpoKvitteringMelding()))
				.filter(altinnDokument -> MESSAGE_CHANNEL.equals(altinnDokument.dpoKvitteringMelding().getMessageChannelName()))
				.toList();
	}

	private AltinnDokument unpack(MessageFromAltinn2 melding) {
		log.info("Pakker ut zipfil med referanse={}", melding.filreferanse());

		try (AutoCloseableTempFile tempFile = new AutoCloseableTempFile("altinn", "test")) {
			FileUtils.copyInputStreamToFile(melding.inputStream(), tempFile.toFile());

			return buildAltinnDokumentFromTempFile(tempFile.toFile(), melding.filreferanse());
		} catch (IOException e) {
			log.error(TEMPFILE_EXCEPTION, e);
			throw new DokumentUnpackingException(TEMPFILE_EXCEPTION, e);
		} finally {
			log.info("Pakket ut zipfil med referanse={}", melding.filreferanse());
		}
	}

	private AltinnDokument buildAltinnDokumentFromTempFile(File tempFile, String fileReference) {
		BrokerServiceManifest manifest = null;
		DpoKvitteringMelding dpoKvitteringMelding = null;

		try (ZipFile zipFile = new ZipFile(tempFile)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();  // entries = manifest.xml || sbd.json
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				final InputStream inputStream = zipFile.getInputStream(zipEntry);
				if (MANIFEST_XML.equals(zipEntry.getName())) {
					manifest = unmarshalXmlObject(inputStream);
				} else if (SBD_JSON.equals(zipEntry.getName())) {
					dpoKvitteringMelding = objectMapper.readValue(inputStream, DpoKvitteringMelding.class);
				} else {
					log.info("Hopper over fil: {}", zipFile.getName());
				}
			}
		} catch (JAXBException | IOException e) {
			log.error(UNMARSHALLING_EXCEPTION + "{}", fileReference, e);
			throw new DokumentUnpackingException(UNMARSHALLING_EXCEPTION + fileReference, e);
		}
		return AltinnDokument.builder()
				.fileReference(fileReference)
				.manifest(manifest)
				.dpoKvitteringMelding(dpoKvitteringMelding)
				.build();
	}

	private static BrokerServiceManifest unmarshalXmlObject(InputStream inputStream) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(BrokerServiceManifest.class);
		Unmarshaller unmarshal = context.createUnmarshaller();
		return  (BrokerServiceManifest) unmarshal.unmarshal(inputStream);
	}
}
