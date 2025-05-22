package no.nav.dokdistdpo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokdistdpo.exception.functional.DokdistdpoIllegalArgumentException;
import no.nav.dokdistdpo.exception.technical.KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException;
import org.springframework.http.ProblemDetail;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class DokdistdpoUtils {

	public static ProblemDetail getProblemDetail(InputStream problem) {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.convertValue(problem, ProblemDetail.class);
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
			throw new DokdistdpoIllegalArgumentException(format("Feltet %s kan ikke være null or tomt", field));
		}
	}

	public static void assertNotNull(String field, Object value) {
		if (isNull(value)) {
			throw new DokdistdpoIllegalArgumentException(format("Feltet %s kan ikke være null.", field));
		}
	}

}
