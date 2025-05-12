package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.arkivmelding;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.BusinessMessage;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement(name = "arkivmelding")
public class ArkivMelding extends BusinessMessage<ArkivMelding> {
	String identifier;
	Object content;
}
