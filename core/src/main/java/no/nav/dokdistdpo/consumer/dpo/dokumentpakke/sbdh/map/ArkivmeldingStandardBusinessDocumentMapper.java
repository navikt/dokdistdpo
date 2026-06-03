package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map;

import no.nav.dokdistdpo.consumer.dpo.altinn3.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.Arkivmelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.BusinessScope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.CorrelationInformation;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.DocumentIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.PartnerIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.Receiver;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.Scope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.Sender;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.StandardBusinessDocumentHeader;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;

import static java.time.Duration.ofDays;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_DOCUMENT_IDENTIFICATOR;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_XML;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.MESSAGE_CHANNEL_INSTANCE_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.asIso6523;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.AvtaltStandardBusinessDocumentMapper.SIKKERHETSNIVAA;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.AvtaltStandardBusinessDocumentMapper.TYPE_VERSION;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.ScopeType.CONVERSATION_ID;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.ScopeType.MESSAGE_CHANNEL;
import static no.nav.dokdistdpo.consumer.dpo.maskinporten.Authority.ISO_6523_ACTORID_UPIS;

public class ArkivmeldingStandardBusinessDocumentMapper {

	public static final String HEADER_VERSION = "1.0";
	public static final String SCOPE_MESSAGECHANELL_IDENTIFIER = "dokdistdpo";
	public static final String DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING = "arkivmelding";
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

		sbdh.addSender(createSender());
		sbdh.addReceiver(createReceiver(forsendelse.mottakerId()));

		return StandardBusinessDocument.builder()
				.standardBusinessDocumentHeader(sbdh)
				.any(createArkivmelding(forsendelse.forsendelseMetadata()))
				.build();
	}

	private Receiver createReceiver(String mottakerOrgnummer) {
		final Receiver receiver = new Receiver();
		final PartnerIdentification receiverIdentification = new PartnerIdentification();
		receiverIdentification.setAuthority(ISO_6523_ACTORID_UPIS.getValue());
		receiverIdentification.setValue(asIso6523(mottakerOrgnummer));
		receiver.setIdentifier(receiverIdentification);
		return receiver;
	}

	private Sender createSender() {
		final Sender sender = new Sender();
		final PartnerIdentification receiverIdentification = new PartnerIdentification();
		receiverIdentification.setAuthority(ISO_6523_ACTORID_UPIS.getValue());
		receiverIdentification.setValue(asIso6523(NAV_ORGNUMMER));
		sender.setIdentifier(receiverIdentification);
		return sender;
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
				.type(CONVERSATION_ID.getFullname())
				.instanceIdentifier(conversationId)
				.identifier(ARKIVMELDING_PROCESS_IDENTIFIER)
				.build();
		conversationIdScope.addScopeInformation(correlationInformation);

		return conversationIdScope;
	}

	private Scope createMessageChannelScope() {
		return Scope.builder()
				.type(MESSAGE_CHANNEL.getFullname())
				.instanceIdentifier(MESSAGE_CHANNEL_INSTANCE_IDENTIFIER.toString())
				.identifier(SCOPE_MESSAGECHANELL_IDENTIFIER)
				.build();
	}

	private Arkivmelding createArkivmelding(String dpoMelding) {
		final Arkivmelding arkivmelding = new Arkivmelding();
		arkivmelding.setIdentifier(ARKIVMELDING_PROCESS_IDENTIFIER);
		arkivmelding.setSikkerhetsnivaa(SIKKERHETSNIVAA);
		arkivmelding.setHoveddokument(ARKIVMELDING_XML);
		arkivmelding.setContent(dpoMelding);
		return arkivmelding;
	}
}
