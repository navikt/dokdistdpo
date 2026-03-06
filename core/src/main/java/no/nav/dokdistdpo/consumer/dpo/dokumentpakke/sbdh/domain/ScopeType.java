package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public enum ScopeType implements Predicate<Scope> {
	CONVERSATION_ID("ConversationId"),
	BESTILLINGS_ID("BestillingsId"),
	SENDER_REF("SenderRef"),
	MESSAGE_CHANNEL("MessageChannel"),
	RECEIVER_REF("ReceiverRef");

	private final String fullname;

	@Override
	public boolean test(Scope scope) {
		return this.fullname.equals(scope.getType()) || this.name().equals(scope.getType());
	}
}
