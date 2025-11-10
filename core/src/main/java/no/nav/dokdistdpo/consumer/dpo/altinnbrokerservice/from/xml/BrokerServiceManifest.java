package no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.from.xml;



import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"externalServiceCode",
		"externalServiceEditionCode",
		"sendersReference",
		"reportee",
		"sentDate",
		"fileList",
		"propertyList"
})
@XmlRootElement(name = "BrokerServiceManifest")
@Data
public class BrokerServiceManifest {

	@XmlElement(name = "ExternalServiceCode", required = true)
	protected String externalServiceCode;
	@XmlElement(name = "ExternalServiceEditionCode", required = true)
	protected BigInteger externalServiceEditionCode;
	@XmlElement(name = "SendersReference", required = true)
	protected String sendersReference;
	@XmlElement(name = "Reportee", required = true)
	protected String reportee;
	@XmlElement(name = "SentDate")
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar sentDate;
	@XmlElement(name = "FileList")
	protected FileList fileList;
	@XmlElement(name = "PropertyList")
	protected PropertyList propertyList;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {
			"file"
	})
	public static class FileList {

		@XmlElement(name = "File")
		protected List<File> file;

		public List<File> getFile() {
			if (file == null) {
				file = new ArrayList<>();
			}
			return this.file;
		}

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = {
				"fileName",
				"checkSum"
		})
		@Data
		public static class File {

			@XmlElement(name = "FileName", required = true)
			protected String fileName;
			@XmlElement(name = "CheckSum")
			protected String checkSum;

		}

	}



	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {
			"property"
	})
	public static class PropertyList {

		@XmlElement(name = "Property")
		protected List<Property> property;

		public List<Property> getProperty() {
			if (property == null) {
				property = new ArrayList<>();
			}
			return this.property;
		}

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = {
				"propertyKey",
				"propertyValue"
		})
		@Data
		public static class Property {

			@XmlElement(name = "PropertyKey", required = true)
			protected String propertyKey;
			@XmlElement(name = "PropertyValue", required = true)
			protected String propertyValue;

		}
	}
}

