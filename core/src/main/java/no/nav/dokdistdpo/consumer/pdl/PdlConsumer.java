package no.nav.dokdistdpo.consumer.pdl;

import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.functional.PdlFunctionalException;
import no.nav.dokdistdpo.exception.functional.PersonIkkeFunnetException;
import no.nav.dokdistdpo.exception.technical.PdlTechnicalException;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_PDL;
import static no.nav.dokdistdpo.consumer.pdl.PdlResponseMapper.mapPdlResponse;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.getProblemDetail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Component
public class PdlConsumer {

	// https://pdldocs-navno.msappproxy.net/ekstern/index.html#_dokumenter_hjemmel
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "behandlingsnummer";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B315";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final RestClient restClient;

	public PdlConsumer(RestClient restClient,
					   DokdistdpoProperties dokdistdpoProperties) {
		this.restClient = restClient.mutate()
				.baseUrl(dokdistdpoProperties.endpoints().pdl().url())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setContentType(APPLICATION_JSON);
					httpHeaders.set(HEADER_PDL_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER);
				})
				.build();
	}

	public HentPersonInfo hentPersonInfo(final String ident) {
		var hentPersonResponse = restClient.post()
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PDL))
				.body(mapRequest(ident))
				.exchange((req, res) -> {
					if (res.getStatusCode().isError()) {
						ProblemDetail problemDetail = getProblemDetail(res.getBody());
						if (res.getStatusCode().is4xxClientError()) {
							throw new PdlFunctionalException(format("hentPersonInfo feilet funksjonelt med feilmelding=%s",
									res.getStatusText()), problemDetail);
						}
						throw new PdlTechnicalException(format("hentPersonInfo feilet teknisk med feilmelding=%s",
								res.getStatusText()), problemDetail);
					}
					return res.bodyTo(PdlHentPersonResponse.class);
				});

		return mapToHentPersonInfo(hentPersonResponse);
	}

	private HentPersonInfo mapToHentPersonInfo(PdlHentPersonResponse pdlHentPerson) {
		if (isNull(pdlHentPerson) || pdlHentPerson.errors().isEmpty()) {
			return mapPdlResponse(pdlHentPerson);
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlHentPerson.errors().getFirst().extension().code())) {
				throw new PersonIkkeFunnetException("Fant ikke person in PDL.");
			}
			throw new PdlFunctionalException("Kunne ikke hente person i PDL. " + pdlHentPerson.errors(), null);
		}
	}

	private PDLRequest mapRequest(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);

		return PDLRequest.builder()
				.query(HENT_PERSON_GRAPHQL_QUERY)
				.variables(variables)
				.build();
	}

	private static final String HENT_PERSON_GRAPHQL_QUERY = """
			query hentPerson($ident: ID!){
			  hentPerson(ident: $ident){
			    navn(historikk: false){
			      fornavn
			      mellomnavn
			      etternavn
			      forkortetNavn
			    }
			    folkeregisteridentifikator(historikk: false){
			      identifikasjonsnummer
			      type
			      status
			    }
			  }
			}
			""";
}
