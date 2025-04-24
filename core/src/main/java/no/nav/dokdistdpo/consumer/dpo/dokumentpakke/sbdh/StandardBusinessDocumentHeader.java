package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class StandardBusinessDocumentHeader {
	private String headerVersion;
	private Set<Partner> sender;
	private Set<Partner> receiver;
	private DocumentIdentification documentIdentification;
	private BusinessScope businessScope;

	public void addSender(Partner partner) {
		if (sender == null) { sender = new HashSet<>(); }
		sender.add(partner);
	}

	public void addReceiver(Partner partner) {
		if (receiver == null) { receiver = new HashSet<>(); }
		receiver.add(partner);
	}
}
