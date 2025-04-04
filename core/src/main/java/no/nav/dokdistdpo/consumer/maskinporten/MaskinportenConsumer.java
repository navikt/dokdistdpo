package no.nav.dokdistdpo.consumer.maskinporten;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.config.properties.MaskinportenProperties;
import no.nav.dokdistdpo.exception.functional.MaskinportenFunctionalException;
import no.nav.dokdistdpo.exception.technical.MaskinportenTechnicalException;
import no.nav.dokdistdpo.exception.technical.SertifikatException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nimbusds.jose.JOSEObjectType.JWT;
import static com.nimbusds.jose.JWSAlgorithm.RS256;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.DEFAULT_ZONE_ID;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.consumer.maskinporten.Consumer.Authority.ISO_6523_ACTORID_UPIS;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Component
public class MaskinportenConsumer {

	private static final String TEKNISK_FEIL_MELDING = "Klarte ikke hente AccessToken fra maskinporten. Teknisk feil: ";
	private static final String FUNKSJONELL_FEIL_MELDING = "Klarte ikke hente AccessToken fra maskinporten. Funksjonell feil: ";

	private final MaskinportenProperties maskinportenProperties;
	private final RestClient restClient;

	public MaskinportenConsumer(MaskinportenProperties maskinportenProperties,
								RestClient.Builder restClientBuilder) {
		this.maskinportenProperties = maskinportenProperties;
		this.restClient = restClientBuilder
				.baseUrl(maskinportenProperties.tokenEndpoint())
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
	}

	public OidcTokenResponse fetchMaskinportenToken() {
		LinkedMultiValueMap<String, String> attrMap = new LinkedMultiValueMap<>();
		attrMap.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
		attrMap.add("assertion", generateJWT());

		try {
			return restClient.post()
					.body(attrMap)
					.retrieve()
					.onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
						throw new MaskinportenTechnicalException(TEKNISK_FEIL_MELDING + res.getStatusText());
					})
					.body(OidcTokenResponse.class);
		} catch (RestClientResponseException e) {
			if (e.getStatusCode().is4xxClientError()) {
				log.warn(FUNKSJONELL_FEIL_MELDING + "{}", e.getMessage());
				throw new MaskinportenFunctionalException(FUNKSJONELL_FEIL_MELDING + e.getMessage(), e);
			}
			throw new MaskinportenTechnicalException(TEKNISK_FEIL_MELDING + e.getMessage());
		}
	}

	private String generateJWT() {
		JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
				.audience(maskinportenProperties.issuer())
				.issuer(maskinportenProperties.clientId())
				.claim("scope", getCurrentScope())
				.claim("consumer", Consumer.builder()
						.id(NAV_ORGNUMMER)
						.authority(ISO_6523_ACTORID_UPIS.getValue())
						.build())
				.jwtID(UUID.randomUUID().toString())
				.issueTime(Date.from(OffsetDateTime.now(DEFAULT_ZONE_ID).toInstant()))
				.expirationTime(Date.from(OffsetDateTime.now(DEFAULT_ZONE_ID).toInstant()))
				.build();

		return createSignedJWT(maskinportenProperties.clientJwk(), jwtClaims).serialize();
	}

	private String getCurrentScope() {
		return Stream.of(maskinportenProperties.scopes()).collect(Collectors.joining(" "));
	}

	private SignedJWT createSignedJWT(String rsaJwk, JWTClaimsSet claimsSet) {
		try {
			var rsakey = RSAKey.parse(rsaJwk);
			JWSHeader header = new JWSHeader.Builder(RS256)
					.keyID(rsakey.getKeyID())
					.type(JWT)
					.build();
			SignedJWT signedJWT = new SignedJWT(header, claimsSet);
			JWSSigner signer = new RSASSASigner(rsakey);
			signedJWT.sign(signer);

			return signedJWT;
		} catch (ParseException | JOSEException e) {
			throw new SertifikatException("Klarte ikke å generere signert JWT", e);
		}
	}
}
