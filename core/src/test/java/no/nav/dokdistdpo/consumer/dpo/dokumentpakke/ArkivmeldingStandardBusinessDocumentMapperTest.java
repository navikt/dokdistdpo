package no.nav.dokdistdpo.consumer.dpo.dokumentpakke;

import no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.arkivmelding.ArkivMelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.arkivmelding.ArkivmeldingStandardBusinessDocumentMapper;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.AvtaltStandardBusinessDocumentMapper;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.Partner;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.PartnerIdentification;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.Scope;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.ScopeType;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.StandardBusinessDocumentHeader;
import org.junit.jupiter.api.Test;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_DOCUMENT_IDENTIFICATOR;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.MESSAGE_CHANNEL_INSTANCE_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SCOPE_CONVERSATION_ID_ARKIVMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType.DPO_ARKIVMELDING;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_AUTHORITY;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.arkivmelding.ArkivmeldingStandardBusinessDocumentMapper.ARKIVMELDING_TYPE_VERSION;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.arkivmelding.ArkivmeldingStandardBusinessDocumentMapper.DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING;
import static no.nav.dokdistdpo.consumer.dpo.dokumentpakke.arkivmelding.ArkivmeldingStandardBusinessDocumentMapper.SCOPE_MESSAGECHANELL_IDENTIFIER;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.ARKIVMELDING_XML;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.BESTILLINGS_ID;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.CONVERSATION_ID;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.MOTTAKER_ID;
import static no.nav.dokdistdpo.consumer.dpo.testutils.TestUtils.createNavDokumentpakke;
import static org.assertj.core.api.Assertions.assertThat;

class ArkivmeldingStandardBusinessDocumentMapperTest {

	private ArkivmeldingStandardBusinessDocumentMapper mapper = new ArkivmeldingStandardBusinessDocumentMapper();


	@Test
	void shouldMapDpoArkivmelding() {

		StandardBusinessDocument sbd = mapper.mapArkivmeldingEnvelope(createNavDokumentpakke(DPO_ARKIVMELDING), ARKIVMELDING_XML);

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

		assertThat(sbdh.getDocumentIdentification().getTypeVersion()).isEqualTo(ARKIVMELDING_TYPE_VERSION);
		assertThat(sbdh.getDocumentIdentification().getStandard()).isEqualTo(ARKIVMELDING_DOCUMENT_IDENTIFICATOR);
		assertThat(sbdh.getDocumentIdentification().getInstanceIdentifier()).isEqualTo(BESTILLINGS_ID.toString());
		assertThat(sbdh.getDocumentIdentification().getType()).isEqualTo(DOKUMENTIDENTIFICATION_TYPE_ARKIVMELDING);
		assertThat(sbdh.getDocumentIdentification().getMultipleType()).isTrue();

		assertThat(sbdh.getBusinessScope().getScope()).hasSize(2);
		assertThat(sbdh.getBusinessScope().getScope()).anyMatch(ScopeType.CONVERSATION_ID)
				.flatExtracting(Scope::getInstanceIdentifier, Scope::getIdentifier)
				.contains(CONVERSATION_ID.toString(), SCOPE_CONVERSATION_ID_ARKIVMELDING_PROCESS_IDENTIFIER);

		assertThat(sbdh.getBusinessScope().getScope())
				.anyMatch(ScopeType.MESSAGE_CHANNEL)
				.flatExtracting(Scope::getInstanceIdentifier, Scope::getIdentifier)
				.contains(MESSAGE_CHANNEL_INSTANCE_IDENTIFIER.toString(), SCOPE_MESSAGECHANELL_IDENTIFIER);

		assertThat(sbd.getAny()).isInstanceOf(ArkivMelding.class);
	}

}