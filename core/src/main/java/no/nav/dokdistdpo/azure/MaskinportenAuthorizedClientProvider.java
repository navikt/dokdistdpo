package no.nav.dokdistdpo.azure;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import no.nav.dokdistdpo.config.properties.MaskinportenProperties;
import no.nav.dokdistdpo.exception.technical.DokdistdpoTechnicalException;
import org.springframework.security.oauth2.client.JwtBearerOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.RestClientJwtBearerTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static no.nav.dokdistdpo.azure.OAuthEnabledRestClientConfig.CLIENT_REGISTRATION_MASKINPORTEN;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;

@Component
public class MaskinportenAuthorizedClientProvider implements OAuth2AuthorizedClientProvider {

	private final JwtBearerOAuth2AuthorizedClientProvider jwtBearerAuthorizedClientProvider;

	public MaskinportenAuthorizedClientProvider(MaskinportenProperties maskinportenProperties) {
		this.jwtBearerAuthorizedClientProvider = createMaskinportenJwtBearerAuthorizedClientProvider(maskinportenProperties);
	}

	@Override
	public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
		return jwtBearerAuthorizedClientProvider.authorize(context);
	}


	private JwtBearerOAuth2AuthorizedClientProvider createMaskinportenJwtBearerAuthorizedClientProvider(MaskinportenProperties maskinportenProperties) {
		try {
			JWK maskinportenClientJwk = RSAKey.parse(maskinportenProperties.clientJwk());
			JWKSource<SecurityContext> maskinportenClientJwkSource = new ImmutableJWKSet<>(new JWKSet(maskinportenClientJwk));

			var jwtBearerAuthorizedClientProvider = new JwtBearerOAuth2AuthorizedClientProvider();

			var restClientJwtBearerTokenResponseClient = new RestClientJwtBearerTokenResponseClient();
			restClientJwtBearerTokenResponseClient.setParametersConverter(new DefaultOAuth2TokenRequestParametersConverter<>());
			jwtBearerAuthorizedClientProvider.setJwtAssertionResolver(oAuth2AuthorizationContext ->
					resolveJwtAssertion(oAuth2AuthorizationContext, maskinportenClientJwkSource).orElse(null));
			jwtBearerAuthorizedClientProvider.setAccessTokenResponseClient(restClientJwtBearerTokenResponseClient);
			return jwtBearerAuthorizedClientProvider;
		} catch (ParseException e) {
			throw new DokdistdpoTechnicalException("Failed to parse JWK for Maskinporten client", e);
		}
	}

	private static Optional<Jwt> resolveJwtAssertion(OAuth2AuthorizationContext context, JWKSource<SecurityContext> maskinportenClientJwkSource) {
		ClientRegistration clientRegistration = context.getClientRegistration();

		if (CLIENT_REGISTRATION_MASKINPORTEN.equals(clientRegistration.getRegistrationId())) {
			JwsHeader.Builder headersBuilder = JwsHeader.with(RS256);

			Instant issuedAt = Instant.now();
			Instant expiresAt = issuedAt.plus(Duration.ofSeconds(60));

			JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
					.issuer(clientRegistration.getClientId())
					.audience(singletonList(clientRegistration.getProviderDetails().getIssuerUri()))
					.id(UUID.randomUUID().toString())
					.issuedAt(issuedAt)
					.expiresAt(expiresAt)
					.claim("scope", clientRegistration.getScopes().stream().reduce((a, b) -> a + " " + b).orElse(""));

			NimbusJwtEncoder nimbusJwtEncoder = new NimbusJwtEncoder(maskinportenClientJwkSource);
			Jwt jws = nimbusJwtEncoder.encode(JwtEncoderParameters.from(headersBuilder.build(), claimsBuilder.build()));
			return Optional.of(jws);
		}
		return Optional.empty();
	}

}
