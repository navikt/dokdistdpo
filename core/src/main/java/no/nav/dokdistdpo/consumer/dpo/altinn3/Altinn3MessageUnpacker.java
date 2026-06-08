package no.nav.dokdistdpo.consumer.dpo.altinn3;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json.DpoKvitteringMelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.AltinnDokument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.Altinn3InnsendtFilkvittering;
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
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SBD_JSON;

@Slf4j
@Component
public class Altinn3MessageUnpacker {

	private static final String TEMPFILE_EXCEPTION = "Feil ved innlesing/kopiering av inputStream til temporær fil";
	private static final String DESERIALISERE_EXCEPTION = "Kunne ikke deserialisere fil med fileReferenceId: ";
	private static final String MESSAGE_CHANNEL = "dokdistdpo";

	private final JsonMapper jsonMapper;

	public Altinn3MessageUnpacker(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public List<AltinnDokument> unpackMessageFromAltinn(List<Altinn3InnsendtFilkvittering> messageFromAltinn2s) {
		return messageFromAltinn2s.stream()
				.map(this::unpack)
				.filter(altinnDokument -> nonNull(altinnDokument.dpoKvitteringMelding()))
				.filter(altinnDokument -> MESSAGE_CHANNEL.equals(altinnDokument.dpoKvitteringMelding().getMessageChannelName()))
				.toList();
	}

	private AltinnDokument unpack(Altinn3InnsendtFilkvittering melding) {
		log.info("Pakker ut zipfil med referanse={}", melding.fileReferenceId());

		try (AutoCloseableTempFile tempFile = new AutoCloseableTempFile("altinn", "test")) {
			FileUtils.copyInputStreamToFile(melding.content(), tempFile.toFile());

			return buildAltinnDokumentFromTempFile(tempFile.toFile(), melding.fileReferenceId());
		} catch (IOException e) {
			log.error(TEMPFILE_EXCEPTION, e);
			throw new DokumentUnpackingException(TEMPFILE_EXCEPTION, e);
		} finally {
			log.info("Pakket ut zipfil med referanse={}", melding.fileReferenceId());
		}
	}

	private AltinnDokument buildAltinnDokumentFromTempFile(File tempFile, String fileReference) {
		try (ZipFile zipFile = new ZipFile(tempFile)) {
			DpoKvitteringMelding dpoKvitteringMelding = null;
			Enumeration<? extends ZipEntry> entries = zipFile.entries();  // entries = manifest.xml || sbd.json
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
					if (SBD_JSON.equals(zipEntry.getName())) {
						dpoKvitteringMelding = jsonMapper.readValue(inputStream, DpoKvitteringMelding.class);
					} else {
						log.info("Hopper over fil: {}", zipFile.getName());
					}
				}
			}
			return AltinnDokument.builder()
					.fileReference(fileReference)
					.dpoKvitteringMelding(dpoKvitteringMelding)
					.build();
		} catch (IOException | JacksonException e) {
			log.error(DESERIALISERE_EXCEPTION + "{}", fileReference, e);
			throw new DokumentUnpackingException(DESERIALISERE_EXCEPTION + fileReference, e);
		}
	}
}
