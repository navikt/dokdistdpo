package no.nav.dokdistdpo.utils;

import no.nav.dokdistdpo.exception.functional.SafJournalpostValidationException;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

public final class SafJournalpostUtils {

	private SafJournalpostUtils() {
	}

	public static void assertNotNull(String field, Object value, String journalpostId) {
		if (isNull(value)) {
			throw new SafJournalpostValidationException(format("Feltet %s kan ikke være null, journalpostId=%s", field, journalpostId));
		}
	}

	public static void assertCollectionNotNull(String field, List<?> values, String journalpostId) {
		if (isEmpty(values)) {
			throw new SafJournalpostValidationException(format("Feltet %s kan ikke være null, journalpostId=%s", field, journalpostId));
		}
	}

	public static void assertNotEmpty(String field, String value, String journalpostId) {
		if (isBlank(value)) {
			throw new SafJournalpostValidationException(format("Feltet %s kan ikke være null or tomt, journalpostId=%s", field, journalpostId));
		}
	}

	public static void assertNotNull(String field, Object value, String journalpostId, String dokumentInfoId) {
		if (isNull(value)) {
			throw new SafJournalpostValidationException(format("Feltet %s kan ikke være null, journalpostid=%s, dokumentInfoId=%s", field, journalpostId, dokumentInfoId));
		}
	}

	public static void assertNotEmpty(String field, String value, String dokumentInfoId, String journalpostId) {
		if (isBlank(value)) {
			throw new SafJournalpostValidationException(format("Feltet %s kan ikke være null or tomt, dokumentInfoId=%s, journalpostId=%s", field, dokumentInfoId, journalpostId));
		}
	}

}
