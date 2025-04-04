package no.nav.dokdistdpo.consumer.maskinporten;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
public record Consumer(@JsonProperty("Authority")
					   String authority,
					   @JsonProperty("ID")
					   String id) {

	@Getter
	@AllArgsConstructor
	public enum Authority {
		/*For orgnr*/
		ISO_6523_ACTORID_UPIS("iso6523-actorid-upis"),
		/*for personnummer*/
		ISO_3166_1_ALFA2("iso3166-1-alfa2");
		private final String value;
	}
}
