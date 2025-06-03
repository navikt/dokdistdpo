package no.nav.dokdistdpo.consumer.serviceregistry;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;

@Builder
public record IdentifierResource(InfoRecord infoRecord, List<ServiceRecord> serviceRecords) {

	@Builder
	public record InfoRecord(String identifier, String organizationName) {
	}

	@Builder
	public record ServiceRecord(String organisationNumber,
								String pemCertificate,
								String process,
								List<String> documentTypes,
								Service service) {
	}

	@Builder
	public record Service(ServiceIdentifier identifier,
						  String endpointUrl,
						  String serviceCode,
						  String serviceEditionCode,
						  Integer securityLevel) {

	}

	@Getter
	@RequiredArgsConstructor
	public enum ServiceIdentifier {
		DPO("DPO"),
		DPV("DPV");

		private final String fullname;
	}

	Optional<ServiceRecord> findServiceRecord(String process, ServiceIdentifier serviceIdentifier) {
		return isEmpty(serviceRecords) ? Optional.empty() : serviceRecords.stream()
				.filter(serviceRecord -> process.equals(serviceRecord.process))
				.filter(serviceRecord -> serviceIdentifier == serviceRecord.service.identifier)
				.findAny();
	}
}
