package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Partner", propOrder = {
		"identifier",
		"contactInformation"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Partner {

	@XmlElement(name = "Identifier", required = true)
	protected PartnerIdentification identifier;

	public Partner setIdentifier(PartnerIdentification identifier) {
		this.identifier = identifier;
		identifier.setPartner(this);
		return this;
	}
}
