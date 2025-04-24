package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.asice;

import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.NavDokument;
import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils;
import no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.ZipFile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.asice.AsiceCreator.MANIFEST_XML;
import static no.nav.dokdistdpo.consumer.dpo.testutils.CertTestUtils.itestVirksomhetssertifikatProperties;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.classpathToString;
import static org.apache.commons.io.IOUtils.toBufferedInputStream;
import static org.assertj.core.api.Assertions.assertThat;

class AsiceCreatorTest {

	private static final String ARKIVMELDING_NAME = "arkivmelding.xml";
	private static final String AVTALTMELDING_CONTENTS = "avtalt";
	private static final String DOKUMENT_1_NAME = "test1.pdf";
	private static final String DOKUMENT_1_CONTENTS = "test1pdf";
	private static final String DOKUMENT_2_NAME = "test2.pdf";
	private static final String DOKUMENT_2_CONTENTS = "test2pdf";
	private static final String MOTTAKER_ID = "974761084";
	private static final String CONVERSATION_ID = UUID.randomUUID().toString();
	private static final String BESTILLINGS_ID = UUID.randomUUID().toString();

	private final AsiceCreator asiceCreator = new AsiceCreator();

	@Test
	public void shouldCreateAndSignAsiceDocument() throws IOException {
		ByteArrayOutputStream asiceStreamed = asiceCreator.createAsiceStreamed(createNavDokumentpakke(),
				Stream.of(NavDokument.fromVedlegg(DOKUMENT_1_NAME, new ByteArrayInputStream(DOKUMENT_1_CONTENTS.getBytes())),
						NavDokument.fromVedlegg(DOKUMENT_2_NAME, new ByteArrayInputStream(DOKUMENT_2_CONTENTS.getBytes()))),
				new AppCertificate(itestVirksomhetssertifikatProperties())
		);

		ByteArrayInputStream asice = new ByteArrayInputStream(asiceStreamed.toByteArray());
		final List<ZipFile> zipEntries = TestUtils.zipEntries(toBufferedInputStream(asice));

		assertThat(zipEntries).size().isEqualTo(8);
		assertThat(zipEntries).extracting(ZipFile::getName).containsAll(
				List.of("mimetype",
						MANIFEST_XML,
						ARKIVMELDING_NAME,
						DOKUMENT_1_NAME,
						DOKUMENT_2_NAME,
						"META-INF/ASiCManifest.xml",
						"META-INF/manifest.xml"
				));
		assertFileContents(zipEntries, ARKIVMELDING_NAME, AVTALTMELDING_CONTENTS);
		assertFileContents(zipEntries, MANIFEST_XML, classpathToString("asice/manifest.xml"));
		assertFileContents(zipEntries, DOKUMENT_1_NAME, DOKUMENT_1_CONTENTS);
		assertFileContents(zipEntries, DOKUMENT_2_NAME, DOKUMENT_2_CONTENTS);

	}

	private NavDokumentpakke createNavDokumentpakke() {
		return NavDokumentpakke.builder()
				.mottakerId(MOTTAKER_ID)
				.bestillingsId(BESTILLINGS_ID)
				.conversationId(CONVERSATION_ID)
				.navDokumenter(List.of(createNavDokument()))
				.arkivmelding(createNavDokument())
				.build();
	}

	private NavDokument createNavDokument() {
		return NavDokument.fromAvtaltmelding(new ByteArrayInputStream(AVTALTMELDING_CONTENTS.getBytes()));
	}

	private void assertFileContents(List<ZipFile> zipFiles, String filename, String exceptedFileContents) {
		final ZipFile arkivmelding = zipFiles.stream()
				.filter(z -> filename.equals(z.getName()))
				.findFirst()
				.orElseThrow(IllegalStateException::new);

		assertThat(arkivmelding.getContentsAsString()).isEqualToIgnoringWhitespace(exceptedFileContents);
	}
}