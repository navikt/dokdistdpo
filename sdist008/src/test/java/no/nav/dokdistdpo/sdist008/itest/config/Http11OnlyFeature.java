package no.nav.dokdistdpo.sdist008.itest.config;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

public class Http11OnlyFeature extends AbstractFeature {

	@Override
	public void initialize(Client client, Bus bus) {
		Conduit conduit = client.getConduit();
		if (conduit instanceof HTTPConduit httpConduit) {
			if (httpConduit.getClient() == null) {
				HTTPClientPolicy policy = new HTTPClientPolicy();
				policy.setVersion("1.1");
				httpConduit.setClient(policy);
			} else {
				httpConduit.getClient().setVersion("1.1");
			}
		}

		super.initialize(client, bus);
	}
}
