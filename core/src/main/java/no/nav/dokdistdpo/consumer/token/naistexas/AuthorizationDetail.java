package no.nav.dokdistdpo.consumer.token.naistexas;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.dokdistdpo.consumer.dpo.maskinporten.Consumer;

import java.util.List;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_ORG_AUTHORITY;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.asIso6523;

public record AuthorizationDetail(
		String type,
		@JsonProperty("systemuser_org")
		Consumer consumer,
		@JsonProperty("system_id")
		String systemId) {

	public static List<AuthorizationDetail> authorizationDetails(String systemId) {
		AuthorizationDetail authorizationDetail = new AuthorizationDetail(
				"urn:altinn:systemuser",
				Consumer.builder()
						.authority(ISO6523_ORG_AUTHORITY)
						.id(asIso6523(NAV_ORGNUMMER))
						.build(),
				systemId
		);

		return List.of(authorizationDetail);
	}
}
