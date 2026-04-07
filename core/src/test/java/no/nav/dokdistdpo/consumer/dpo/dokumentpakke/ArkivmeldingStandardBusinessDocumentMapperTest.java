package no.nav.dokdistdpo.consumer.dpo.dokumentpakke;

import no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.Arkivmelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.Partner;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.PartnerIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.Scope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.ScopeType;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.StandardBusinessDocumentHeader;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.ArkivmeldingStandardBusinessDocumentMapper;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.AvtaltStandardBusinessDocumentMapper;
import org.junit.jupiter.api.Test;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_DOCUMENT_IDENTIFICATOR;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.MESSAGE_CHANNEL_INSTANCE_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType.DPO_ARKIVMELDING;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_ORG_AUTHORITY;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.ArkivmeldingStandardBusinessDocumentMapper.DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.AvtaltStandardBusinessDocumentMapper.SCOPE_MESSAGECHANELL_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.AvtaltStandardBusinessDocumentMapper.TYPE_VERSION;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.BESTILLINGS_ID;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.CONVERSATION_ID;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.MOTTAKER_ID;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.createForsendelse;
import static org.assertj.core.api.Assertions.assertThat;

class ArkivmeldingStandardBusinessDocumentMapperTest {

	private final ArkivmeldingStandardBusinessDocumentMapper mapper = new ArkivmeldingStandardBusinessDocumentMapper();


	@Test
	void shouldMapDpoArkivmelding() {

		StandardBusinessDocument sbd = mapper.mapArkivmeldingEnvelope(createForsendelse(DPO_ARKIVMELDING));

		StandardBusinessDocumentHeader sbdh = sbd.getStandardBusinessDocumentHeader();

		assertThat(sbdh.getHeaderVersion()).isEqualTo(AvtaltStandardBusinessDocumentMapper.HEADER_VERSION);

		assertThat(sbdh.getSender()).extracting(Partner::getIdentifier)
				.extracting(PartnerIdentification::getAuthority).contains(ISO6523_ORG_AUTHORITY);
		assertThat(sbdh.getSender()).extracting(Partner::getIdentifier)
				.extracting(PartnerIdentification::getValue).contains(Organisasjonsnummer.asIso6523(NAV_ORGNUMMER));

		assertThat(sbdh.getReceiver()).extracting(Partner::getIdentifier)
				.extracting(PartnerIdentification::getAuthority).contains(ISO6523_ORG_AUTHORITY);
		assertThat(sbdh.getReceiver()).extracting(Partner::getIdentifier)
				.extracting(PartnerIdentification::getValue)
				.contains(Organisasjonsnummer.asIso6523(MOTTAKER_ID));

		assertThat(sbdh.getDocumentIdentification().getTypeVersion()).isEqualTo(TYPE_VERSION);
		assertThat(sbdh.getDocumentIdentification().getStandard()).isEqualTo(ARKIVMELDING_DOCUMENT_IDENTIFICATOR);
		assertThat(sbdh.getDocumentIdentification().getInstanceIdentifier()).isEqualTo(BESTILLINGS_ID.toString());
		assertThat(sbdh.getDocumentIdentification().getType()).isEqualTo(DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING);
		assertThat(sbdh.getDocumentIdentification().getMultipleType()).isTrue();

		assertThat(sbdh.getBusinessScope().getScope()).hasSize(2);
		assertThat(sbdh.getBusinessScope().getScope()).anyMatch(ScopeType.CONVERSATION_ID)
				.flatExtracting(Scope::getInstanceIdentifier, Scope::getIdentifier)
				.contains(CONVERSATION_ID.toString(), ARKIVMELDING_PROCESS_IDENTIFIER);

		assertThat(sbdh.getBusinessScope().getScope())
				.anyMatch(ScopeType.MESSAGE_CHANNEL)
				.flatExtracting(Scope::getInstanceIdentifier, Scope::getIdentifier)
				.contains(MESSAGE_CHANNEL_INSTANCE_IDENTIFIER.toString(), SCOPE_MESSAGECHANELL_IDENTIFIER);

		assertThat(sbd.getAny()).isInstanceOf(Arkivmelding.class);
	}

}