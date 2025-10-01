package no.nav.dokdistdpo.consumer.dpo.maskinporten;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.config.properties.MaskinportenProperties;
import no.nav.dokdistdpo.exception.functional.MaskinportenFunctionalException;
import no.nav.dokdistdpo.exception.technical.JwtSerializeException;
import no.nav.dokdistdpo.exception.technical.MaskinportenTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.nimbusds.jose.util.StandardCharset.UTF_8;
import static java.util.Date.from;
import static no.nav.dokdistdpo.config.cache.LocalCacheConfig.MASKINPORTEN_CACHE;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.DEFAULT_ZONE_ID;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.consumer.dpo.maskinporten.Authority.ISO_6523_ACTORID_UPIS;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.util.StreamUtils.copyToString;

@Slf4j
@Component
public class MaskinportenConsumer {

	public static final String FUNKSJONELL_FEIL_ERROR_MESSAGE = "Klarte ikke hente AccessToken fra maskinporten. Funksjonell feil: ";
	public static final String TEKNISK_FEIL_ERROR_MESSAGE = "Klarte ikke hente AccessToken fra maskinporten. Teknisk feil: ";
	private static final String SERTIFIKAT_ENCODING_FEIL = "Kunne ikke enkode sertifikat";
	private static final String SIGNERING_FEIL = "Feil ved signering av JWT";

	private final MaskinportenProperties maskinportenProperties;
	private final RestClient restClient;
	private final DokdistdpoProperties.DpoUserProperties dpoProperties;
	private final AppCertificate appCertificate;

	public MaskinportenConsumer(MaskinportenProperties maskinportenProperties,
								RestClient.Builder restClientBuilder,
								DokdistdpoProperties dokdistdpoProperties,
								AppCertificate appCertificate) {
		this.maskinportenProperties = maskinportenProperties;
		this.restClient = restClientBuilder
				.baseUrl(maskinportenProperties.tokenEndpoint())
				.defaultHeaders(headers -> headers.setContentType(APPLICATION_FORM_URLENCODED))
				.build();
		this.dpoProperties = dokdistdpoProperties.dpo();
		this.appCertificate = appCertificate;
	}

	@Cacheable(MASKINPORTEN_CACHE)
	public String getMaskinportenToken() {

		LinkedMultiValueMap<String, String> attrMap = new LinkedMultiValueMap<>();
		attrMap.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
		attrMap.add("assertion", getSignedJWT());

		TokenResponse tokenResponse = restClient.post()
				.body(attrMap)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (request, res) -> handleError(res))
				.body(TokenResponse.class);

		return tokenResponse.accessToken();
	}

	private String getSignedJWT() {
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.audience(maskinportenProperties.issuer())
				.issuer(dpoProperties.clientid())
				.claim("scope", getCurrentScopes(dpoProperties.scope()))
				.claim("consumer", Consumer.builder()
						.authority(ISO_6523_ACTORID_UPIS.getValue())
						.id(NAV_ORGNUMMER)
						.build())
				.jwtID(UUID.randomUUID().toString())
				.issueTime(from(OffsetDateTime.now(DEFAULT_ZONE_ID).toInstant()))
				.expirationTime(from(OffsetDateTime.now(DEFAULT_ZONE_ID).toInstant().plusSeconds(30)))
				.build();
		return generateSignedJWTFromCertificate(appCertificate, claims);

	}

	private String getCurrentScopes(String scope) {
		ArrayList<String> scopeList = new ArrayList<>();
		scopeList.add(scope);
		return scopeList.stream()
				.reduce((a, b) -> a + " " + b).orElse("");
	}

	public static String generateSignedJWTFromCertificate(AppCertificate appCertificate, JWTClaimsSet claims) {
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
				.x509CertChain(List.of(encodeCertificate(appCertificate)))
				.build();

		RSASSASigner signer = new RSASSASigner(appCertificate.getPrivateKey());

		SignedJWT signedJWT = new SignedJWT(header, claims);
		try {
			signedJWT.sign(signer);
			return signedJWT.serialize();
		} catch (JOSEException e) {
			log.error(SIGNERING_FEIL, e);
			throw new JwtSerializeException(SIGNERING_FEIL, e);
		}
	}

	private static Base64 encodeCertificate(AppCertificate appCertificate) {
		try {
			return Base64.encode(appCertificate.getX509Certificate().getEncoded());
		} catch (CertificateEncodingException e) {
			log.error(SERTIFIKAT_ENCODING_FEIL, e);
			throw new JwtSerializeException(SERTIFIKAT_ENCODING_FEIL, e);
		}
	}

	private void handleError(ClientHttpResponse response) throws IOException {
		String errorMelding = copyToString(response.getBody(), UTF_8);
		if (response.getStatusCode().is4xxClientError()) {
			throw new MaskinportenFunctionalException(FUNKSJONELL_FEIL_ERROR_MESSAGE + errorMelding);
		}
		throw new MaskinportenTechnicalException(TEKNISK_FEIL_ERROR_MESSAGE + errorMelding);
	}

}
