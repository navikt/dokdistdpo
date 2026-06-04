package no.nav.dokdistdpo.qdist015.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ObjectWriter;

import static tools.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class JsonSerializer {

	private static final JsonMapper objectMapper = JsonMapper.builder()
			.enable(ALLOW_JAVA_COMMENTS)
			.disable(FAIL_ON_UNKNOWN_PROPERTIES)
			.build();
	private static final ObjectWriter writer = objectMapper.writer();

	public static String serialize(Object object) {
		try {
			return writer.writeValueAsString(object);
		} catch (JacksonException e) {
			throw new IllegalStateException(e);
		}
	}

	public static <T> T deserialize(String jsonPayload, Class<T> clazz) {
		try {
			if (isBlank(jsonPayload)) {
				throw new IllegalStateException("json payload er null");
			}
			return objectMapper.readValue(jsonPayload, clazz);
		} catch (JacksonException e) {
			throw new IllegalStateException(e);
		}
	}

	private JsonSerializer() {
	}
}
