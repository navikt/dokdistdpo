package no.nav.dokdistdpo.consumer.dokdistadmin;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.functional.DokdistdpoFunctionalException;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.lang.String.format;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_DOKDISTADMIN;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Slf4j
@Component
public class DokdistAdminConsumer {

	private final RestClient restClient;

	public DokdistAdminConsumer(RestClient.Builder restClientBuilder,
								DokdistdpoProperties dokdistdpoProperties) {
		this.restClient = restClientBuilder
				.baseUrl(dokdistdpoProperties.endpoints().dokdistadmin().url())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public HentForsendelseResponse hentForsendelse(String forsendelseId) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/{forsendelseId}")
						.build(forsendelseId))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					if (res.getStatusCode().is4xxClientError()) {
						throw new DokdistdpoFunctionalException(format("hentForsendelse feilet funksjonelt med forsendelseID=%s. Feilmelding=%s", forsendelseId, res.getStatusText()));
					}
					throw new DokdistdpoTechnicalException(format("hentForsendelse feilet teknisk med forsendelseId=%s, feilmelding=%s", forsendelseId, res.getStatusText()));
				})
				.body(HentForsendelseResponse.class);
	}
}
