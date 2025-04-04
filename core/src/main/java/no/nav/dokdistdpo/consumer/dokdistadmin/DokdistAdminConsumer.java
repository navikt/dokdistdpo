package no.nav.dokdistdpo.consumer.dokdistadmin;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.azure.OAuth2TokenInterceptor;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.functional.DokdistdpoFunctionalException;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static java.lang.String.format;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_DOKDISTADMIN;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class DokdistAdminConsumer {

	private final RestClient restClient;

	public DokdistAdminConsumer(RestClient.Builder restClientBuilder,
								DokdistdpoProperties dokdistdpoProperties,
								@Qualifier("authorizedClientManager") OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
		this.restClient = restClientBuilder
				.baseUrl(dokdistdpoProperties.endpoints().dokdistadmin().url())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.requestInterceptor(new OAuth2TokenInterceptor(oAuth2AuthorizedClientManager, CLIENT_REGISTRATION_DOKDISTADMIN))
				.build();
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public HentForsendelseResponse hentForsendelse(String forsendelseId) {
		try {
			return restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/{forsendelseId}")
							.build(forsendelseId))
					.retrieve()
					.body(HentForsendelseResponse.class);
		} catch (RestClientResponseException e) {
			if (e.getStatusCode().is4xxClientError()) {
				throw new DokdistdpoFunctionalException(format("hentForsendelse feilet funksjonelt med forsendelseID=%s. Feilmelding=%s", forsendelseId, e.getStatusText()), e);
			}
			throw new DokdistdpoTechnicalException(format("hentForsendelse feilet teknisk med forsendelseId=%s, feilmelding=%s",
					forsendelseId, e.getStatusText()), e);
		}
	}
}
