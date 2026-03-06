package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Scope", propOrder = {
		"type",
		"instanceIdentifier",
		"identifier",
		"scopeInformation"
})
@Data
@Builder
public class Scope {

	@XmlElement(name = "Type", required = true)
	protected String type;

	@XmlElement(name = "InstanceIdentifier", required = true)
	protected String instanceIdentifier;

	@XmlElement(name = "Identifier")
	protected String identifier;

	@XmlElement(name = "ScopeInformation")
	protected Set<CorrelationInformation> scopeInformation;


	public Set<CorrelationInformation> getScopeInformation() {
		if (scopeInformation == null) {
			scopeInformation = new HashSet<>();
		}
		return this.scopeInformation;
	}

	public Scope addScopeInformation(CorrelationInformation correlationInformation) {
		getScopeInformation().add(correlationInformation);
		return this;
	}
}
