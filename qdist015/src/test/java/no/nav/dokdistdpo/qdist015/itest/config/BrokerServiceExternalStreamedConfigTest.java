package no.nav.dokdistdpo.qdist015.itest.config;

import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamed;
import no.nav.dokdistdpo.config.cxf.AbstractCxfEndpointConfig;
import no.nav.dokdistdpo.config.cxf.ClientCallBackHandler;
import no.nav.dokdistdpo.config.cxf.interceptor.CookiesInInterceptor;
import no.nav.dokdistdpo.config.cxf.interceptor.CookiesOutInterceptor;
import no.nav.dokdistdpo.config.cxf.interceptor.HeaderOutInterceptor;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static no.altinn.brokerserviceexternalstreamed.BrokerServiceExternalStreamedSF.CustomBindingIBrokerServiceExternalStreamed;
import static no.altinn.brokerserviceexternalstreamed.BrokerServiceExternalStreamedSF.SERVICE;

@Profile("itest")
@Configuration
public class BrokerServiceExternalStreamedConfigTest extends AbstractCxfEndpointConfig {

	public BrokerServiceExternalStreamedConfigTest(Bus bus, DokdistdpoProperties dokdistdpoProperties) {
		super(bus, dokdistdpoProperties);
	}

	@Bean
	public IBrokerServiceExternalStreamed iBrokerServiceExternalStreamed(DokdistdpoProperties dokdistdpoProperties) {
		setAddress("wsdl/BrokerServiceExternalStreamedTest.wsdl");
		setServiceName(SERVICE);
		setEndpointName(CustomBindingIBrokerServiceExternalStreamed);
		setAddress(dokdistdpoProperties.altinn().brokerserviceexternalstreamed().endpointurl());

		addInInterceptor(new CookiesInInterceptor());
		addOutInterceptor(new HeaderOutInterceptor());
		addOutInterceptor(new CookiesOutInterceptor());

		IBrokerServiceExternalStreamed iBrokerServiceExternalStreamed = createPort(IBrokerServiceExternalStreamed.class);
		final Client client = ClientProxy.getClient(iBrokerServiceExternalStreamed);
		setRequestContext(client, dokdistdpoProperties.dpoUser());
		return iBrokerServiceExternalStreamed;
	}

	private void setRequestContext(final Client client, DokdistdpoProperties.DpoUserProperties dpoUserProperties) {
		client.getRequestContext().put("ws-security.must-understand", Boolean.TRUE);
		client.getRequestContext().put("ws-security.username", dpoUserProperties.username());
		client.getRequestContext().put("ws-security.callback-handler", new ClientCallBackHandler(dpoUserProperties.password()));
		client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", Boolean.TRUE);
		client.getRequestContext().put("jakarta.xml.ws.session.maintain", Boolean.TRUE);
	}
}
