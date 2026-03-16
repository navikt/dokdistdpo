package no.nav.dokdistdpo.config.altinn2.cxf;

import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
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

import static no.altinn.brokerserviceexternal.BrokerServiceExternalSF.CustomBindingIBrokerServiceExternal;
import static no.altinn.brokerserviceexternal.BrokerServiceExternalSF.SERVICE;

@Profile("nais")
@Configuration
public class BrokerServiceExternalConfig extends AbstractCxfEndpointConfig {

	public BrokerServiceExternalConfig(Bus bus, DokdistdpoProperties dokdistdpoProperties) {
		super(bus, dokdistdpoProperties);
	}

	@Bean
	public IBrokerServiceExternal iBrokerServiceExternal(DokdistdpoProperties dokdistdpoProperties) {
		setWsdlUrl("wsdl/BrokerServiceExternal.wsdl");
		setServiceName(SERVICE);
		setEndpointName(CustomBindingIBrokerServiceExternal);
		setAddress(dokdistdpoProperties.altinn2().brokerserviceexternal().endpointurl());

		addInInterceptor(new CookiesInInterceptor());
		addOutInterceptor(new CookiesOutInterceptor());
		addOutInterceptor(new HeaderOutInterceptor());

		IBrokerServiceExternal iBrokerServiceExternal = createPort(IBrokerServiceExternal.class, false);
		Client client = ClientProxy.getClient(iBrokerServiceExternal);
		setRequestContext(client);

		return iBrokerServiceExternal;
	}

}
