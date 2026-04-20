package no.nav.dokdistdpo.sdist008.itest.config;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static no.nav.dokdistdpo.sdist008.itest.TestingUtils.classpathToByteArray;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class WiremockStub {

	private static final Integer FORSENDELSE_ID = 1231;
	protected static final String BASE_DOKDISTADMIN_PATH = "/administrerforsendelse";

	public static void stubAltinn3DownloadFilStatus(String path) {
		stubFor(get("/altinn3/broker/api/v1/filetransfer/d3d6d652-90f6-4d20-9f28-4f1b55cfda78/download")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToByteArray(path))));
	}

	public static void stubAltinn3GetFilTransferIder() {
		stubFor(get("/altinn3/broker/api/v1/filetransfer?resourceId=eformidling-dpo-meldingsutveksling&status=Published&recipientStatus=Initialized")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("altinn3/altinn3_get_fil_transfer_ider.json")));
	}

	public static void stubAltinn3ConfirmDownload() {
		stubFor(post("/altinn3/broker/api/v1/filetransfer/d3d6d652-90f6-4d20-9f28-4f1b55cfda78/confirmdownload")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	public static void stubGetHentEformidlingForsendelser() {
		stubFor(get("/administrerforsendelse/henteformidlingforsendelser?distribusjonKanal=DPO")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/henteformidlingforsendelser.json")));
	}

	public static void stubGetHentForsendelse() {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/hentforsendelse-happy.json")));
	}

	public static void stubPostLagreJuridiskLogg() {
		stubFor(post(urlMatching("/juridisklogg/api/rest/logg"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("juridisklogg/juridisklogg_happy_response.json")));
	}

	public static void stubPutOppdaterForsendelse() {
		stubFor(put("/administrerforsendelse/oppdaterforsendelse")
				.willReturn(aResponse()
						.withStatus(OK.value())));
	}

	public static void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	public static void stubNaisTexasToken() {
		stubFor(post("/nais")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nais-texas/token_response.json")));
	}


	public static void stubAltinn3Token() {
		stubFor(post("/altinn3/authentication/api/v1/exchange/maskinporten")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("altinn3/altinn3_token.json")));
	}
}
