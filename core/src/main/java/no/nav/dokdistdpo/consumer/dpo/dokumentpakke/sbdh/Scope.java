package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class Scope {

	private String type;
	private String instanceIdentifier;
	private String identifier;
	private Set<CorrelationInformation> scopeInformation;

	public Set<CorrelationInformation> getScopeInformation() {
		if (scopeInformation == null) { scopeInformation = new HashSet<>();}
		return scopeInformation;
	}

	public void addScopeInformation(CorrelationInformation correlationInformation) {
		getScopeInformation().add(correlationInformation);
	}
}
