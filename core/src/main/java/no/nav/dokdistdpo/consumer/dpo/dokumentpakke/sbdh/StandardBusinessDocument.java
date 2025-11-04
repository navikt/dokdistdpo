package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;
import java.util.Set;

import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType.CONVERSATION_ID;

@Data
@Builder
@JsonSerialize(using = StandardBusinessDocumentSerializer.class)
public class StandardBusinessDocument {

	@NotNull
	StandardBusinessDocumentHeader standardBusinessDocumentHeader;

	@NotNull
	@JsonAlias({"arkivmelding_kvittering", "avtalt", "arkivmelding", "status"})
	Object any;

	public Set<Scope> getScopes() {
		return standardBusinessDocumentHeader
				.getBusinessScope()
				.getScope();
	}

	public Scope getScope(ScopeType scopeType) {
		return findScope(scopeType).orElseThrow(() -> new RuntimeException(String.format("Missing scope %s", scopeType.name())));
	}

	public Optional<Scope> findScope(ScopeType scopeType) {
		return getScopes().stream()
				.filter(scopeType)
				.findAny();
	}

	public String getProcess() {
		return getScope(CONVERSATION_ID)
				.getIdentifier();
	}

	public String getDokumentId() {
		return standardBusinessDocumentHeader.getDocumentIdentification().getInstanceIdentifier();
	}
}
