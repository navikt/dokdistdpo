package no.nav.dokdistdpo.sdist008.itest.config;

import no.altinn.brokerserviceexternal.BrokerServiceExternalSF;
import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
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

@Profile("itest")
@Configuration
public class BrokerServiceExternalTestConfig extends AbstractCxfEndpointConfig {

	public BrokerServiceExternalTestConfig(Bus bus, DokdistdpoProperties dokdistdpoProperties) {
		super(bus, dokdistdpoProperties);
	}

	@Bean
	public IBrokerServiceExternal iBrokerServiceExternal(DokdistdpoProperties dokdistdpoProperties) {
		setAddress("wsdl/BrokerServiceExternalTest.wsdl");
		setServiceName(BrokerServiceExternalSF.SERVICE);
		setEndpointName(BrokerServiceExternalSF.CustomBindingIBrokerServiceExternal);
		setAddress(dokdistdpoProperties.altinn2().brokerserviceexternal().endpointurl());

		// Bruker kun http 1.1 for å unngå problemer med WireMock og streams
		addFeature(new Http11OnlyFeature());

		addInInterceptor(new CookiesInInterceptor());
		addOutInterceptor(new HeaderOutInterceptor());
		addOutInterceptor(new CookiesOutInterceptor());

		IBrokerServiceExternal iBrokerServiceExternal = createPort(IBrokerServiceExternal.class, false);
		final Client client = ClientProxy.getClient(iBrokerServiceExternal);
		setRequestContext(client, dokdistdpoProperties.dpo());

		return iBrokerServiceExternal;
	}

	private void setRequestContext(final Client client, DokdistdpoProperties.DpoUserProperties dpoUserProperties) {
		client.getRequestContext().put("ws-security.must-understand", Boolean.TRUE);
		client.getRequestContext().put("ws-security.username", dpoUserProperties.username());
		client.getRequestContext().put("ws-security.callback-handler", new ClientCallbackHandler(dpoUserProperties.password()));
		client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", Boolean.TRUE);
		client.getRequestContext().put("jakarta.xml.ws.session.maintain", Boolean.TRUE);
	}
}
