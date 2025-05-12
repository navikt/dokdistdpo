package no.nav.dokdistdpo.qdist015.avtaltmelding;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TestUtils {


	private TestUtils() {
	}

	public static LocalDateTime convertFromXmlGregorianCalendarToLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
		return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
	}
}
