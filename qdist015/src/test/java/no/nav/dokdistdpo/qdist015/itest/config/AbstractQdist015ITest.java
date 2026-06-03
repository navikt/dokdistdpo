package no.nav.dokdistdpo.qdist015.itest.config;

import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import lombok.SneakyThrows;
import no.nav.dokdistdpo.consumer.gcloudstorage.EncryptedBucketStorage;
import org.apache.commons.io.IOUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.AVTALTMELDING_PROCESS_IDENTIFIER;
import static no.nav.dokdistdpo.constant.MDCConstant.CALL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Profile("itest")
public abstract class AbstractQdist015ITest {

	protected static final String FORSENDELSE_ID = "333333";
	protected static final String BASE_DOKDISTADMIN_PATH = "/administrerforsendelse";
	protected static final String OPPDATERFORSENDELSE_URL = BASE_DOKDISTADMIN_PATH + "/oppdaterforsendelse";
	public static final String DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK = "dokumentObjektReferanseHoveddok";
	public static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 = "dokumentObjektReferanseVedlegg1";
	public static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 = "dokumentObjektReferanseVedlegg2";
	public static final String HOVEDDOK_TEST_CONTENT = "HOVEDDOK_TEST_CONTENT";
	public static final String VEDLEGG1_TEST_CONTENT = "VEDLEGG1_TEST_CONTENT";
	public static final String VEDLEGG2_TEST_CONTENT = "VEDLEGG2_TEST_CONTENT";
	public static final String TRYGDERETTEN_ORGNUMMER = "974761084";

	protected static String callId = UUID.randomUUID().toString();

	@Autowired
	protected EncryptedBucketStorage encryptedBucketStorage;

	@Autowired
	protected Queue qdist015;
	@Autowired
	protected Queue qdist015FunksjonellFeil;
	@Autowired
	protected Queue qdist009;
	@Autowired
	protected Queue backoutQueue;
	@Autowired
	protected JmsTemplate jmsTemplate;

	protected static void stubPutOppdaterForsendelse(HttpStatus status) {
		stubFor(put(urlMatching(OPPDATERFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(status.value())));
	}

	protected static void stubPostJuridisklogg(HttpStatus status) {
		stubFor(post(urlMatching("/juridisklogg.*"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(status.value())
						.withBodyFile("juridisklogg/juridisklogg_happy_response.json")));
	}

	protected static void stubPostOpprettForsendelse(HttpStatus status) {
		stubFor(post(BASE_DOKDISTADMIN_PATH)
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(status.value())
						.withBodyFile("dokdistadmin/opprettforsendelse-response.json")));
	}

	protected static void stubGetForsendelse(String responsebody) {
		stubFor(get(BASE_DOKDISTADMIN_PATH + "/" + FORSENDELSE_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString(responsebody).replace("insertCallIdHere", callId))));
	}

	protected static void stubGetForsendelseWithMetadataType(String metadataType) {
		stubFor(get(BASE_DOKDISTADMIN_PATH + "/" + FORSENDELSE_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/dokdistadmin/hentforsendelse-with-forsendelsemetadata-type.json")
								.replace("insertCallIdHere", callId).replace("metadataType", metadataType))));
	}

	protected static void stubGetForsendelseWithForsendelseStatus(String forsendelseStatus) {
		stubFor(get(BASE_DOKDISTADMIN_PATH + "/" + FORSENDELSE_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/dokdistadmin/hentforsendelse-with-forsendelsestatus.json")
								.replace("insertCallIdHere", callId).replace("forsendelseStatus", forsendelseStatus))));
	}

	public static void stubGetServiceRegistry(HttpStatus status) {
		stubFor(get(urlMatching("/serviceregistry/identifier/974761084/process/urn:no:difi:profile:avtalt:avtalt:ver1.0"))
				.willReturn(aResponse()
						.withStatus(status.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("serviceregistry/serviceregistry_happy_response.json")));
	}

	public static void stubGetNotFoundServiceRegistry() {
		stubFor(get(urlMatching("/serviceregistry/identifier/974761084/process/urn:no:difi:profile:avtalt:avtalt:ver1.0"))
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("serviceregistry/serviceregistry_feil_response.json")));
	}

	protected static void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	public static void stubPostMaskinporten() {
		stubFor(post(urlMatching("/maskinporten"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("maskinporten/maskinporten_happy_response.json")));
	}

	@SneakyThrows
	public static String classpathToString(String classpathResource) {
		try {
			InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
			String message = IOUtils.toString(inputStream, UTF_8);
			IOUtils.closeQuietly(inputStream);
			return message;
		} catch (IOException e) {
			throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
		}
	}

	public void sendStringMessage(Queue queue, final String message) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = session.createTextMessage();
			msg.setText(message);
			if (MDC.get(CALL_ID) != null) {
				msg.setStringProperty(CALL_ID, callId);
			}
			return msg;
		});
	}


	protected void verifyPostJuridiskLoggLagre() {
		verify(postRequestedFor(urlEqualTo("/juridisklogg/api/rest/logg")));
	}

	protected void verifyServiceRegistry() {
		verify(getRequestedFor(urlMatching("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + AVTALTMELDING_PROCESS_IDENTIFIER)));
	}

	protected void verifyPutAdministrerforsendelse() {
		verify(putRequestedFor(urlMatching(OPPDATERFORSENDELSE_URL))
				.withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
	}

	protected void verifyGetForsendelse() {
		verify(getRequestedFor(urlMatching(BASE_DOKDISTADMIN_PATH + "/" + FORSENDELSE_ID)));
	}

	protected void assertMessageOnQueue(Queue queue) {
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
}
