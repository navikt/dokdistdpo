package no.nav.dokdistdpo.sdist008.itest;

import no.nav.dokdistdpo.sdist008.altinn3.Sdist008Altinn3Service;
import no.nav.dokdistdpo.sdist008.itest.config.ApplicationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubAltinn3ConfirmDownload;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubAltinn3DownloadFilStatus;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubAltinn3GetFilTransferIder;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubAltinn3Token;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubAzure;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubGetHentEformidlingForsendelser;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubGetHentForsendelse;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubNaisTexasToken;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubPostLagreJuridiskLogg;
import static no.nav.dokdistdpo.sdist008.itest.config.WiremockStub.stubPutOppdaterForsendelse;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles("itest")
@SpringBootTest(
		classes = ApplicationTestConfig.class,
		webEnvironment = RANDOM_PORT)
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 0)
class Sdist008Altinn3RouteIT {


	@Autowired
	private Sdist008Altinn3Service sdist008Service;

	@BeforeEach
	void setUp() {
		stubAzure();
		stubAltinn3Token();
		stubNaisTexasToken();
	}

	@Test
	void shouldDownloadFileWithStatusSendtAndUpdateForsendelseStatusToBekreftet() {
		stubAltinn3GetFilTransferIder();
		stubGetHentEformidlingForsendelser();
		stubAltinn3DownloadFilStatus("__files/zip/altinn_sbd_kvittering_sendt.zip");
		stubGetHentForsendelse();
		stubPutOppdaterForsendelse();
		stubAltinn3ConfirmDownload();

		sdist008Service.oppdaterForsendelse();

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/eformidlingforsendelser?distribusjonKanaler=DPO,TRYGDERETTEN")));
		verify(1, getRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer\\?.*")));
		verify(1, getRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer/.*/download")));
		verify(1, putRequestedFor(urlMatching("/administrerforsendelse/oppdaterforsendelse")));
		verify(1, postRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer/.*/confirmdownload")));
	}

	@Test
	void shouldDownloadFileWithStatusLevertAndUpdateForsendelseStatusToEkspedert() {
		stubAltinn3GetFilTransferIder();
		stubGetHentEformidlingForsendelser();
		stubGetHentForsendelse();
		stubAltinn3DownloadFilStatus("__files/zip/altinn_sbd_kvittering_lest.zip");
		stubPutOppdaterForsendelse();
		stubPostLagreJuridiskLogg();
		stubAltinn3ConfirmDownload();

		sdist008Service.oppdaterForsendelse();

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/eformidlingforsendelser?distribusjonKanaler=DPO,TRYGDERETTEN")));
		verify(1, getRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer\\?.*")));
		verify(1, getRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer/.*/download")));
		verify(1, putRequestedFor(urlMatching("/administrerforsendelse/oppdaterforsendelse")));
		verify(1, postRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer/.*/confirmdownload")));
	}

	@Test
	void shouldDownloadFileWithStatusLevertAndUpdateForsendelseStatusToFail() {
		stubAltinn3GetFilTransferIder();
		stubGetHentEformidlingForsendelser();
		stubGetHentForsendelse();
		stubAltinn3DownloadFilStatus("__files/zip/altinn_sbd_levetid_utlopt.zip");
		stubPutOppdaterForsendelse();
		stubPostLagreJuridiskLogg();
		stubAltinn3ConfirmDownload();

		sdist008Service.oppdaterForsendelse();

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/eformidlingforsendelser?distribusjonKanaler=DPO,TRYGDERETTEN")));
		verify(1, getRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer\\?.*")));
		verify(1, getRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer/.*/download")));
		verify(1, putRequestedFor(urlMatching("/administrerforsendelse/oppdaterforsendelse")));
		verify(1, postRequestedFor(urlMatching("/altinn3/broker/api/v1/filetransfer/.*/confirmdownload")));
	}
}
