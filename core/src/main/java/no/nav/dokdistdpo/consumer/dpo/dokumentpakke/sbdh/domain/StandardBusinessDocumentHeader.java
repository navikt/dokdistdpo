package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StandardBusinessDocumentHeader", propOrder = {
		"headerVersion",
		"sender",
		"receiver",
		"documentIdentification",
		"manifest",
		"businessScope"
})
@Data
@Builder
public class StandardBusinessDocumentHeader {

	@XmlElement(name = "HeaderVersion", required = true)
	private String headerVersion;

	@Setter
	@XmlElement(name = "Sender", required = true)
	private Set<Sender> sender;

	@XmlElement(name = "Receiver", required = true)
	private Set<Receiver> receiver;

	@XmlElement(name = "DocumentIdentification", required = true)
	private DocumentIdentification documentIdentification;

	@XmlElement(name = "BusinessScope")
	private BusinessScope businessScope;

	public Set<Sender> getSender() {
		if (sender == null) {
			sender = new HashSet<>();
		}
		return this.sender;
	}

	public void addSender(Sender partner) {
		getSender().add(partner);
	}

	public Set<Receiver> getReceiver() {
		if (receiver == null) {
			receiver = new HashSet<>();
		}
		return this.receiver;
	}

	public void addReceiver(Receiver partner) {
		getReceiver().add(partner);
	}

	@JsonIgnore
	public String getDocumentType() {
		return this.getDocumentIdentification().getStandard();
	}

	@JsonIgnore
	public Set<Scope> getScopes() {
		return Optional.ofNullable(this.getBusinessScope()).flatMap(p -> Optional.ofNullable(p.getScope())).orElseGet(Collections::emptySet);
	}

	@JsonIgnore
	public Optional<Scope> getScope(ScopeType scopeType) {
		return this.getScopes().stream().filter(scope -> scopeType.name().equals(scope.getType()) || scopeType.getFullname().equals(scope.getType())).findAny();
	}
}