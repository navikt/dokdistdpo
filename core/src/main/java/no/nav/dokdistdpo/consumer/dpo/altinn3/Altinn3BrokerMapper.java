package no.nav.dokdistdpo.consumer.dpo.altinn3;

import lombok.extern.slf4j.Slf4j;
import no.altinn.services.altinn3.domain.FileTransferInitalizeExt;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.AltinnDpoRequest;

import java.util.List;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

@Slf4j
public class Altinn3BrokerMapper {

	private static final String FILE_NAME = "sbd.zip";
	private static final String ALTINN3_ORGANISASJON_IDENTIFIER = "urn:altinn:organization:identifier-no:";
	private static final String RESOURCE_ID = "eformidling-dpo-meldingsutveksling";

	public FileTransferInitalizeExt mapInitiateFileTransfer(AltinnDpoRequest altinnDpoRequest, byte[] sbdZip) {
		return FileTransferInitalizeExt.builder()
				.fileName(FILE_NAME)
				.resourceId(RESOURCE_ID)
				.sendersFileTransferReference(altinnDpoRequest.forsendelse().konversjonsId())
				.sender(mapOrganisasjonIdentifier(NAV_ORGNUMMER))
				.recipients(List.of(mapOrganisasjonIdentifier(altinnDpoRequest.dpoMottakerInfo().orgnummer())))
				.checksum(md5Hex(sbdZip))
				.disableVirusScan(false)
				.build();
	}

	private static String mapOrganisasjonIdentifier(String orgnummer) {
		return ALTINN3_ORGANISASJON_IDENTIFIER + orgnummer;
	}
}
