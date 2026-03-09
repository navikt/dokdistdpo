package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PartnerIdentification", propOrder = {
		"value"
})
@EqualsAndHashCode(exclude = "partner")
@Getter
@Setter
@ToString(exclude = "partner")
@RequiredArgsConstructor
public class PartnerIdentification {

	@XmlTransient
	@JsonIgnore
	private Partner partner;

	@XmlValue
	protected String value;

	@XmlAttribute(name = "Authority")
	protected String authority;
}
