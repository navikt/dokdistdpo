package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

public class Sender extends Partner {

	@Override
	public Sender setIdentifier(PartnerIdentification identifier) {
		this.identifier = identifier;
		return this;
	}
}
