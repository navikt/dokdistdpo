package no.nav.dokdistdpo.consumer.dpo.maskinporten;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Consumer {
	@JsonProperty("authority")
	@JsonAlias("Authority")
	private String authority;
	@JsonProperty("ID")
	private String id;
}