package no.nav.dokdistdpo.consumer.serviceregistry;

import org.springframework.stereotype.Component;

import java.util.Optional;

import static no.nav.dokdistdpo.consumer.serviceregistry.IdentifierResource.ServiceIdentifier.DPO;

@Component
public class DpoMottakerInfoService {

	private final ServiceRegistryConsumer serviceRegistryConsumer;

	public DpoMottakerInfoService(ServiceRegistryConsumer serviceRegistryConsumer) {
		this.serviceRegistryConsumer = serviceRegistryConsumer;
	}

	public DpoMottakerInfo hentMottakerInfo(ServiceRegistryRequest serviceRegistryRequest) {
		final IdentifierResource identifierResource = serviceRegistryConsumer.getIdentifierResource(serviceRegistryRequest.mottakerId(), serviceRegistryRequest.processIdentifier());

		if (identifierResource == null) {
			return null;
		}

		final Optional<IdentifierResource.ServiceRecord> anyServiceRecord = identifierResource.findServiceRecord(serviceRegistryRequest.processIdentifier(), DPO);
		IdentifierResource.ServiceRecord serviceRecord = anyServiceRecord.get();

		final IdentifierResource.Service service = serviceRecord.service();

		return new DpoMottakerInfo(serviceRecord.organisationNumber(), serviceRecord.pemCertificate(), service.serviceCode(), service.serviceEditionCode());
	}
}
