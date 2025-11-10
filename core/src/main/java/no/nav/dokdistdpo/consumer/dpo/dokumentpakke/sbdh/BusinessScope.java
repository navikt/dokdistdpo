package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BusinessScope", propOrder = {
		"scope"
})
@Data
@Builder
public class BusinessScope {

	@XmlElement(name = "Scope")
	protected Set<Scope> scope;

	public Set<Scope> getScope() {
		if (scope == null) {
			scope = new LinkedHashSet<>();
		}
		return this.scope;
	}
}
