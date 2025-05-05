package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactInformation {
	private String contact;
	private String emailAddress;
	private String faxNumber;
	private String telephoneNumber;
	private String contactTypeIdentifier;
}
