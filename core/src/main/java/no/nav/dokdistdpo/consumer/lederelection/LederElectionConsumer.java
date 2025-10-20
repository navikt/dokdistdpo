package no.nav.dokdistdpo.consumer.lederelection;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;

@Slf4j
@Component
public class LederElectionConsumer {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;


	public LederElectionConsumer(RestClient.Builder restClientBuilder,
								 ObjectMapper objectMapper,
								 @Value("${elector.path}") String electorPath) {
		this.objectMapper = objectMapper;
		this.restClient = restClientBuilder
				.baseUrl(electorPath.startsWith("http") ? electorPath : "http://" + electorPath)
				.build();
	}

	public boolean isLeder() {
		try {
			String response = restClient.get()
					.retrieve()
					.body(String.class);
			String leder = objectMapper.readTree(response).get("name").asText();
			String hostName = InetAddress.getLocalHost().getHostName();
			return hostName.equals(leder);
		} catch (Exception e) {
			log.error("Kunne ikke bestemme lederpod. Feilmelding: {}", e.getMessage());
			return false;
		}
	}
}
