package no.nav.dokdistdpo.config.altinn2.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.transport.http.HTTPConduit;

public class TimeoutFeature extends AbstractFeature {

	private final int connectionTimeout;
	private final int receiveTimeout;

	public TimeoutFeature(int connectionTimeout, int receiveTimeout) {
		this.connectionTimeout = connectionTimeout;
		this.receiveTimeout = receiveTimeout;
	}

	@Override
	public void initialize(Client client, Bus bus) {
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();

		httpConduit.getClient().setConnectionTimeout(connectionTimeout);
		httpConduit.getClient().setReceiveTimeout(receiveTimeout);

		super.initialize(client, bus);
	}
}
