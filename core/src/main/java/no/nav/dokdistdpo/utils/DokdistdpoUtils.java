package no.nav.dokdistdpo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ProblemDetail;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
}
