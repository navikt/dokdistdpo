package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.manifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hoveddokument")
@XmlRootElement(name = "hoveddokument")
public class HovedDokument {

	@XmlAttribute
	private String href;

	@XmlAttribute
	private String mime;

	@XmlElement
	private Tittel tittel;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@XmlType(name = "tittel")
	@XmlRootElement(name = "tittel")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Tittel {
		@XmlValue
		private String tittel;

		@XmlAttribute
		private String lang;
	}
}
