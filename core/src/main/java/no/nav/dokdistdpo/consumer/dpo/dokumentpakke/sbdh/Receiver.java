package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

public class Receiver extends Partner {

	@Override
	public Receiver setIdentifier(PartnerIdentification identifier) {
		this.identifier = identifier;
		return this;
	}
}
