package no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.service;

import jakarta.activation.DataHandler;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.ws.BindingProvider;
import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternalstreamed.BrokerServiceExternalStreamedSF;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamed;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamedDownloadFileStreamedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalstreamed.ObjectFactory;
import no.altinn.brokerserviceexternalstreamed.ReceiptExternalStreamedBE;
import no.altinn.brokerserviceexternalstreamed.StreamedPayloadExternalBE;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.from.MessageFromAltinn2;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.AltinnReasonFactory;
import no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.ReceiptTo;
import no.nav.dokdistdpo.exception.functional.DokumentpakkingException;
import no.nav.dokdistdpo.exception.technical.AltinnBrokerServiceWsException;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.consumer.dpo.altinn2.altinn2brokerservice.to.AltinnReasonFactory.from;

@Slf4j
@Component
public class AltinnBrokerServiceStreamed {

	private static final String ALTINN_OPPLASTING_FEILET = "Opplasting av fil til Altinn feilet: {}";
	private static final String ALTINN_AVLESING_AV_MELDING_FEILET = "Kunne ikke lese fil fra Altinn: ";
	private static final String ALTINN_NEDLASTING_FEILET = "Kunne ikke laste ned fil fra Altinn: {}";

	private final String ALTINN_BROKERSERVICEEXTERNALSTREAMED_NAMESPACE = BrokerServiceExternalStreamedSF.SERVICE.getNamespaceURI();

	private final IBrokerServiceExternalStreamed brokerServiceExternalStreamed;
	private final ObjectFactory objectFactory;

	public AltinnBrokerServiceStreamed(IBrokerServiceExternalStreamed brokerServiceExternalStreamed) {
		this.brokerServiceExternalStreamed = brokerServiceExternalStreamed;
		this.objectFactory = new ObjectFactory();
	}

	public ReceiptTo uploadFileToAltinn(String filreferanse, String fileName, DataHandler dataHandler) {

		List<Header> headerList = mapHeaders(filreferanse, fileName).orElse(Collections.emptyList());

		((BindingProvider) brokerServiceExternalStreamed).getRequestContext().put(Header.HEADER_LIST, headerList);
		StreamedPayloadExternalBE streamedPayloadExternalBE = objectFactory.createStreamedPayloadExternalBE();
		streamedPayloadExternalBE.setDataStream(dataHandler);

		try {
			final ReceiptExternalStreamedBE receiptExternalStreamedBE = brokerServiceExternalStreamed.uploadFileStreamed(streamedPayloadExternalBE);

			return ReceiptTo.builder()
					.lastChanged(receiptExternalStreamedBE.getLastChanged().getValue())
					.parentReceiptId(receiptExternalStreamedBE.getParentReceiptId())
					.receiptHistory(receiptExternalStreamedBE.getReceiptHistory().getValue())
					.receiptId(receiptExternalStreamedBE.getReceiptId())
					.receiptStatusCode(receiptExternalStreamedBE.getReceiptStatusCode().getValue())
					.receiptText(receiptExternalStreamedBE.getReceiptText().getValue())
					.receiptTypeName(receiptExternalStreamedBE.getReceiptTypeName().getValue())
					.build();

		} catch (IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage e) {
			log.error(ALTINN_OPPLASTING_FEILET, from(e));
			throw new AltinnBrokerServiceWsException(ALTINN_OPPLASTING_FEILET, from(e), e);
		}
	}

	public List<MessageFromAltinn2> downloadFilesFromAltinn(List<String> filreferanser) {
		return filreferanser.stream()
				.map(filreferanse -> mapReferenceToDownloadedFile(filreferanse, downloadFile(filreferanse)))
				.toList();
	}

	private MessageFromAltinn2 mapReferenceToDownloadedFile(String filreferanse, DataHandler dataHandler) {
		try {
			InputStream inputStream = dataHandler.getInputStream();
			return new MessageFromAltinn2(filreferanse, inputStream);

		} catch (IOException | IllegalStateException e) {
			log.error(ALTINN_AVLESING_AV_MELDING_FEILET, e);
			throw new DokumentpakkingException(ALTINN_AVLESING_AV_MELDING_FEILET, e);
		}

	}

	public DataHandler downloadFile(String filreferanse) {
		try {
			final DataHandler dataHandler = brokerServiceExternalStreamed.downloadFileStreamed(filreferanse, NAV_ORGNUMMER);
			log.info("Lastet ned fil fra Altinn med filreferanse={}", filreferanse);
			return dataHandler;
		} catch (IBrokerServiceExternalStreamedDownloadFileStreamedAltinnFaultFaultFaultMessage e) {
			log.error(ALTINN_NEDLASTING_FEILET, from(e));
			throw new AltinnBrokerServiceWsException(ALTINN_NEDLASTING_FEILET, AltinnReasonFactory.from(e), e);
		}
	}

	private Optional<List<Header>> mapHeaders(String filreferanse, String fileName) {
		List<Header> headers = new ArrayList<>();

		try {
			Header reportee = new Header(new QName(ALTINN_BROKERSERVICEEXTERNALSTREAMED_NAMESPACE, "Reportee"), NAV_ORGNUMMER, new JAXBDataBinding(String.class));
			Header reference = new Header(new QName(ALTINN_BROKERSERVICEEXTERNALSTREAMED_NAMESPACE, "Reference"), filreferanse, new JAXBDataBinding(String.class));
			Header filename = new Header(new QName(ALTINN_BROKERSERVICEEXTERNALSTREAMED_NAMESPACE, "FileName"), fileName, new JAXBDataBinding(String.class));

			headers.add(reportee);
			headers.add(reference);
			headers.add(filename);

			return Optional.of(headers);
		} catch (JAXBException e) {
			log.error("Feil i filopplasting to Altinn", e);
			return Optional.empty();
		}
	}
}
