package no.nav.dokdistdpo.config.cxf;

import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.HashMap;

public abstract class AbstractCxfEndpointConfig {

	private final JaxWsProxyFactoryBean factoryBean;
	private final DokdistdpoProperties dokdistdpoProperties;

	public AbstractCxfEndpointConfig(Bus bus, DokdistdpoProperties dokdistdpoProperties) {
		factoryBean = new JaxWsProxyFactoryBean();
		factoryBean.setProperties(new HashMap<>());
		this.dokdistdpoProperties = dokdistdpoProperties;
		factoryBean.setBus(bus);
	}

	protected void setAddress(String aktoerUrl) {
		factoryBean.setAddress(aktoerUrl);
	}

	protected void setWsdlUrl(String classPathResourceWsdlUrl) {
		factoryBean.setWsdlURL(getUrlFromClasspathResource(classPathResourceWsdlUrl));
	}

	protected void setEndpointName(QName endpointName) {
		factoryBean.setEndpointName(endpointName);
	}

	protected void setServiceName(QName serviceName) {
		factoryBean.setServiceName(serviceName);
	}

	protected void addFeature(Feature feature) {
		factoryBean.getFeatures().add(feature);
	}

	protected void addOutInterceptor(Interceptor<? extends Message> interceptor) {
		factoryBean.getOutInterceptors().add(interceptor);
	}

	protected void addInInterceptor(Interceptor<? extends Message> interceptor) {
		factoryBean.getInInterceptors().add(interceptor);
	}

	protected <T> T createPort(Class<T> portType) {
		factoryBean.getFeatures().add(new TimeoutFeature(dokdistdpoProperties.altinn().brokerserviceexternal().connecttimeoutms(),
				dokdistdpoProperties.altinn().brokerserviceexternal().readtimeoutms()));
		return factoryBean.create(portType);
	}

	private static String getUrlFromClasspathResource(String classpathResource) {
		URL url = AbstractCxfEndpointConfig.class.getClassLoader().getResource(classpathResource);
		if (url != null) {
			return url.toString();
		}
		throw new IllegalStateException("Failed to find resource: " + classpathResource);
	}
}
