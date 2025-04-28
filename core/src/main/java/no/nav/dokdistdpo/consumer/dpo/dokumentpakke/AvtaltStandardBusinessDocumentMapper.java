package no.nav.dokdistdpo.consumer.dpo.dokumentpakke;

import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.AvtaltMelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.BusinessScope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.CorrelationInformation;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.DocumentIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.Scope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocumentHeader;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;

import static java.time.Duration.ofDays;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_DOCUMENT_IDENTIFICATOR;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_XML;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.MESSAGE_CHANNEL_INSTANCE_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType.CONVERSATION_ID;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType.MESSAGE_CHANNEL;

public class AvtaltStandardBusinessDocumentMapper {

	public static final String HEADER_VERSION = "1.0";
	static final String TYPE_VERSION = "1.0";
	static final String ARKIVMELDING_TYPE_VERSION = "2.0";
	static final String SCOPE_MESSAGECHANELL_IDENTIFIER = "dokdistdpo";
	static final String DOKUMENTIDENTIFICATION_TYPE_AVTALTMELDING = "avtalt";
	static final String DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING = "arkivmelding";
	public static final int SIKKERHETSNIVAA = 4;
	public static final Duration EXPECTED_RESPONSE_WITHIN_HOURS = ofDays(10);


	public StandardBusinessDocument mapAvtaltMeldingEnvelope(NavDokumentpakke navDokumentpakke,
															 String dpoMelding) {


		BusinessScope businessScope = BusinessScope.builder()
				.scope(Set.of(createAvtaltConversationIdScope(navDokumentpakke.conversationId()),
						createMessageChannelScope()))
				.build();
		StandardBusinessDocumentHeader sbdh = StandardBusinessDocumentHeader.builder()
				.headerVersion(HEADER_VERSION)
				.documentIdentification(createDocumentIdentification(navDokumentpakke.bestillingsId()))
				.businessScope(businessScope)
				.build();

		sbdh.addReceiver(sbdh.createPartner(navDokumentpakke.mottakerId()));
		sbdh.addSender(sbdh.createPartner(NAV_ORGNUMMER));

		return StandardBusinessDocument.builder()
				.standardBusinessDocumentHeader(sbdh)
				.any(createAvtaltMelding(dpoMelding))
				.build();
	}

	private DocumentIdentification createDocumentIdentification(String bestillingsId) {
		return DocumentIdentification.builder()
				.standard(AVTALTMELDING_DOCUMENT_IDENTIFICATOR)
				.typeVersion(TYPE_VERSION)
				.instanceIdentifier(bestillingsId)
				.type(DOKUMENTIDENTIFICATION_TYPE_AVTALTMELDING)
				.multipleType(true)
				.creationDateAndTime(OffsetDateTime.now().minusSeconds(10))
				.build();
	}

	private Scope createAvtaltConversationIdScope(final String conversationId) {
		CorrelationInformation correlationInformation = CorrelationInformation.builder()
				.expectedResponseDateTime(OffsetDateTime.now().plus(EXPECTED_RESPONSE_WITHIN_HOURS))
				.build();

		Scope conversationIdScope = Scope.builder()
				.type(CONVERSATION_ID.name())
				.instanceIdentifier(conversationId)
				.identifier(SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER)
				.build();
		conversationIdScope.addScopeInformation(correlationInformation);

		return conversationIdScope;
	}

	private Scope createMessageChannelScope() {
		return Scope.builder()
				.type(MESSAGE_CHANNEL.name())
				.instanceIdentifier(MESSAGE_CHANNEL_INSTANCE_IDENTIFIER.toString())
				.identifier(SCOPE_MESSAGECHANELL_IDENTIFIER)
				.build();
	}

	private AvtaltMelding createAvtaltMelding(String avtaltmelding) {
		final AvtaltMelding avtaltMelding = new AvtaltMelding();
		avtaltMelding.setIdentifier(SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER);
		avtaltMelding.setSikkerhetsnivaa(SIKKERHETSNIVAA);
		avtaltMelding.setHoveddokument(AVTALTMELDING_XML);
		avtaltMelding.setContent(avtaltmelding);
		return avtaltMelding;
	}
}
