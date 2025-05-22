package no.nav.dokdistdpo.qdist015.itest.config;

import lombok.SneakyThrows;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@ActiveProfiles("itest")
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 0)
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
public abstract class AbstractQdist015ITest {

	protected static final String FORSENDELSE_ID = "33333";
	protected static final String BASE_DOKDISTADMIN_PATH = "/administrerforsendelse/";
	protected static final String OPPDATERFORSENDELSE_URL = BASE_DOKDISTADMIN_PATH + "oppdaterforsendelse";
	public static final String DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK = "dokumentObjektReferanseHoveddok";
	public static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 = "dokumentObjektReferanseVedlegg1";
	public static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 = "dokumentObjektReferanseVedlegg2";
	public static final String HOVEDDOK_TEST_CONTENT = "HOVEDDOK_TEST_CONTENT";
	public static final String VEDLEGG1_TEST_CONTENT = "VEDLEGG1_TEST_CONTENT";
	public static final String VEDLEGG2_TEST_CONTENT = "VEDLEGG2_TEST_CONTENT";
	public static final String TRYGDERETTEN_ORGNUMMER = "974761084";
	;

	protected static String callId;

	@Autowired
	private DokdistdpoProperties dokdistdpoProperties;

	@BeforeEach
	void setUp() {
		callId = UUID.randomUUID().toString();
	}

	protected void stubPostIntiateBrokerService() {
		stubFor(post(urlMatching("/brokerserviceexternal"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
						.withBody(classpathToString("__files/altinn/brokerserviceinit_happy_response.xml").replace("localurl",
								dokdistdpoProperties.altinn().brokerserviceexternal().endpointurl()))));
	}

	protected static void stubUploadBrokerServiceStreamed() {
		stubFor(post(urlMatching("/brokerserviceexternalstreamed/upload"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
						.withBody(classpathToString("__files/altinn/brokerserviceupload_happy_response.xml"))));
	}

	protected static void stubPostJuridiskLoggLagre() {
		stubFor(post(urlMatching("/juridisklogg.*"))
				.willReturn(aResponse()
						.withStatus(OK.value())));
	}

	protected static void stubPutOppdaterForsendelse(HttpStatus status) {
		stubFor(put(urlMatching(OPPDATERFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(status.value())));
	}

	protected static void stubGetForsendelse(String responsebody) {
		stubFor(get(BASE_DOKDISTADMIN_PATH + FORSENDELSE_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString(responsebody).replace("insertCallIdHere", callId))));
	}

	public static void stubGetServiceRegistry(String orgnummer, String processIdent) {
		stubFor(get(urlMatching("/serviceregistry/identifier/" + orgnummer + "/process/" + processIdent))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/serviceregistry/serviceregistry_happy_response.json"))));
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
						.withBody(classpathToString("__files/maskinporten/maskinporten_happy_response.json"))));
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
}
