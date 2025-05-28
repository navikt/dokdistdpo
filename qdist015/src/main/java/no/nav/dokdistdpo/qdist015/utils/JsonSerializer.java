package no.nav.dokdistdpo.qdist015.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class JsonSerializer {

	private static ObjectMapper objectMapper = new ObjectMapper();
	private static final ObjectWriter writer = objectMapper.writer();

	static {
		objectMapper.configure(ALLOW_COMMENTS, true);
		objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static String serialize(Object object) {
		try {
			return writer.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}

	public static <T> T deserialize(String jsonPayload, Class<T> clazz) {
		try {
			if (isBlank(jsonPayload)) {
				throw new IllegalStateException("json payload er null");
			}
			return objectMapper.readValue(jsonPayload, clazz);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}
}
