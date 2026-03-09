package no.nav.dokdistdpo.consumer.dpo.dokumentpakke;

import no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.AvtaltMelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.Partner;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.PartnerIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.Scope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.ScopeType;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain.StandardBusinessDocumentHeader;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.AvtaltStandardBusinessDocumentMapper;
import org.junit.jupiter.api.Test;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_DOCUMENT_IDENTIFICATOR;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.MESSAGE_CHANNEL_INSTANCE_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType.DPO_AVTALEMELDING;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_AUTHORITY;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.AvtaltStandardBusinessDocumentMapper.DOKUMENTIDENTIFICATION_TYPE_AVTALTMELDING;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.AvtaltStandardBusinessDocumentMapper.SCOPE_MESSAGECHANELL_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.map.AvtaltStandardBusinessDocumentMapper.TYPE_VERSION;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.BESTILLINGS_ID;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.CONVERSATION_ID;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.MOTTAKER_ID;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.createForsendelse;
import static org.assertj.core.api.Assertions.assertThat;

class AvtaltStandardBusinessDocumentMapperTest {

	private final AvtaltStandardBusinessDocumentMapper mapper = new AvtaltStandardBusinessDocumentMapper();

	@Test
	void shouldMapDpoAvtaltmelding() {

		StandardBusinessDocument sbd = mapper.mapAvtaltMeldingEnvelope(createForsendelse(DPO_AVTALEMELDING));

		StandardBusinessDocumentHeader sbdh = sbd.getStandardBusinessDocumentHeader();

		assertThat(sbdh.getHeaderVersion()).isEqualTo(AvtaltStandardBusinessDocumentMapper.HEADER_VERSION);

		assertThat(sbdh.getSender()).extracting(Partner::getIdentifier)
				.extracting(PartnerIdentification::getAuthority).contains(ISO6523_AUTHORITY);
		assertThat(sbdh.getSender()).extracting(Partner::getIdentifier)
				.extracting(PartnerIdentification::getValue).contains(Organisasjonsnummer.asIso6523(NAV_ORGNUMMER));

		assertThat(sbdh.getReceiver()).extracting(Partner::getIdentifier)
				.extracting(PartnerIdentification::getAuthority).contains(ISO6523_AUTHORITY);
		assertThat(sbdh.getReceiver()).extracting(Partner::getIdentifier)
				.extracting(PartnerIdentification::getValue)
				.contains(Organisasjonsnummer.asIso6523(MOTTAKER_ID));

		assertThat(sbdh.getDocumentIdentification().getTypeVersion()).isEqualTo(TYPE_VERSION);
		assertThat(sbdh.getDocumentIdentification().getStandard()).isEqualTo(AVTALTMELDING_DOCUMENT_IDENTIFICATOR);
		assertThat(sbdh.getDocumentIdentification().getInstanceIdentifier()).isEqualTo(BESTILLINGS_ID.toString());
		assertThat(sbdh.getDocumentIdentification().getType()).isEqualTo(DOKUMENTIDENTIFICATION_TYPE_AVTALTMELDING);
		assertThat(sbdh.getDocumentIdentification().getMultipleType()).isTrue();

		assertThat(sbdh.getBusinessScope().getScope()).hasSize(2);
		assertThat(sbdh.getBusinessScope().getScope()).anyMatch(ScopeType.CONVERSATION_ID)
				.flatExtracting(Scope::getInstanceIdentifier, Scope::getIdentifier)
				.contains(CONVERSATION_ID.toString(), AVTALTMELDING_PROCESS_IDENTIFIER);

		assertThat(sbdh.getBusinessScope().getScope())
				.anyMatch(ScopeType.MESSAGE_CHANNEL)
				.flatExtracting(Scope::getInstanceIdentifier, Scope::getIdentifier)
				.contains(MESSAGE_CHANNEL_INSTANCE_IDENTIFIER.toString(), SCOPE_MESSAGECHANELL_IDENTIFIER);

		assertThat(sbd.getAny()).isInstanceOf(AvtaltMelding.class);
	}

}