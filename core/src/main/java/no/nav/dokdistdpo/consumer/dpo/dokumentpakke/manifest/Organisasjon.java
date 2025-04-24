package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.manifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_AUTHORITY;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_PREFIX;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisasjon")
@XmlRootElement(name = "organiasjon")
public class Organisasjon {

	@XmlAttribute
	private String authority;

	@XmlValue
	private String orgnummer;

	public Organisasjon(String orgnummer) {
		this.authority = ISO6523_AUTHORITY;
		this.orgnummer = ISO6523_PREFIX + orgnummer;
	}
}
