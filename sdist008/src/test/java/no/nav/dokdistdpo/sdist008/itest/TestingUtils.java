package no.nav.dokdistdpo.sdist008.itest;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;

public final class TestingUtils {

	private TestingUtils() {
	}

	@SneakyThrows
	public static byte[] classpathToByteArray(String classpathResource) {
		try (InputStream content = new ClassPathResource(classpathResource).getInputStream()) {
			ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
			IOUtils.copy(content, zipFile);
			return zipFile.toByteArray();
		} catch (IOException e) {
			throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
		}
	}
}
