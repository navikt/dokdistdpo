package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class PartnerIdentification implements Serializable {
	private Partner partner;
	private String authority;
	private String value;
}
