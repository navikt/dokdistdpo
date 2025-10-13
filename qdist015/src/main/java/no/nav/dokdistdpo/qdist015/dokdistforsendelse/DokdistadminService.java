package no.nav.dokdistdpo.qdist015.dokdistforsendelse;

import no.nav.dokdistdpo.consumer.dokdistadmin.DokdistAdminConsumer;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ForsendelseMetadataType;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dpo.serviceregistry.DpoMottakerInfo;
import no.nav.dokdistdpo.consumer.dpo.serviceregistry.DpoMottakerInfoService;
import no.nav.dokdistdpo.consumer.dpo.serviceregistry.ServiceRegistryRequest;
import org.springframework.stereotype.Component;

import static java.lang.Enum.valueOf;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.ARKIVMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_PROCESS_IDENTIFIER;

@Component
public class DokdistadminService {

	private final DokdistAdminConsumer dokdistAdminConsumer;
	private final DpoMottakerInfoService dpoMottakerInfoService;
	private final ForsendelseValidator forsendelseValidator;

	public DokdistadminService(DokdistAdminConsumer dokdistAdminConsumer,
							   DpoMottakerInfoService dpoMottakerInfoService) {
		this.dokdistAdminConsumer = dokdistAdminConsumer;
		this.dpoMottakerInfoService = dpoMottakerInfoService;
		this.forsendelseValidator = new ForsendelseValidator();
	}

	public HentForsendelseResponse hentForsendelse(Long forsendelseId) {
		HentForsendelseResponse hentForsendelseResponse = dokdistAdminConsumer.hentForsendelse(forsendelseId);
		forsendelseValidator.assertHentForsendelse(hentForsendelseResponse);
		return hentForsendelseResponse;
	}

	public DpoMottakerInfo hentServiceRegistryMottakerInfo(HentForsendelseResponse hentForsendelseResponse) {
		return dpoMottakerInfoService.hentMottakerInfo(mapToServiceRegistry(hentForsendelseResponse));
	}

	private ServiceRegistryRequest mapToServiceRegistry(HentForsendelseResponse hentForsendelseResponse) {
		return ServiceRegistryRequest.builder()
				.mottakerId(hentForsendelseResponse.mottaker().mottakerId())
				.processIdentifier(getProcessIdentifier(hentForsendelseResponse.forsendelseMetadataType()))
				.build();
	}

	private String getProcessIdentifier(String metadataType) {
		ForsendelseMetadataType forsendelseMetadataType = valueOf(ForsendelseMetadataType.class, metadataType);
		return switch (forsendelseMetadataType) {
			case DPO_ARKIVMELDING -> ARKIVMELDING_PROCESS_IDENTIFIER;
			case DPO_AVTALEMELDING -> AVTALTMELDING_PROCESS_IDENTIFIER;
		};
	}
}
