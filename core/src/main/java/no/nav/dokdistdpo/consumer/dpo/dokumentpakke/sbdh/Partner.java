package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class Partner {
	PartnerIdentification identifier;
	Set<ContactInformation> contactInformation;
}
