package no.nav.dokdistdpo.consumer.token.naistexas;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.dokdistdpo.consumer.dpo.maskinporten.Consumer;

import java.util.List;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_ORG_AUTHORITY;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.asIso6523;

public record AutorizationDetail(
		String type,
		@JsonProperty("systemuser_org")
		Consumer consumer,
		@JsonProperty("system_id")
		String systemId) {

	private static final String NAV_SYSTEM_REGISTER = NAV_ORGNUMMER + "_dokdistdpo";

	public static List<AutorizationDetail> authorizationDetails() {
		AutorizationDetail authorizationDetail = new AutorizationDetail(
				"urn:altinn:systemuser",
				Consumer.builder()
						.authority(ISO6523_ORG_AUTHORITY)
						.id(asIso6523(NAV_ORGNUMMER))
						.build(),
				NAV_SYSTEM_REGISTER
		);

		return List.of(authorizationDetail);
	}
}
