package no.nav.dokdistdpo.qdist015.itest;

import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import no.nav.dokdistdpo.consumer.gcloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpo.qdist015.DokdistDokumentFromStorage;
import no.nav.dokdistdpo.qdist015.itest.config.AbstractQdist015ITest;
import no.nav.dokdistdpo.utils.JsonSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.utils.MdcConstant.CALL_ID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.springframework.http.HttpStatus.OK;

public class Qdist015RouteITest extends AbstractQdist015ITest {


	@Autowired
	private EncryptedBucketStorage encryptedBucketStorage;

	@Autowired
	private Queue qdist015;
	@Autowired
	private Queue qdist015FunksjonellFeil;
	@Autowired
	private Queue qdist009;
	@Autowired
	private JmsTemplate jmsTemplate;

	@BeforeEach
	public void setUp() {
		reset(encryptedBucketStorage);

		stubAzure();
		Mockito.when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString()))
				.thenReturn(JsonSerializer.serialize(DokdistDokumentFromStorage.builder().pdf(HOVEDDOK_TEST_CONTENT.getBytes()).build()));
		Mockito.when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1), anyString()))
				.thenReturn(JsonSerializer.serialize(DokdistDokumentFromStorage.builder().pdf(VEDLEGG1_TEST_CONTENT.getBytes()).build()));
		Mockito.when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2), anyString()))
				.thenReturn(JsonSerializer.serialize(DokdistDokumentFromStorage.builder().pdf(VEDLEGG2_TEST_CONTENT.getBytes()).build()));
	}

	@Test
	@Disabled
	void shouldUploadFileToAltinn() {

		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPostMaskinporten();
		stubGetServiceRegistry(TRYGDERETTEN_ORGNUMMER, SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER);
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubPostJuridiskLoggLagre();
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAsserted(this::verifyAltinnUploadWithPostProcessing);

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
		verify(1, postRequestedFor(urlMatching("/brokerserviceexternal")));
	}

	private void verifyPostUploadBrokerServiceStreamed() {
		verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternalstreamed/upload")));
	}

	private void verifyPostJuridiskLoggLagre() {
		verify(1, postRequestedFor(urlEqualTo("/juridisklogg"))
				.withRequestBody(equalToJson(classpathToString("__files/juridisklogg/juridiskloggRequest.json"), true, true))
				.withRequestBody(matchingJsonPath("$.meldingsId", containing(CALL_ID)))
				.withRequestBody(matchingJsonPath("$.meldingsInnhold")));
	}

	private void verifyServiceRegistry() {
		verify(1, postRequestedFor(urlMatching("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER)));
	}

	private void verifyPutAdministrerforsendelse() {
		verify(0, putRequestedFor(urlEqualTo(OPPDATERFORSENDELSE_URL)));
	}

	private void verifyGetForsendelse() {
		verify(1, getRequestedFor(urlMatching(BASE_DOKDISTADMIN_PATH + FORSENDELSE_ID)));
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
