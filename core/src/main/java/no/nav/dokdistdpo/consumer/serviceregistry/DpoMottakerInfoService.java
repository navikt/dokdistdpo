package no.nav.dokdistdpo.consumer.serviceregistry;

import no.nav.dokdistdpo.exception.functional.MottakerInfoIkkeFunnetException;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.lang.String.format;
import static no.nav.dokdistdpo.consumer.serviceregistry.IdentifierResource.ServiceIdentifier.DPO;

@Component
public class DpoMottakerInfoService {

	private final ServiceRegistryConsumer serviceRegistryConsumer;

	public DpoMottakerInfoService(ServiceRegistryConsumer serviceRegistryConsumer) {
		this.serviceRegistryConsumer = serviceRegistryConsumer;
	}

	public RegistryMottakerInfo hentMottakerInfo(ServiceRegistryRequest serviceRegistryRequest) {
		final IdentifierResource identifierResource = serviceRegistryConsumer.getIdentifierResource(serviceRegistryRequest.mottakerId(), serviceRegistryRequest.processIdentifier())
				.orElse(null);

		if (identifierResource == null) {
			return null;
		}

		final Optional<IdentifierResource.ServiceRecord> anyServiceRecord = identifierResource.findServiceRecord(serviceRegistryRequest.processIdentifier(), DPO);

		IdentifierResource.ServiceRecord serviceRecord = anyServiceRecord
				.orElseThrow(() -> new MottakerInfoIkkeFunnetException(format("Fant ikke mottakerinfo for organisasjon=%s og process=%s",
						serviceRegistryRequest.mottakerId(),  serviceRegistryRequest.processIdentifier())));

		final IdentifierResource.Service service= serviceRecord.service();

		return new RegistryMottakerInfo(serviceRecord.organisationNumber(), serviceRecord.pemCertificate(), service.serviceCode(), service.serviceEditionCode());
	}
}
