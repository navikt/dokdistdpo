package no.nav.dokdistdpo.config.altinn2.cxf;

import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;

import javax.xml.namespace.QName;
import java.net.URL;

import static jakarta.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;
import static java.lang.Boolean.TRUE;

public abstract class AbstractCxfEndpointConfig {

	private final JaxWsProxyFactoryBean factoryBean;
	private final DokdistdpoProperties dokdistdpoProperties;

	protected AbstractCxfEndpointConfig(Bus bus, DokdistdpoProperties dokdistdpoProperties) {
		factoryBean = new JaxWsProxyFactoryBean();
		this.dokdistdpoProperties = dokdistdpoProperties;
		factoryBean.setBus(bus);
	}

	protected void setAddress(String endpointUrl) {
		factoryBean.setAddress(endpointUrl);
	}

	protected void setWsdlUrl(String classPathResourceWsdlUrl) {
		factoryBean.setWsdlURL(getUrlFromClasspathResource(classPathResourceWsdlUrl));
	}

	protected void addFeature(Feature feature) {
		factoryBean.getFeatures().add(feature);
	}

	protected void setEndpointName(QName endpointName) {
		factoryBean.setEndpointName(endpointName);
	}

	protected void setServiceName(QName serviceName) {
		factoryBean.setServiceName(serviceName);
	}

	protected void addOutInterceptor(Interceptor<? extends Message> interceptor) {
		factoryBean.getOutInterceptors().add(interceptor);
	}

	protected void addInInterceptor(Interceptor<? extends Message> interceptor) {
		factoryBean.getInInterceptors().add(interceptor);
	}

	protected <T> T createPort(Class<T> portType, boolean isStreamed) {
		factoryBean.setBindingId(SOAP12HTTP_BINDING);
		DokdistdpoProperties.Altinn2Properties altinn2Properties = dokdistdpoProperties.altinn2();
		factoryBean.getFeatures().add(new TimeoutFeature(
				isStreamed ? altinn2Properties.brokerserviceexternalstreamed().connecttimeoutms() :
						altinn2Properties.brokerserviceexternal().connecttimeoutms(),
				isStreamed ? altinn2Properties.brokerserviceexternalstreamed().readtimeoutms() :
						altinn2Properties.brokerserviceexternal().readtimeoutms()));
		return factoryBean.create(portType);
	}

	private static String getUrlFromClasspathResource(String classpathResource) {
		URL url = AbstractCxfEndpointConfig.class.getClassLoader().getResource(classpathResource);
		if (url != null) {
			return url.toString();
		}
		throw new IllegalStateException("Failed to find resource: " + classpathResource);
	}

	protected void setRequestContext(final Client client) {
		client.getRequestContext().put("ws-security.must-understand", TRUE);
		client.getRequestContext().put("ws-security.username", dokdistdpoProperties.dpo().username());
		client.getRequestContext().put("ws-security.callback-handler", new ClientCallbackHandler(dokdistdpoProperties.dpo().password()));
		client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", TRUE);
		client.getRequestContext().put("jakarta.xml.ws.session.maintain", TRUE);
		client.getRequestContext().put("org.apache.cxf.logging.enable", true);
		client.getEndpoint().getInFaultInterceptors().add(new LoggingInInterceptor());

	}
}
