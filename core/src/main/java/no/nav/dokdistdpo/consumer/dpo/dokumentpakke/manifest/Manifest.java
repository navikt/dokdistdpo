package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.manifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Manifest", propOrder = {"mottaker", "avsender", "hoveddokument",})
@XmlRootElement(name = "manifest")
public class Manifest {
	@XmlElement(required = true)
	Mottaker mottaker;

	@XmlElement(required = true)
	Avsender avsender;

	@XmlElement(required = true)
	HovedDokument hoveddokument;
}
