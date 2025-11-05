package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerIdentification {
	@JsonIgnore
	private Partner partner;
	private String authority;
	private String value;
}
