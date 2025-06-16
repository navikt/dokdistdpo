package no.nav.dokdistdpo.utils;

import no.nav.dokdistdpo.exception.functional.DokdistdpoIllegalArgumentException;
import no.nav.dokdistdpo.exception.technical.KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class DokdistdpoUtils {

	public static ProblemDetail getProblemDetail(ClientHttpResponse response) throws IOException {
		String message = IOUtils.toString(response.getBody(), StandardCharsets.UTF_8);
		return ProblemDetail.forStatusAndDetail(response.getStatusCode(), message)
				;
	}

	public static InputStream toBufferedStream(InputStream inputStream) {
		return new BufferedInputStream(inputStream);
	}

	public static InputStream toBufferedStream(byte[] data) {
		return new BufferedInputStream(new ByteArrayInputStream(data));
	}

	public static XMLGregorianCalendar convertLocalDateTimeToXmlGregorianCalendar(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			throw new KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException("Kunne ikke konvertere fra localDateTime til XmlGregorianCalendar. localDateTime=null");
		}
		try {
			return DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(localDateTime.format(ISO_LOCAL_DATE_TIME));
		} catch (DatatypeConfigurationException e) {
			throw new KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException(
					format("Kunne ikke konvertere fra localDateTime til XmlGregorianCalendar. Forsøkte å konvertere localDateTime=%s", localDateTime), e);
		}
	}

	public static void assertNotEmpty(String field, String value) {
		if (isBlank(value)) {
			throw new DokdistdpoIllegalArgumentException(format("%s kan ikke være null or tomt", field));
		}
	}

	public static void assertNotNull(String field, Object value) {
		if (isNull(value)) {
			throw new DokdistdpoIllegalArgumentException(format("%s kan ikke være null.", field));
		}
	}
}
