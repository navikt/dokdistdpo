
package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json;

import lombok.Data;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.Scope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.ScopeType;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.StandardBusinessDocumentHeader;

import java.util.Optional;

import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.ScopeType.CONVERSATION_ID;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.ScopeType.MESSAGE_CHANNEL;

@Data
public class DpoKvitteringMelding {

	private StandardBusinessDocumentHeader standardBusinessDocumentHeader;
	private KvitteringStatus status;

	public String getConversationId() {
		return standardBusinessDocumentHeader.getBusinessScope().getScope().stream()
				.filter(CONVERSATION_ID)
				.findAny().orElseThrow(RuntimeException::new)
				.getInstanceIdentifier();
	}

	public String getMessageChannelName() {
		return standardBusinessDocumentHeader.getBusinessScope().getScope().stream()
				.filter(MESSAGE_CHANNEL)
				.map(Scope::getIdentifier)
				.findAny().orElse("[ikke satt]");
	}

	public String getProcess() {
		return this.getScope(ScopeType.CONVERSATION_ID).map(Scope::getIdentifier).orElseThrow(() -> new IllegalStateException("SBD missing process"));
	}

	public Optional<Scope> getScope(ScopeType scopeType) {
		return Optional.ofNullable(this.standardBusinessDocumentHeader).flatMap(p -> p.getScope(scopeType));
	}
}
