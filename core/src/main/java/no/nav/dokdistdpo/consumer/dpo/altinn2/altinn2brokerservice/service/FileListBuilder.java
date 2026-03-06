package no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.service;

import jakarta.xml.bind.JAXBElement;
import no.altinn.brokerserviceexternal.ArrayOfFile;
import no.altinn.brokerserviceexternal.File;
import no.altinn.brokerserviceexternal.ObjectFactory;

public class FileListBuilder {

	String filename;

	public FileListBuilder withFilename(String filename) {
		this.filename = filename;
		return this;
	}

	public JAXBElement<ArrayOfFile> build() {
		ObjectFactory objectFactory = new ObjectFactory();
		ArrayOfFile arrayOfFile = new ArrayOfFile();
		File file = new File();
		file.setFileName(filename);
		arrayOfFile.getFile().add(file);
		return objectFactory.createArrayOfFile(arrayOfFile);
	}
}
