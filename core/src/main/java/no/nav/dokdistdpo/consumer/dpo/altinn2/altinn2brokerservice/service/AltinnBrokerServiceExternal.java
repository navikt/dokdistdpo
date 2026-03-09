package no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.service;

import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternal.ArrayOfRecipient;
import no.altinn.brokerserviceexternal.BrokerServiceAvailableFile;
import no.altinn.brokerserviceexternal.BrokerServiceAvailableFileList;
import no.altinn.brokerserviceexternal.BrokerServiceInitiation;
import no.altinn.brokerserviceexternal.BrokerServiceSearch;
import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalConfirmDownloadedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalGetAvailableFilesAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalInitiateBrokerServiceAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalTestAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.Manifest;
import no.altinn.brokerserviceexternal.ObjectFactory;
import no.altinn.brokerserviceexternal.Recipient;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.AltinnReasonFactory;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.SearchCriteria;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.ServiceCode;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.UploadManifest;
import no.nav.dokdistdpo.exception.technical.AltinnBrokerServiceWsException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.convertLocalDateTimeToXmlGregorianCalendar;

@Slf4j
@Component
public class AltinnBrokerServiceExternal {

	private static final String INITIATE_BROKER_SERVICE_FEILET = "Kall til IBrokerServiceExternal.initiateBrokerService feilet.";
	private static final String GET_AVAILABLE_FILES_FEILET = "Kall til IBrokerServiceExternal.getAvailableFiles feilet.";
	private static final String ALTINN_TESTKALL_FEILET = "Testkall mot altinn feilet.";
	private static final String CONFIRM_DOWNLOADED_FEILET = "Kall til IBrokerServiceExternal.confirmDownloaded feilet";


	private final IBrokerServiceExternal iBrokerServiceExternal;
	private final ObjectFactory objectFactory;

	public AltinnBrokerServiceExternal(IBrokerServiceExternal iBrokerServiceExternal) {
		this.iBrokerServiceExternal = iBrokerServiceExternal;
		this.objectFactory = new ObjectFactory();
	}

	public String initiateBrokerService(UploadManifest uploadManifest) {
		try {
			return iBrokerServiceExternal.initiateBrokerService(getBrokerServiceInitiation(uploadManifest));
		} catch (IBrokerServiceExternalInitiateBrokerServiceAltinnFaultFaultFaultMessage e) {
			throw new AltinnBrokerServiceWsException(INITIATE_BROKER_SERVICE_FEILET, AltinnReasonFactory.from(e), e);
		}
	}

	public List<String> getAvailableFiles(ServiceCode serviceCode, SearchCriteria searchCriteria) {
		return getFileReferences(serviceCode, searchCriteria)
				.map(BrokerServiceAvailableFileList::getBrokerServiceAvailableFile)
				.orElse(Collections.emptyList())
				.stream()
				.map(BrokerServiceAvailableFile::getFileReference)
				.toList();
	}

	public Optional<BrokerServiceAvailableFileList> getFileReferences(ServiceCode serviceCode, SearchCriteria criteria) {
		try {
			return Optional.of(iBrokerServiceExternal.getAvailableFiles(getBrokerServiceSearch(serviceCode, criteria)));
		} catch (IBrokerServiceExternalGetAvailableFilesAltinnFaultFaultFaultMessage e) {
			throw new AltinnBrokerServiceWsException(GET_AVAILABLE_FILES_FEILET, AltinnReasonFactory.from(e), e);
		}
	}

	public void confirmDownloaded(String fileReference) {
		try {
			iBrokerServiceExternal.confirmDownloaded(fileReference, NAV_ORGNUMMER);
			log.info("Kvittering med filreferanse={} er lastet ned fra Altinn", fileReference);
		} catch (IBrokerServiceExternalConfirmDownloadedAltinnFaultFaultFaultMessage e) {
			log.error(CONFIRM_DOWNLOADED_FEILET + "med fileReference: {}", fileReference);
			throw new AltinnBrokerServiceWsException(CONFIRM_DOWNLOADED_FEILET, AltinnReasonFactory.from(e), e);
		}
	}

	public void test() {
		try {
			iBrokerServiceExternal.test();
		} catch (IBrokerServiceExternalTestAltinnFaultFaultFaultMessage e) {
			throw new AltinnBrokerServiceWsException(ALTINN_TESTKALL_FEILET, AltinnReasonFactory.from(e), e);
		}
	}

	private BrokerServiceInitiation getBrokerServiceInitiation(UploadManifest uploadManifest) {
		BrokerServiceInitiation brokerServiceInitiation = objectFactory.createBrokerServiceInitiation();
		brokerServiceInitiation.setManifest(getManifest(uploadManifest));
		brokerServiceInitiation.setRecipientList(getArrayOfRecipient(uploadManifest.mottakerId()));
		return brokerServiceInitiation;
	}

	private ArrayOfRecipient getArrayOfRecipient(String mottakerId) {
		ArrayOfRecipient arrayOfRecipient = objectFactory.createArrayOfRecipient();
		Recipient recipient = objectFactory.createRecipient();
		recipient.setPartyNumber(mottakerId);
		arrayOfRecipient.getRecipient().add(recipient);
		return arrayOfRecipient;
	}

	private Manifest getManifest(UploadManifest uploadManifest) {
		Manifest manifest = new Manifest();
		manifest.setReportee(uploadManifest.avsender());
		manifest.setSendersReference(uploadManifest.senderReference());
		manifest.setExternalServiceCode(uploadManifest.serviceCode());
		manifest.setExternalServiceEditionCode(parseInt(uploadManifest.serviceEditionCode()));
		manifest.setFileList(new FileListBuilder().withFilename(uploadManifest.fileZipName()).build());

		return manifest;
	}

	private BrokerServiceSearch getBrokerServiceSearch(ServiceCode serviceCode, SearchCriteria criteria) {
		BrokerServiceSearch brokerServiceSearch = objectFactory.createBrokerServiceSearch();
		brokerServiceSearch.setFileStatus(criteria.availableFileStatus());
		brokerServiceSearch.setReportee(NAV_ORGNUMMER);
		brokerServiceSearch.setExternalServiceCode(objectFactory.createBrokerServiceSearchExternalServiceCode(serviceCode.serviceCode()));
		brokerServiceSearch.setExternalServiceEditionCode(serviceCode.serviceEditionCode());
		brokerServiceSearch.setMinSentDateTime(criteria.minSentDate() == null ? null : convertLocalDateTimeToXmlGregorianCalendar(criteria.minSentDate()));
		brokerServiceSearch.setMaxSentDateTime(criteria.maxSentDate() == null ? null : convertLocalDateTimeToXmlGregorianCalendar(criteria.maxSentDate()));
		return brokerServiceSearch;
	}
}
