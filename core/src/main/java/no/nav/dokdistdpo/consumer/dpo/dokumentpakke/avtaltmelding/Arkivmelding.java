package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement(name = "arkivmelding", namespace = "urn:no:difi:meldingsutveksling:2.0")
public class Arkivmelding extends BusinessMessage<Arkivmelding> {
	String identifier;
	Object content;
}
