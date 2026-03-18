package no.nav.dokdistdpo.qdist015.itest;

import no.nav.dokdistdpo.exception.technical.FileDownloadFromBucketTechnicalException;
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
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
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
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@DirtiesContext
@EnableAutoConfiguration
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
class Qdist015RouteIT extends AbstractQdist015ITest {

	private static final String MEDIA_TYPE_SOAP_XML = "application/soap+xml";
	private static final String MEDIA_TYPE_SOAP_XML_UTF_8 = "application/soap+xml; charset=utf-8";

	@BeforeEach
	void setUp() {
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
	void shouldThrowTechnicalExceptionAndToBackoutQueueWhenServiceRegistryFailsWithNotFound() {
		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPostMaskinporten();
		stubGetNotFoundServiceRegistry();
		stubPostOpprettForsendelse(OK);
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowTechnicalExceptionWhenFileDownloadFromBucketFails() {
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString())).thenThrow(FileDownloadFromBucketTechnicalException.class);
		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPutOppdaterForsendelse(OK);
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowTechnicalExceptionWhenUploadFileToAltinnFails() {
		stubGetForsendelse("__files/dokdistadmin/hentforsendelse-happy.json");
		stubPostMaskinporten();
		stubGetServiceRegistry(OK);
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed(INTERNAL_SERVER_ERROR);
		stubPostJuridisklogg(OK);
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist015, classpathToString("__files/qdist015/qdist015-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	void stubPostIntiateBrokerService() {
		stubFor(post(urlMatching("/brokerserviceexternal"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
						.withBody(classpathToString("__files/altinn2/brokerserviceinit_happy_response.xml").replace("localurl",
								dokdistdpoProperties.altinn2().brokerserviceexternal().endpointurl()))));
	}

	static void stubUploadBrokerServiceStreamed() {
		stubFor(post(urlMatching("/brokerserviceexternalstreamed/upload"))
				.withHeader(CONTENT_TYPE, containing(MEDIA_TYPE_SOAP_XML))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, MEDIA_TYPE_SOAP_XML_UTF_8)
						.withBodyFile("altinn2/brokerserviceupload_happy_response.xml")));
	}

	static void stubUploadBrokerServiceStreamed(HttpStatus status) {
		stubFor(post(urlMatching("/brokerserviceexternalstreamed/upload"))
				.withHeader(CONTENT_TYPE, containing(MEDIA_TYPE_SOAP_XML))
				.willReturn(aResponse()
						.withStatus(status.value())
						.withHeader(CONTENT_TYPE, MEDIA_TYPE_SOAP_XML_UTF_8)
						.withBodyFile("altinn2/brokerserviceuploadfile_fault_response.xml")));
	}

}