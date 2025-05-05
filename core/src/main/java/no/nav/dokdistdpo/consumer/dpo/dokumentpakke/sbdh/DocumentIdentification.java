package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class DocumentIdentification {
	private String standard;
	String typeVersion;
	String instanceIdentifier;
	String type;
	Boolean multipleType;
	OffsetDateTime creationDateAndTime;
}
