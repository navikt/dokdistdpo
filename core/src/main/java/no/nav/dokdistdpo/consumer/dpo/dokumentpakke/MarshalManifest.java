package no.nav.dokdistdpo.consumer.dpo.dokumentpakke;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.manifest.Manifest;
import no.nav.dokdistdpo.exception.functional.DokumentpakkingException;

import java.io.OutputStream;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

@Slf4j
public class MarshalManifest {

	private static final JAXBContext JAXB_CONTEXT;

	static {

		try {
			JAXB_CONTEXT = JAXBContext.newInstance(Manifest.class);
		} catch (JAXBException e) {
			throw new IllegalStateException("Klarte ikke sette opp JAXBContext", e);
		}
	}

	static void marshal(Manifest doc, OutputStream outputStream) {
		try {

			Marshaller jaxbMarshaller = JAXB_CONTEXT.createMarshaller();
			jaxbMarshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(doc, outputStream);
		} catch (JAXBException e) {
			throw new DokumentpakkingException("Klarte ikke å marshalle Manifest", e);
		}
	}
}
