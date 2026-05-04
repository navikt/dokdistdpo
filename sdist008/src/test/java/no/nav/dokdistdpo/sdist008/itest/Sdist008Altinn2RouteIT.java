package no.nav.dokdistdpo.sdist008.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdistdpo.sdist008.Sdist008Altinn2Service;
import no.nav.dokdistdpo.sdist008.itest.config.ApplicationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistdpo.sdist008.itest.TestingUtils.getDownloadBody;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.TRANSFER_ENCODING;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;


@EnableAutoConfiguration
@AutoConfigureWireMock(port = 0)
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
@ActiveProfiles("itest")
class Sdist008Altinn2RouteIT {

	private static final Integer FORSENDELSE_ID = 1231;
	protected static final String BASE_DOKDISTADMIN_PATH = "/administrerforsendelse";
	private static final String XPATH_GET_AVAILABLE_FILES = "//*[local-name()='GetAvailableFiles']";
	private static final String XPATH_CONFIRM_DOWNLOADED = "//*[local-name()='ConfirmDownloaded']";

	@Autowired
	private Sdist008Altinn2Service sdist008Altinn2Service;

	@BeforeEach
	void setUp() {
		stubAzure();
		WireMock.resetAllRequests();
	}

	@Test
	void shouldHentKvitteringFromAltinnWithStatusSendtAndUpdateForsendelseStatusToBekreftet() throws IOException {
		stubGetHentEformidlingForsendelser();
		stubPostBrokerserviceExternalGetAvailableFiles();
		stubPostBrokerServiceExternalStreamedDownloadFileStreamed("__files/zip/altinn_sbd_kvittering_sendt.zip");
		stubGetAdministrerforsendleseHentForsendelse();
		stubPutAdministrerForsendelseOppdaterForsendelse();
		stubPostBrokerserviceExternalConfirmDownloaded();

		sdist008Altinn2Service.hentKvitteringOgOppdaterForsendelseStatus();

		verify(1, getRequestedFor(urlEqualTo(BASE_DOKDISTADMIN_PATH + "/eformidlingforsendelser?distribusjonKanaler=DPO,TRYGDERETTEN")));
		verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternal")).withRequestBody(matchingXPath(XPATH_GET_AVAILABLE_FILES)));
		verify(1, postRequestedFor(urlMatching("/brokerserviceexternalstreamed/download")));
		verify(1, putRequestedFor(urlMatching("/administrerforsendelse/oppdaterforsendelse"))
				.withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
		verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternal")).withRequestBody(matchingXPath(XPATH_CONFIRM_DOWNLOADED)));

	}

	@Test
	void shouldHentKvitteringFromAltinnWithStatusSendtAndUpdateForsendelseStatusToEkspedert() throws IOException {
		stubGetHentEformidlingForsendelser();
		stubPostBrokerserviceExternalGetAvailableFiles();
		stubPostBrokerServiceExternalStreamedDownloadFileStreamed("__files/zip/altinn_sbd_kvittering_lest.zip");
		stubGetForsendelse();
		stubPostLagreJuridiskLogg();
		stubPutAdministrerForsendelseOppdaterForsendelse();
		stubPostBrokerserviceExternalConfirmDownloaded();

		sdist008Altinn2Service.hentKvitteringOgOppdaterForsendelseStatus();

		verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternal")).withRequestBody(matchingXPath(XPATH_GET_AVAILABLE_FILES)));
		verify(1, postRequestedFor(urlMatching("/brokerserviceexternalstreamed/download")));
		verify(1, getRequestedFor(urlMatching("/administrerforsendelse/1231")));
		verify(1, postRequestedFor(urlMatching("/juridisklogg/api/rest/logg")));
		verify(1, putRequestedFor(urlMatching("/administrerforsendelse/oppdaterforsendelse"))
				.withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
	}

	@Test
	void shouldNotUpdateForsendelseWhenStatusovergangIsUgyldig() throws IOException {
		stubGetHentEformidlingForsendelser();
		stubPostBrokerserviceExternalGetAvailableFiles();
		stubPostBrokerServiceExternalStreamedDownloadFileStreamed("__files/zip/altinn_sbd_kvittering_mottatt.zip");
		stubGetForsendelse();
		stubPostLagreJuridiskLogg();
		stubPutAdministrerForsendelseOppdaterForsendelse();
		stubPostBrokerserviceExternalConfirmDownloaded();

		sdist008Altinn2Service.hentKvitteringOgOppdaterForsendelseStatus();

		verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternal")));
		verify(1, postRequestedFor(urlMatching("/brokerserviceexternalstreamed/download")));
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/eformidlingforsendelser?distribusjonKanaler=DPO,TRYGDERETTEN")));
	}


	private void stubPostBrokerserviceExternalGetAvailableFiles() {
		stubFor(post(urlEqualTo("/brokerserviceexternal"))
				.withRequestBody(matchingXPath(XPATH_GET_AVAILABLE_FILES))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
						.withBodyFile("altinn2/brokerserviceexternal/getavailablefiles_happy_response.xml")));
	}

	private void stubPostBrokerserviceExternalConfirmDownloaded() {
		stubFor(post(urlEqualTo("/brokerserviceexternal"))
				.withRequestBody(matchingXPath(XPATH_CONFIRM_DOWNLOADED))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
						.withBodyFile("altinn2/brokerserviceexternal/confirmdownloaded_happy_response.xml")));
	}

	private void stubPostBrokerServiceExternalStreamedDownloadFileStreamed(String path) throws IOException {
		String boundary = "uuid:c678c2f3-c620-4d19-9884-fc1c36c1d29a+id=174513";

		stubFor(post(urlMatching("/brokerserviceexternalstreamed/download"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, String.format("multipart/related; type=\"application/xop+xml\"; start=\"<http://tempuri.org/1>\"; boundary=\"%s\"; start-info=\"text/xml\"", boundary))
						.withHeader(TRANSFER_ENCODING, "chunked")
						.withHeader("MIME-Version", "1.0")
						.withBody(getDownloadBody(boundary, path))));
	}

	protected static void stubGetForsendelse() {
		stubFor(get(BASE_DOKDISTADMIN_PATH + "/" + FORSENDELSE_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/hentforsendelse-happy.json")));
	}

	private void stubGetHentEformidlingForsendelser() {
		stubFor(get("/administrerforsendelse/eformidlingforsendelser?distribusjonKanaler=DPO,TRYGDERETTEN")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/henteformidlingforsendelser.json")));
	}

	private void stubGetAdministrerforsendleseHentForsendelse() {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/hentforsendelse_with_status.json")));
	}

	private void stubPostLagreJuridiskLogg() {
		stubFor(post(urlMatching("/juridisklogg/api/rest/logg"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("juridisklogg/juridisklogg_happy_response.json")));
	}

	private void stubPutAdministrerForsendelseOppdaterForsendelse() {
		stubFor(put("/administrerforsendelse/oppdaterforsendelse")
				.willReturn(aResponse()
						.withStatus(OK.value())));
	}

	protected static void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}
}