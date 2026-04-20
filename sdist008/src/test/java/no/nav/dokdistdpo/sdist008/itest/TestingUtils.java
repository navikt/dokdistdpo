package no.nav.dokdistdpo.sdist008.itest;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class TestingUtils {

	private TestingUtils() {
	}

	@SneakyThrows
	public static String classpathToString(String classpathResource) {
		try (InputStream inputStream = new ClassPathResource(classpathResource).getInputStream()) {
			return IOUtils.toString(inputStream, UTF_8);
		} catch (IOException e) {
			throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
		}
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

	public static byte[] getDownloadBody(String boundary, String path) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		final Charset utf8 = UTF_8;
		IOUtils.write("--" + boundary + "\r\n", bos, utf8);
		IOUtils.write("Content-ID: <http://tempuri.org/1>\r\n", bos, utf8);
		IOUtils.write("Content-Transfer-Encoding: 8bit\r\n", bos, utf8);
		IOUtils.write("Content-Type: application/xop+xml; charset=UTF-8; type=\"text/xml\"\r\n", bos, utf8);
		IOUtils.write("\r\n", bos, utf8);
		IOUtils.write(classpathToString("__files/altinn2/brokerserviceexternalstreamed/downloadfilestreamed_happy_response.xml"), bos, utf8);
		IOUtils.write("\r\n", bos, utf8);
		IOUtils.write("--" + boundary + "\r\n", bos, utf8);
		IOUtils.write("Content-ID: <http://tempuri.org/1/637169441367559832>\r\n", bos, utf8);
		IOUtils.write("Content-Transfer-Encoding: binary\r\n", bos, utf8);
		IOUtils.write("Content-Type: application/octet-stream\r\n", bos, utf8);
		IOUtils.write("\r\n", bos, utf8);
		IOUtils.write(classpathToByteArray(path), bos);
		IOUtils.write("\r\n", bos, utf8);
		IOUtils.write("--" + boundary, bos, utf8);

		return bos.toByteArray();
	}
}
