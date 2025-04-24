package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class BusinessScope implements Serializable {

	private Set<Scope> scope;

	public Set<Scope> getScope() {
		if (scope == null) { scope = new HashSet<>();}
		return scope;
	}

	public void addScope(Scope scope) {
		getScope().add(scope);
	}
}
