package no.nav.dokdistdpo.qdist015.map;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ArkivInformasjon;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Mottaker;
import no.nav.dokdistdpo.consumer.dpo.altinn3.AltinnDpoRequest.Forsendelse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Base64;
import java.util.stream.Stream;

import static no.nav.dokdistdpo.qdist015.itest.config.AbstractQdist015ITest.classpathToString;
import static org.assertj.core.api.Assertions.assertThat;

class AltinnRequestMapperTest {

	private static final String KONVERSASJON_ID = "1234627b-b791-4d5f-923e-5fb132839e5f";

	private final AltinnRequestMapper altinnRequestMapper = new AltinnRequestMapper();

	@ParameterizedTest
	@MethodSource
	void skalMappe(byte[] forsendelseMetadata, String forventetForsendelseMetadata) {
		HentForsendelseResponse hentForsendelseResponse = lagMinimumHentForsendelseResponse(forsendelseMetadata);

		Forsendelse forsendelse = altinnRequestMapper.mapForsendelse(KONVERSASJON_ID, hentForsendelseResponse);

		assertThat(forsendelse.bestillingsId()).isEqualTo("a1c54241-ddad-4275-886a-f96c6b4cf0dd");
		assertThat(forsendelse.konversjonsId()).isEqualTo(KONVERSASJON_ID);
		assertThat(forsendelse.journalpostId()).isEqualTo("453581689");
		assertThat(forsendelse.mottakerId()).isEqualTo("19019920173");
		assertThat(forsendelse.organisasjonsnavn()).isEqualTo("ROBUST TRANFLASKE");
		assertThat(forsendelse.forsendelseMetadata()).isEqualTo(forventetForsendelseMetadata);
		assertThat(forsendelse.meldingType()).isEqualTo("DPO_AVTALEMELDING");
	}

	private static Stream<Arguments> skalMappe() {
		return Stream.of(
				Arguments.of(Base64.getDecoder().decode(classpathToString("__files/forsendelsemetadata/base64EnkodetForsendelseMetadata.txt")), forsendelseMetadata()),
				Arguments.of(null, null)
		);
	}

	private HentForsendelseResponse lagMinimumHentForsendelseResponse(byte[] forsendelseMetadata) {
		ArkivInformasjon arkivInformasjon = new ArkivInformasjon("453581689", "JOARK");
		Mottaker mottaker = new Mottaker("19019920173", "ROBUST TRANFLASKE", null);

		return HentForsendelseResponse.builder()
				.bestillingsId("a1c54241-ddad-4275-886a-f96c6b4cf0dd")
				.arkivInformasjon(arkivInformasjon)
				.mottaker(mottaker)
				.forsendelseMetadata(forsendelseMetadata)
				.forsendelseMetadataType("DPO_AVTALEMELDING")
				.build();
	}

	private static String forsendelseMetadata() {
		return classpathToString("__files/forsendelsemetadata/forsendelsemetadata.xml");
	}

}