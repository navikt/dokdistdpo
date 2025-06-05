package no.nav.dokdistdpo.consumer.serviceregistry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class DpoMottakerInfoService {

	private final ServiceRegistryConsumer serviceRegistryConsumer;

	public DpoMottakerInfoService(ServiceRegistryConsumer serviceRegistryConsumer) {
		this.serviceRegistryConsumer = serviceRegistryConsumer;
	}

	public DpoMottakerInfo hentMottakerInfo(ServiceRegistryRequest serviceRegistryRequest) {
		final IdentifierResource identifierResource = serviceRegistryConsumer.getIdentifierResource(serviceRegistryRequest.mottakerId(), serviceRegistryRequest.processIdentifier());

		if (identifierResource == null || isEmpty(identifierResource.serviceRecords())) {
			return null;
		}
		log.info("Henter mottakerinfo fra service registry for mottakerId={} og processIdentifier={}",
				serviceRegistryRequest.mottakerId(), serviceRegistryRequest.processIdentifier());

		final Optional<IdentifierResource.ServiceRecord> anyServiceRecord = identifierResource.findServiceRecord(serviceRegistryRequest.processIdentifier());
		IdentifierResource.ServiceRecord serviceRecord = anyServiceRecord.orElse(null);

		if (isServiceRecordNull(serviceRecord) || isServiceNull(serviceRecord.service())) {
			return null;
		}

		return new DpoMottakerInfo(serviceRecord.organisationNumber(), serviceRecord.pemCertificate(),
				serviceRecord.service().serviceCode(), serviceRecord.service().serviceEditionCode());
	}

	boolean isServiceRecordNull(IdentifierResource.ServiceRecord serviceRecord) {
		return serviceRecord == null || serviceRecord.service() == null || serviceRecord.pemCertificate() == null;
	}

	boolean isServiceNull(IdentifierResource.Service service) {
		return service == null || service.serviceCode() == null || service.serviceEditionCode() == null;
	}
}
