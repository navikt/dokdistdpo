package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentIdentification", propOrder = {
		"standard",
		"typeVersion",
		"instanceIdentifier",
		"type",
		"multipleType",
		"creationDateAndTime"
})
@Data
@Builder
public class DocumentIdentification implements Serializable {

	@XmlElement(name = "Standard", required = true)
	protected String standard;

	@XmlElement(name = "TypeVersion", required = true)
	protected String typeVersion;

	@XmlElement(name = "InstanceIdentifier", required = true)
	protected String instanceIdentifier;

	@XmlElement(name = "Type", required = true)
	protected String type;

	@XmlElement(name = "MultipleType")
	protected Boolean multipleType;

	@XmlElement(name = "CreationDateAndTime", required = true)
	@XmlSchemaType(name = "dateTime")
	@XmlJavaTypeAdapter(OffsetDateTimeAdapter.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	protected OffsetDateTime creationDateAndTime;
}
