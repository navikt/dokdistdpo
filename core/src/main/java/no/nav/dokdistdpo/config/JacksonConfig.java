package no.nav.dokdistdpo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET;
import static com.fasterxml.jackson.databind.MapperFeature.DEFAULT_VIEW_INCLUSION;
import static com.fasterxml.jackson.databind.SerializationFeature.CLOSE_CLOSEABLE;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.DEFAULT_ZONE_ID;

@Configuration
public class JacksonConfig {

	@Bean
	Clock clock() {
		return Clock.system(DEFAULT_ZONE_ID);
	}

	@Bean(name = "dpoObjectMapper")
	public ObjectMapper dpoObjectMapper(Clock clock) {
		Objects.requireNonNull(clock, "Clock kan ikke være null");

		return new Jackson2ObjectMapperBuilder()
				.deserializerByType(OffsetDateTime.class, new IsoDateTimeDeserializer(clock))
				.modulesToInstall(JavaTimeModule.class)
				.serializationInclusion(NON_NULL)
				.featuresToEnable(
						INDENT_OUTPUT,
						DEFAULT_VIEW_INCLUSION)
				.featuresToDisable(
						WRITE_DATES_AS_TIMESTAMPS,
						CLOSE_CLOSEABLE,
						AUTO_CLOSE_TARGET)
				.build();
	}

	private static final class IsoDateTimeDeserializer extends InstantDeserializer<OffsetDateTime> {

		IsoDateTimeDeserializer(Clock clock) {
			super(
					OffsetDateTime.class,
					DateTimeFormatter.ISO_DATE_TIME,
					temporal -> parseWithClock(clock, temporal),
					a -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
					a -> OffsetDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
					(d, z) -> d.withOffsetSameInstant(z.getRules().getOffset(d.toLocalDateTime())),
					true
			);
		}

		private static OffsetDateTime parseWithClock(Clock clock, TemporalAccessor temporal) {
			ZoneId zone = temporal.query(TemporalQueries.zone());
			if (zone != null) {
				return OffsetDateTime.from(temporal);
			}
			LocalDateTime localDateTime = LocalDateTime.from(temporal);
			ZoneOffset offset = DEFAULT_ZONE_ID.getRules().getOffset(LocalDateTime.now(clock));
			return localDateTime.atOffset(offset);
		}
	}
}
