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

import java.time.OffsetDateTime;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CorrelationInformation", propOrder = {
		"expectedResponseDateTime"
})
@Data
@Builder
public class CorrelationInformation {

	@XmlElement(name = "ExpectedResponseDateTime")
	@XmlSchemaType(name = "dateTime")
	@XmlJavaTypeAdapter(OffsetDateTimeAdapter.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	protected OffsetDateTime expectedResponseDateTime;
}
