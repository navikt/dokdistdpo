package no.nav.dokdistdpo.qdist015;

import no.nav.dokdistdpo.consumer.saf.SafJournalpost;
import no.nav.dokdistdpo.exception.functional.DokdistdpoIllegalArgumentException;
import no.nav.dokdistdpo.exception.technical.KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public final class Qdist015Utils {

	private static final String AKTOERID = "AKTOERID";
	private static final String ORGNR = "ORGNR";
	private static final String FNR = "FNR";

	private Qdist015Utils() {
	}

	public static boolean isBrukerTypeOrgNr(SafJournalpost.Bruker bruker) {
		return ORGNR.equals(bruker.type());
	}

	public static boolean isBrukerTypeFnr(SafJournalpost.Bruker bruker) {
		return FNR.equals(bruker.type());
	}

	public static boolean isBrukerTypeAktoerId(SafJournalpost.Bruker bruker) {
		return AKTOERID.equals(bruker.type());
	}

	public static boolean isHoveddokument(int rekkefolge) {
		return rekkefolge == 1;
	}

	public static XMLGregorianCalendar convertLocalDateTimeToXmlGregorianCalendar(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			throw new DokdistdpoIllegalArgumentException("Kunne ikke konvertere fra localDateTime til XmlGregorianCalendar. localDateTime=null");
		}
		try {
			return DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(localDateTime.format(ISO_LOCAL_DATE_TIME));
		} catch (DatatypeConfigurationException e) {
			throw new KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException(
					format("Kunne ikke konvertere fra localDateTime til XmlGregorianCalendar. Forsøkte å konvertere localDateTime=%s", localDateTime), e);
		}
	}

	public static XMLGregorianCalendar getNow() {
		XMLGregorianCalendar now;
		try {
			now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			throw new DokdistdpoIllegalArgumentException("Kunne ikke hente dagens dato", e);
		}
		return now;
	}
}
