package no.nav.dokdistdpo.qdist015.itest;

import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import no.nav.dokdistdpo.consumer.gcloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpo.exception.technical.FileDownloadFromBucketTechnicalException;
import no.nav.dokdistdpo.qdist015.GCSForsendelseDokument;
import no.nav.dokdistdpo.qdist015.itest.config.AbstractQdist015ITest;
import no.nav.dokdistdpo.qdist015.utils.JsonSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.constant.MDCConstant.CALL_ID;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@ActiveProfiles("itest")
class Qdist015RouteIT extends AbstractQdist015ITest {

	@Autowired
	private EncryptedBucketStorage encryptedBucketStorage;

	@Autowired
	private Queue qdist015;
	@Autowired
	private Queue qdist015FunksjonellFeil;
	@Autowired
	private Queue qdist009;
	@Autowired
	private Queue backoutQueue;
	@Autowired
	private JmsTemplate jmsTemplate;

	@BeforeEach
	public void setUp() {
		reset(encryptedBucketStorage);

		stubAzure();
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString()))
				.thenReturn(JsonSerializer.serialize(GCSForsendelseDokument.builder().pdf(HOVEDDOK_TEST_CONTENT.getBytes()).build()));
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1), anyString()))
				.thenReturn(JsonSerializer.serialize(GCSForsendelseDokument.builder().pdf(VEDLEGG1_TEST_CONTENT.getBytes()).build()));
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2), anyString()))
				.thenReturn(JsonSerializer.serialize(GCSForsendelseDokument.builder().pdf(VEDLEGG2_TEST_CONTENT.getBytes()).build()));
	}

	@Test
	void shouldUploadFileToAltinn() {
		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubPostJuridisklogg(OK);
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyAltinnUploadWithPostProcessing);
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "DPO_MELDING"})
	void shouldThrowFunctionalExceptionWhenForsendelseMetadataIsNullOrIsNotOneOfAvtaleOrArkivmeldingType(String metadataType) {
		stubGetForsendelseWithMetadataType(metadataType);
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist015FunksjonellFeil));
	}

	@ParameterizedTest
	@ValueSource(strings = {"OPPRETTET", "OVERSENDT", "EKSPEDERT"})
	void shouldThrowFunctionalExceptionWhenForsendelseStatusIsNotKlarForDist(String forsendelseStatus) {
		stubGetForsendelseWithForsendelseStatus(forsendelseStatus);
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist015FunksjonellFeil));
	}

	@Test
	void shouldSendForsendelseTilPrintIfOrganisasjonNotFoundInServiceRegistry() {
		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPostMaskinporten();
		stubGetServiceRegistry(NOT_FOUND);
		stubPostOpprettForsendelse(OK);
		stubPutFeilregistrerforsendelse(OK.value());
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifySendToPrint);
	}

	@Test
	void shouldThrowFileDownloadFromBucketTechnicalException() {
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString())).thenThrow(FileDownloadFromBucketTechnicalException.class);
		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowTechnicalExceptionWhenUploadFileToAltinnFeil() {
		stubPostOpprettForsendelse(OK);

	}

	private void verifySendToPrint() {
		verifyGetForsendelse();
		verifyServiceRegistry();
		verifyPostOpprettForsendelse();
		verifyFeilregistrerForsendelse();
	}

	private void verifyAltinnUploadWithPostProcessing() {
		verifyGetForsendelse();
		verifyServiceRegistry();
		verifyPostIntiateBrokerService();
		verifyPostUploadBrokerServiceStreamed();
		verifyPostJuridiskLoggLagre();
		verifyPutAdministrerforsendelse();
	}

	private void verifyPostIntiateBrokerService() {
		verify(postRequestedFor(urlMatching("/brokerserviceexternal")));
	}

	private void verifyPostUploadBrokerServiceStreamed() {
		verify(postRequestedFor(urlEqualTo("/brokerserviceexternalstreamed/upload")));
	}

	private void verifyPostJuridiskLoggLagre() {
		verify(postRequestedFor(urlEqualTo("/juridisklogg/api/rest/logg")));
	}

	private void verifyServiceRegistry() {
		verify(getRequestedFor(urlMatching("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER)));
	}

	private void verifyPutAdministrerforsendelse() {
		verify(putRequestedFor(urlMatching(OPPDATERFORSENDELSE_URL)));
	}

	private void verifyFeilregistrerForsendelse() {
		verify(putRequestedFor(urlMatching(AbstractQdist015ITest.FEILREGISTRERFORSENDELSE_URL)));
	}

	private void verifyPostOpprettForsendelse() {
		verify(postRequestedFor(urlEqualTo(BASE_DOKDISTADMIN_PATH)));
	}

	private void verifyGetForsendelse() {
		verify(getRequestedFor(urlMatching(BASE_DOKDISTADMIN_PATH + "/" + FORSENDELSE_ID)));
	}

	private void assertMessageOnQueue(Queue queue) {
		String message = receive(queue);
		assertNotNull(message);
		assertEquals(message, classpathToString("__files/qdist015/qdist015-happy.xml"));
	}

	@SuppressWarnings("unchecked")
	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

	private void sendStringMessage(Queue queue, final String message) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = session.createTextMessage();
			msg.setText(message);
			if (MDC.get(CALL_ID) != null) {
				msg.setStringProperty(CALL_ID, callId);
			}
			return msg;
		});
	}
}