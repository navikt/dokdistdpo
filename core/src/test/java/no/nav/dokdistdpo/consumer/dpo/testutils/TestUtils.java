package no.nav.dokdistdpo.consumer.dpo.testutils;

import lombok.Data;
import lombok.SneakyThrows;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType;
import no.nav.dokdistdpo.consumer.dpo.altinn3.AltinnDpoRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.toByteArray;

public final class TestUtils {

	public static final String MOTTAKER_ID = "974761084";
	public static final UUID CONVERSATION_ID = UUID.fromString("5cc73a11-7991-449f-8a65-978f61b8d171");
	public static final UUID BESTILLINGS_ID = UUID.fromString("d710340a-ddec-4bfa-bdfc-745016bbeb33");

	public static final String ARKIVMELDING_XML = TestUtils.classpathToString("avtaltmelding/arkivmelding.xml");


	private TestUtils() {
	}

	public static AltinnDpoRequest.Forsendelse createForsendelse(ForsendelseMetadataType forsendelseMetadata) {
		return AltinnDpoRequest.Forsendelse.builder()
				.mottakerId(MOTTAKER_ID)
				.bestillingsId(BESTILLINGS_ID.toString())
				.konversjonsId(CONVERSATION_ID.toString())
				.forsendelseMetadata(ARKIVMELDING_XML)
				.meldingType(forsendelseMetadata.name())
				.build();
	}

	@SneakyThrows
	public static String classpathToString(String classpathResource) {
		try {
			InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
			String message = IOUtils.toString(inputStream, UTF_8);
			IOUtils.closeQuietly(inputStream);
			return message;
		} catch (IOException e) {
			throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
		}
	}

	public static List<ZipFile> zipEntries(InputStream inputStream) {
		final List<ZipFile> zipEntries = new ArrayList<>();
		try {
			try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
				ZipEntry zipEntry;
				while ((zipEntry = zipInputStream.getNextEntry()) != null) {
					final byte[] contents = toByteArray(zipInputStream);
					zipEntries.add(new ZipFile(zipEntry.getName(), contents));
					zipInputStream.closeEntry();
				}
			}
			return zipEntries;
		} catch (IOException _) {
			return zipEntries;
		}
	}

	@Data
	public static class ZipFile {
		private final String name;
		private final byte[] contents;

		public String getContentsAsString() {
			return new String(contents, UTF_8);
		}
	}
}
