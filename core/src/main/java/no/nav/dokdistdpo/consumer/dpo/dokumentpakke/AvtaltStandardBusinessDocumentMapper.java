package no.nav.dokdistdpo.consumer.dpo.dokumentpakke;

import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.AvtaltMelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.BusinessScope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.CorrelationInformation;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.DocumentIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.PartnerIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.Receiver;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.Scope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.Sender;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocumentHeader;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;

import static java.time.Duration.ofDays;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_XML;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_DOCUMENT_IDENTIFICATOR;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.MESSAGE_CHANNEL_INSTANCE_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.asIso6523;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType.CONVERSATION_ID;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType.MESSAGE_CHANNEL;
import static no.nav.dokdistdpo.consumer.dpo.maskinporten.Authority.ISO_6523_ACTORID_UPIS;

public class AvtaltStandardBusinessDocumentMapper {

	public static final String HEADER_VERSION = "1.0";
	static final String TYPE_VERSION = "1.0";
	static final String SCOPE_MESSAGECHANELL_IDENTIFIER = "dokdistdpo";
	static final String DOKUMENTIDENTIFICATION_TYPE_AVTALTMELDING = "avtalt";
	public static final int SIKKERHETSNIVAA = 4;
	public static final Duration EXPECTED_RESPONSE_WITHIN_HOURS = ofDays(10);


	public StandardBusinessDocument mapAvtaltMeldingEnvelope(AltinnDpoRequest.Forsendelse forsendelse) {
		BusinessScope businessScope = BusinessScope.builder()
				.scope(Set.of(createAvtaltConversationIdScope(forsendelse.konversjonsId()),
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
				.any(createAvtaltMelding(forsendelse.forsendelseMetadata()))
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
				.type(CONVERSATION_ID.getFullname())
				.instanceIdentifier(conversationId)
				.identifier(AVTALTMELDING_PROCESS_IDENTIFIER)
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

	private AvtaltMelding createAvtaltMelding(String avtaltmelding) {
		final AvtaltMelding avtaltMelding = new AvtaltMelding();
		avtaltMelding.setIdentifier(AVTALTMELDING_PROCESS_IDENTIFIER);
		avtaltMelding.setSikkerhetsnivaa(SIKKERHETSNIVAA);
		avtaltMelding.setHoveddokument(ARKIVMELDING_XML);
		avtaltMelding.setContent(avtaltmelding);
		return avtaltMelding;
	}
}
