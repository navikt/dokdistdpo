package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class CorrelationInformation {
	private OffsetDateTime requestingDocumentCreationDateTime;
	private String requestingDocumentInstanceIdentifier;
	private OffsetDateTime expectedResponseDateTime;
}
