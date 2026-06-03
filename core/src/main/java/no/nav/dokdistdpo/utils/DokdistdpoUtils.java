package no.nav.dokdistdpo.utils;

import no.nav.dokdistdpo.exception.functional.DokdistdpoIllegalArgumentException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class DokdistdpoUtils {

	public static InputStream toBufferedStream(InputStream inputStream) {
		return new BufferedInputStream(inputStream);
	}

	public static InputStream toBufferedStream(byte[] data) {
		return new BufferedInputStream(new ByteArrayInputStream(data));
	}

	public static void assertNotEmpty(String field, String value) {
		if (isBlank(value)) {
			throw new DokdistdpoIllegalArgumentException(format("%s kan ikke være null eller tomt", field));
		}
	}

	public static void assertNotNull(String field, Object value) {
		if (isNull(value)) {
			throw new DokdistdpoIllegalArgumentException(format("%s kan ikke være null.", field));
		}
	}

	private DokdistdpoUtils() {
	}
}
