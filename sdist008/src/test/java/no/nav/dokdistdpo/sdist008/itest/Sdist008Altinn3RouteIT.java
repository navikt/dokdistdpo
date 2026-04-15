package no.nav.dokdistdpo.sdist008.itest;

import no.nav.dokdistdpo.sdist008.itest.config.ApplicationTestConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles("itest")
@SpringBootTest(
		classes = ApplicationTestConfig.class,
		webEnvironment = RANDOM_PORT)
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 0)
public class Sdist008Altinn3RouteIT {


}
