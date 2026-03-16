package no.nav.dokdistdpo.qdist015.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdistdpo.qdist015.GCSForsendelseDokument;
import no.nav.dokdistdpo.qdist015.itest.config.AbstractQdist015ITest;
import no.nav.dokdistdpo.qdist015.itest.config.ApplicationTestConfig;
import no.nav.dokdistdpo.qdist015.utils.JsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@DirtiesContext
@EnableAutoConfiguration
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles({"itest", "altinn3"})
public class Qdist015RouteAltinn3IT extends AbstractQdist015ITest {

	@BeforeEach
	void setUp() {
		reset(encryptedBucketStorage);
		naisToken();
		altinnToken();
		stubAzure();
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString()))
				.thenReturn(JsonSerializer.serialize(GCSForsendelseDokument.builder().pdf(HOVEDDOK_TEST_CONTENT.getBytes()).build()));
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1), anyString()))
				.thenReturn(JsonSerializer.serialize(GCSForsendelseDokument.builder().pdf(VEDLEGG1_TEST_CONTENT.getBytes()).build()));
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2), anyString()))
				.thenReturn(JsonSerializer.serialize(GCSForsendelseDokument.builder().pdf(VEDLEGG2_TEST_CONTENT.getBytes()).build()));
	}

	@Test
	void shouldInitiateFileTransferAndUploadFileToAltinn() {
		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		stubPostIntiateFileTransfer();
		stubPostFileUpload(OK);
		stubPostJuridisklogg(OK);
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyAltinn3UploadWithPostProcessing);
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "DPO_MELDING"})
	void shouldThrowExceptionWhenForsendelseMetadataIsNullOrInvalidType(String metadataType) {
		stubGetForsendelseWithMetadataType(metadataType);
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist015FunksjonellFeil));
	}

	@ParameterizedTest
	@ValueSource(strings = {"OPPRETTET", "OVERSENDT", "EKSPEDERT"})
	void shouldThrowExceptionWhenForsendelseStatusIsNotKlarForDist(String forsendelseStatus) {
		stubGetForsendelseWithForsendelseStatus(forsendelseStatus);
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist015FunksjonellFeil));
	}

	@Test
	void shouldThrowTechnicalExceptionAndSendToBackoutQueueWhenServiceRegistryReturnsNotFound() {
		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPostMaskinporten();
		stubGetServiceRegistry();
		stubPostOpprettForsendelse(OK);
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowTechnicalExceptionWhenUploadFileToAltinnFails() {
		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		stubPostIntiateFileTransfer();
		stubPostFileUpload(INTERNAL_SERVER_ERROR);
		stubPostJuridisklogg(OK);
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	protected void verifyAltinn3UploadWithPostProcessing() {
		verifyGetForsendelse();
		verifyPostMaskinporten();
		verifyServiceRegistry();
		verifyPostNaisToken();
		verifyPostAltinnToken();
		verifyPostAltinn3InitiateFileTransfer();
		verifyPostAltinn3Upload();
		verifyPostJuridiskLoggLagre();
		verifyPutAdministrerforsendelse();
	}

	void verifyPostAltinn3Upload() {
		verify(postRequestedFor(urlEqualTo("/altinn3/broker/api/v1/filetransfer/f6f00f2e-9c62-4d0d-8e50-7a83a6e5a1b2/upload")));
	}

	void verifyPostAltinnToken() {
		verify(postRequestedFor(urlEqualTo("/altinn3/authentication/api/v1/exchange/maskinporten")));
	}

	void verifyPostMaskinporten() {
		verify(postRequestedFor(urlEqualTo("/maskinporten")));
	}

	void verifyPostNaisToken() {
		verify(postRequestedFor(urlEqualTo("/nais")));
	}

	void verifyPostAltinn3InitiateFileTransfer() {
		verify(postRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer")));
	}

	void stubPostIntiateFileTransfer() {
		stubFor(post(urlMatching("/altinn3/broker/api/v1/filetransfer"))
				.willReturn(WireMock.aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("altinn3/intialize-file-transfer-response.json")));
	}

	void stubPostFileUpload(HttpStatus status) {
		stubFor(post(urlMatching("/altinn3/broker/api/v1/filetransfer/f6f00f2e-9c62-4d0d-8e50-7a83a6e5a1b2/upload"))
				.willReturn(WireMock.aResponse()
						.withStatus(status.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("altinn3/file-upload-response.json")
				));
	}

	void naisToken() {
		stubFor(post(urlMatching("/nais"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nais-token/token_response.json")));
	}

	void altinnToken() {
		stubFor(post(urlMatching("/altinn3/authentication/api/v1/exchange/maskinporten"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("altinn3/altinn3token.json")));
	}
}