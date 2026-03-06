package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

public class Sender extends Partner {

	@Override
	public Sender setIdentifier(PartnerIdentification identifier) {
		this.identifier = identifier;
		return this;
	}
}
