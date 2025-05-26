package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement(name = "arkivmelding")
public class Arkivmelding extends BusinessMessage<Arkivmelding> {
	String identifier;
	Object content;
}
