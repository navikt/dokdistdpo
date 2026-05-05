package no.nav.dokdistdpo.sdist008.itest.config.altinn2;

import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamed;
import no.nav.dokdistdpo.config.altinn2.cxf.AbstractCxfEndpointConfig;
import no.nav.dokdistdpo.config.altinn2.cxf.ClientCallbackHandler;
import no.nav.dokdistdpo.config.altinn2.cxf.interceptor.CookiesInInterceptor;
import no.nav.dokdistdpo.config.altinn2.cxf.interceptor.CookiesOutInterceptor;
import no.nav.dokdistdpo.config.altinn2.cxf.interceptor.HeaderOutInterceptor;
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
		setAddress(dokdistdpoProperties.altinn2().brokerserviceexternalstreamed().endpointurl());

		addFeature(new Http11OnlyFeature());

		addInInterceptor(new CookiesInInterceptor());
		addOutInterceptor(new HeaderOutInterceptor());
		addOutInterceptor(new CookiesOutInterceptor());

		IBrokerServiceExternalStreamed iBrokerServiceExternalStreamed = createPort(IBrokerServiceExternalStreamed.class, true);
		final Client client = ClientProxy.getClient(iBrokerServiceExternalStreamed);
		setRequestContext(client, dokdistdpoProperties.dpo());
		return iBrokerServiceExternalStreamed;
	}

	private void setRequestContext(final Client client, DokdistdpoProperties.DpoUserProperties dpoUserProperties) {
		client.getRequestContext().put("ws-security.must-understand", Boolean.TRUE);
		client.getRequestContext().put("ws-security.username", dpoUserProperties.username());
		client.getRequestContext().put("ws-security.callback-handler", new ClientCallbackHandler(dpoUserProperties.password()));
		client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", Boolean.TRUE);
		client.getRequestContext().put("jakarta.xml.ws.session.maintain", Boolean.TRUE);
	}
}
