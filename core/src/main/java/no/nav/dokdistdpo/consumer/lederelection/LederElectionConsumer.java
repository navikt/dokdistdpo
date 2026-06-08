package no.nav.dokdistdpo.consumer.lederelection;

import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;

@Slf4j
@Component
public class LederElectionConsumer {

	private final RestClient restClient;
	private final JsonMapper jsonMapper;

	public LederElectionConsumer(RestClient.Builder restClientBuilder,
								 JsonMapper jsonMapper,
								 @Value("${elector.get.url}") String electorPath) {
		this.jsonMapper = jsonMapper;
		this.restClient = restClientBuilder
				.baseUrl(electorPath)
				.build();
	}

	public boolean isLeder() {
		try {
			String response = restClient.get()
					.retrieve()
					.body(String.class);
			String leder = jsonMapper.readTree(response).get("name").asString();
			String hostName = InetAddress.getLocalHost().getHostName();
			return hostName.equals(leder);
		} catch (Exception e) {
			log.error("Kunne ikke bestemme lederpod. Feilmelding: {}", e.getMessage());
			return false;
		}
	}
}
