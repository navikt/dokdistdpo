package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KvitteringStatus {
	@JsonValue
	private String status;
}
