package no.nav.dokdistdpo.consumer.dpo.dokumentpakke;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType;
import no.nav.dokdistdpo.consumer.dpo.DokumentMeldingTypeInfo;
import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.AvtaltMelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.BusinessScope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.CorrelationInformation;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.DocumentIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.Partner;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.PartnerIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.Scope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocumentHeader;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static java.time.Duration.ofDays;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_DOCUMENT_IDENTIFICATOR;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_DOCUMENT_IDENTIFICATOR;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_XML;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SCOPE_CONVERSATION_ID_ARKIVMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_AUTHORITY;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.asIso6523;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType.CONVERSATION_ID;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType.MESSAGE_CHANNEL;

public class StandardBusinessDocumentMapper {

	public static final String HEADER_VERSION = "1.0";
	static final String TYPE_VERSION = "1.0";
	static final String ARKIVMELDING_TYPE_VERSION = "2.0";
	public static final UUID MESSAGE_CHANNEL_INSTANCE_IDENTIFIER = UUID.randomUUID();
	static final String SCOPE_MESSAGECHANELL_IDENTIFIER = "dokdistdpo";
	static final String DOKUMENTIDENTIFICATION_TYPE_AVTALTMELDING = "avtalt";
	static final String DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING = "arkivmelding";
	public static final int SIKKERHETSNIVAA = 4;
	public static final Duration EXPECTED_RESPONSE_WITHIN_HOURS = ofDays(10);


	public StandardBusinessDocument mapDpoMeldingEnvelope(NavDokumentpakke navDokumentpakke,
														  String dpoMelding) {

		var newSbdhRequest = getSBDHRequest(navDokumentpakke.meldingType());

		BusinessScope businessScope = BusinessScope.builder()
				.scope(Set.of(createConversationIdScope(navDokumentpakke.conversationId(), newSbdhRequest.processIdentification()),
						createMessageChannelScope()))
				.build();
		StandardBusinessDocumentHeader sbdh = StandardBusinessDocumentHeader.builder()
				.headerVersion(HEADER_VERSION)
				.documentIdentification(createDocumentIdentification(newSbdhRequest, navDokumentpakke.bestillingsId()))
				.businessScope(businessScope)
				.build();

		sbdh.addReceiver(createReceiver(navDokumentpakke.mottakerId()));
		sbdh.addSender(createSender());

		return StandardBusinessDocument.builder()
				.standardBusinessDocumentHeader(sbdh)
				.any(createSBDObject(navDokumentpakke.meldingType(), dpoMelding))
				.build();
	}

	private DocumentIdentification createDocumentIdentification(DokumentMeldingTypeInfo dokumentMeldingTypeInfo, String bestillingsId) {
		return DocumentIdentification.builder()
				.standard(dokumentMeldingTypeInfo.documentProcessIdentification())
				.typeVersion(dokumentMeldingTypeInfo.typeVersion())
				.instanceIdentifier(bestillingsId)
				.type(dokumentMeldingTypeInfo.type())
				.multipleType(true)
				.creationDateAndTime(OffsetDateTime.now().minusSeconds(10))
				.build();
	}

	private Partner createSender() {
		PartnerIdentification identification = PartnerIdentification.builder()
				.authority(ISO6523_AUTHORITY)
				.value(asIso6523(NAV_ORGNUMMER))
				.build();
		return Partner.builder()
				.identifier(identification)
				.build();
	}

	private Partner createReceiver(String mottakerId) {
		PartnerIdentification identification = PartnerIdentification.builder()
				.authority(ISO6523_AUTHORITY)
				.value(asIso6523(mottakerId))
				.build();
		return Partner.builder()
				.identifier(identification)
				.build();
	}

	private Scope createConversationIdScope(final String conversationId, final String processTdentifier) {
		CorrelationInformation correlationInformation = CorrelationInformation.builder()
				.expectedResponseDateTime(OffsetDateTime.now().plus(EXPECTED_RESPONSE_WITHIN_HOURS))
				.build();

		Scope conversationIdScope = Scope.builder()
				.type(CONVERSATION_ID.name())
				.instanceIdentifier(conversationId)
				.identifier(processTdentifier)
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

	private DokumentMeldingTypeInfo getSBDHRequest(ForsendelseMetadataType meldingType) {
		return switch (meldingType) {
			case DPO_ARKIVMELDING -> mapArkivmelding();

			case DPO_AVTALEMELDING -> mapSBDHAvtaltmelding();
		};
	}

	private DokumentMeldingTypeInfo mapSBDHAvtaltmelding() {
		return DokumentMeldingTypeInfo.builder()
				.typeVersion(TYPE_VERSION)
				.type(DOKUMENTIDENTIFICATION_TYPE_AVTALTMELDING)
				.documentProcessIdentification(AVTALTMELDING_DOCUMENT_IDENTIFICATOR)
				.processIdentification(SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER)
				.build();
	}

	private DokumentMeldingTypeInfo mapArkivmelding() {
		return DokumentMeldingTypeInfo.builder()
				.typeVersion(ARKIVMELDING_TYPE_VERSION)
				.type(DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING)
				.documentProcessIdentification(ARKIVMELDING_DOCUMENT_IDENTIFICATOR)
				.processIdentification(SCOPE_CONVERSATION_ID_ARKIVMELDING_PROCESS_IDENTIFIER)
				.build();
	}

	private Object createSBDObject(final ForsendelseMetadataType metadataType, String dpoMelding) {
		return switch (metadataType) {
			case DPO_ARKIVMELDING -> dpoMelding;

			case DPO_AVTALEMELDING -> createAvtaltMelding(dpoMelding);
		};
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
