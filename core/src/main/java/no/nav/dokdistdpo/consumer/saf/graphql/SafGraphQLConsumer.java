package no.nav.dokdistdpo.consumer.saf.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.consumer.saf.SafJournalpost;
import no.nav.dokdistdpo.consumer.saf.SafJournalpostResponse;
import no.nav.dokdistdpo.exception.functional.SafJournalpostIkkeFunnetException;
import no.nav.dokdistdpo.exception.technical.SafJournalpostQueryTechnicalException;
import no.nav.dokdistdpo.exception.technical.SafJournalpostTechnicalException;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.lang.String.format;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_SAF;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class SafGraphQLConsumer {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public SafGraphQLConsumer(RestClient.Builder restClientBuilder,
							  DokdistdpoProperties dokdistdpoProperties) {
		this.restClient = restClientBuilder
				.baseUrl(dokdistdpoProperties.endpoints().saf().url())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
		this.objectMapper = new ObjectMapper();
	}

	@Retryable(retryFor = SafJournalpostTechnicalException.class)
	public SafJournalpost hentJournalpost(SafGraphQLRequest safGraphQLRequest) {

		SafJournalpostResponse safJournalpostResponse = restClient.post()
				.uri("/graphql")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_SAF))
				.body(convertToJsonString(safGraphQLRequest))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					if (res.getStatusCode().is4xxClientError()) {
						throw new SafJournalpostIkkeFunnetException(format("Henting av journalposten feilet funksjonelt med status=%s og feilmelding=%s ", res.getStatusCode(), res.getStatusText()));
					}
					throw new SafJournalpostQueryTechnicalException(format("SAF graphql teknisk feil med status=%s og feilmelding=%s ", res.getStatusCode(), res.getStatusText()));
				}).body(SafJournalpostResponse.class);

		return safJournalpost(safJournalpostResponse);
	}

	private SafJournalpost safJournalpost(SafJournalpostResponse safJournalpostResponse) {
		if (safJournalpostResponse == null || !isEmpty(safJournalpostResponse.error())) {
			throw new SafJournalpostQueryTechnicalException("Ingen journalpost ble funnet i saf med feilmedling= " + safJournalpostResponse.error());
		}
		return safJournalpostResponse.getJournalpost();
	}

	private String convertToJsonString(SafGraphQLRequest safGraphQLRequest) {
		try {
			return objectMapper.writeValueAsString(safGraphQLRequest);
		} catch (JsonProcessingException e) {
			throw new SafJournalpostQueryTechnicalException(format("Kunne ikke parse safGraphQLRequest med feilmelding=%s ", safGraphQLRequest), e);
		}
	}
}
