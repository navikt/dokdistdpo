package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import tools.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Builder;
import lombok.Data;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StandardBusinessDocument", propOrder = {
		"standardBusinessDocumentHeader",
		"any"
})
@Data
@Builder
@JsonSerialize(using = StandardBusinessDocumentSerializer.class)
public class StandardBusinessDocument {

	@XmlElement(name = "StandardBusinessDocumentHeader")
	@NotNull
	@Valid
	private StandardBusinessDocumentHeader standardBusinessDocumentHeader;

	@XmlAnyElement(lax = true)
	@JsonAlias({"arkivmelding_kvittering", "avtalt", "arkivmelding", "status"})
	@NotNull
	private Object any;
}
