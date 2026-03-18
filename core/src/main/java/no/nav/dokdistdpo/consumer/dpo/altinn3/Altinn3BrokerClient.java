package no.nav.dokdistdpo.consumer.dpo.altinn3;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.altinn.services.altinn3.domain.FileTransferInitalizeExt;
import no.altinn.services.altinn3.domain.FileTransferInitializeResponseExt;
import no.altinn.services.altinn3.domain.FileTransferUploadResponseExt;
import no.altinn.services.altinn3.domain.ProblemDetails;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.functional.Altinn3BrokerFunctionalException;
import no.nav.dokdistdpo.exception.technical.Altinn3BrokerTechnicalException;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.util.StreamUtils.copyToByteArray;

@Component
public class Altinn3BrokerClient {

	private static final String ALTINN3_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";

	private final RestClient altinn3AuthorizeRestClient;
	private final ObjectMapper objectMapper;

	public Altinn3BrokerClient(RestClient altinn3AuthorizeRestClient,
							   DokdistdpoProperties dokdistdpoProperties,
							   ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.altinn3AuthorizeRestClient = altinn3AuthorizeRestClient.mutate()
				.baseUrl(dokdistdpoProperties.altinn3().url())
				.defaultHeader(ALTINN3_SUBSCRIPTION_KEY_HEADER, dokdistdpoProperties.altinn3().apiSubscriptionKey())
				.build();
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public FileTransferInitializeResponseExt intiateFileTransfer(FileTransferInitalizeExt fileTransferInitalizeExt) {
		return altinn3AuthorizeRestClient.post()
				.uri("/broker/api/v1/filetransfer")
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON)
				.body(fileTransferInitalizeExt)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (reg, res) ->
						handleError(res, "intiateFileTransfer feilet med feilmelding=%s")
				)
				.body(FileTransferInitializeResponseExt.class);
	}

	@Retryable(retryFor = DokdistdpoTechnicalException.class)
	public FileTransferUploadResponseExt uploadFileTransfer(UUID fileTransferId, InputStream inputStream) {
		try {
			return altinn3AuthorizeRestClient.post()
					.uri("/broker/api/v1/filetransfer/{fileTransferId}/upload", fileTransferId)
					.accept(APPLICATION_JSON)
					.contentType(APPLICATION_OCTET_STREAM)
					.body(copyToByteArray(inputStream))
					.retrieve()
					.onStatus(HttpStatusCode::isError, (reg, res) ->
							handleError(res, "uploadFileTransfer feilet med feilmelding=%s")
					)
					.body(FileTransferUploadResponseExt.class);
		} catch (IOException e) {
			throw new Altinn3BrokerTechnicalException("Feil ved lesing av inputstream for uploadFileTransfer", e);
		}

	}

	private void handleError(ClientHttpResponse response, String feilmelding) throws IOException {
		ProblemDetails problemDetails = objectMapper.readValue(response.getBody(), ProblemDetails.class);
		if (response.getStatusCode().is4xxClientError()) {
			throw new Altinn3BrokerFunctionalException(feilmelding.formatted(problemDetails));
		}
		throw new Altinn3BrokerTechnicalException(feilmelding.formatted(problemDetails));
	}
}
