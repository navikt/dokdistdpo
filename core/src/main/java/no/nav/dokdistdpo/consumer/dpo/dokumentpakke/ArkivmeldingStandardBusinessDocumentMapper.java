package no.nav.dokdistdpo.consumer.dpo.dokumentpakke;

import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.Arkivmelding;
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
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_DOCUMENT_IDENTIFICATOR;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_XML;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.MESSAGE_CHANNEL_INSTANCE_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.AvtaltStandardBusinessDocumentMapper.SIKKERHETSNIVAA;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.AvtaltStandardBusinessDocumentMapper.TYPE_VERSION;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType.CONVERSATION_ID;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType.MESSAGE_CHANNEL;

public class ArkivmeldingStandardBusinessDocumentMapper {

	public static final String HEADER_VERSION = "1.0";
	static final String SCOPE_MESSAGECHANELL_IDENTIFIER = "dokdistdpo";
	static final String DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING = "arkivmelding";
	public static final Duration EXPECTED_RESPONSE_WITHIN_HOURS = ofDays(10);

	public StandardBusinessDocument mapArkivmeldingEnvelope(AltinnDpoRequest.Forsendelse forsendelse) {

		BusinessScope businessScope = BusinessScope.builder()
				.scope(Set.of(createConversationIdScope(forsendelse.konversjonsId()),
						createMessageChannelScope()))
				.build();
		StandardBusinessDocumentHeader sbdh = StandardBusinessDocumentHeader.builder()
				.headerVersion(HEADER_VERSION)
				.documentIdentification(createDocumentIdentification(forsendelse.bestillingsId()))
				.businessScope(businessScope)
				.build();

		sbdh.addReceiver(sbdh.createPartner(forsendelse.mottakerId()));
		sbdh.addSender(sbdh.createPartner(NAV_ORGNUMMER));

		return StandardBusinessDocument.builder()
				.standardBusinessDocumentHeader(sbdh)
				.any(createArkivmelding(forsendelse.forsendelseMetadata()))
				.build();
	}

	private DocumentIdentification createDocumentIdentification(String bestillingsId) {
		return DocumentIdentification.builder()
				.standard(ARKIVMELDING_DOCUMENT_IDENTIFICATOR)
				.typeVersion(TYPE_VERSION)
				.instanceIdentifier(bestillingsId)
				.type(DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING)
				.multipleType(true)
				.creationDateAndTime(OffsetDateTime.now().minusSeconds(10))
				.build();
	}

	private Scope createConversationIdScope(final String conversationId) {
		CorrelationInformation correlationInformation = CorrelationInformation.builder()
				.expectedResponseDateTime(OffsetDateTime.now().plus(EXPECTED_RESPONSE_WITHIN_HOURS))
				.build();

		Scope conversationIdScope = Scope.builder()
				.type(CONVERSATION_ID.name())
				.instanceIdentifier(conversationId)
				.identifier(ARKIVMELDING_PROCESS_IDENTIFIER)
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

	private Arkivmelding createArkivmelding(String dpoMelding) {
		final Arkivmelding arkivmelding = new Arkivmelding();
		arkivmelding.setIdentifier(ARKIVMELDING_PROCESS_IDENTIFIER);
		arkivmelding.setSikkerhetsnivaa(SIKKERHETSNIVAA);
		arkivmelding.setHoveddokument(AVTALTMELDING_XML);
		arkivmelding.setContent(dpoMelding);
		return arkivmelding;
	}
}
