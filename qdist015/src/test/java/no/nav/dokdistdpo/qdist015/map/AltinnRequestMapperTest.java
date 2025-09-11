package no.nav.dokdistdpo.qdist015.map;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.ArkivInformasjon;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.dokdistadmin.domain.Mottaker;
import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest.Forsendelse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Base64;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AltinnRequestMapperTest {

	private static final String KONVERSASJON_ID = "1234627b-b791-4d5f-923e-5fb132839e5f";

	AltinnRequestMapper altinnRequestMapper = new AltinnRequestMapper();

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
				Arguments.of(Base64.getDecoder().decode("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGFya2l2bWVsZGluZz4KCTxzeXN0ZW0+ZG9rZGlzdGVmb3JtaWRsaW5nPC9zeXN0ZW0+Cgk8bWVsZGluZ0lkPmJlc3RpbGxpbmdzSWQ8L21lbGRpbmdJZD4KCTx0aWRzcHVua3Q+MjAyNS0wMy0wNVQxNjoyNDowNS4yNDcrMDE6MDA8L3RpZHNwdW5rdD4KCTxhbnRhbGxGaWxlcj4yPC9hbnRhbGxGaWxlcj4KCTxtYXBwZSB4c2k6dHlwZT0ic2Frc21hcHBlIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIj4KCQk8dGl0dGVsPkRhZ3BlbmdlcjwvdGl0dGVsPgoJCTxvcHByZXR0ZXREYXRvPjIwMjUtMDMtMTVUMTM6MTU6MzAuMDE8L29wcHJldHRldERhdG8+CgkJPG9wcHJldHRldEF2PlNhayBTYWtiZWhhbmRsZXJzZW48L29wcHJldHRldEF2PgoJCTx2aXJrc29taGV0c3NwZXNpZmlra2VNZXRhZGF0YSB4c2k6dHlwZT0ieHM6c3RyaW5nIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiPjExMTExMQoJCTwvdmlya3NvbWhldHNzcGVzaWZpa2tlTWV0YWRhdGE+CgkJPHBhcnQ+CgkJCTxwYXJ0TmF2bj5OQVYgS2xhZ2VpbnN0YW5zPC9wYXJ0TmF2bj4KCQkJPHBhcnRSb2xsZT5BTVA8L3BhcnRSb2xsZT4KCQkJPG9yZ2FuaXNhc2pvbnNudW1tZXI+CgkJCQk8b3JnYW5pc2Fzam9uc251bW1lcj45OTEwNzgwNDU8L29yZ2FuaXNhc2pvbnNudW1tZXI+CgkJCTwvb3JnYW5pc2Fzam9uc251bW1lcj4KCQkJPGtvbnRha3RwZXJzb24+U2FrIFNha2JlaGFuZGxlcnNlbjwva29udGFrdHBlcnNvbj4KCQk8L3BhcnQ+CgkJPHBhcnQ+CgkJCTxwYXJ0TmF2bj5CamFybmUgQmV0amVudDwvcGFydE5hdm4+CgkJCTxwYXJ0Um9sbGU+REFQPC9wYXJ0Um9sbGU+CgkJCTxvcmdhbmlzYXNqb25zbnVtbWVyLz4KCQkJPGZvZWRzZWxzbnVtbWVyPgoJCQkJPGZvZWRzZWxzbnVtbWVyPjIwMDI2OTAwMDAwPC9mb2Vkc2Vsc251bW1lcj4KCQkJPC9mb2Vkc2Vsc251bW1lcj4KCQk8L3BhcnQ+CgkJPHJlZ2lzdHJlcmluZyB4c2k6dHlwZT0iam91cm5hbHBvc3QiPgoJCQk8b3BwcmV0dGV0RGF0bz4yMDI1LTAzLTE0VDEzOjE1OjMwLjAxPC9vcHByZXR0ZXREYXRvPgoJCQk8b3BwcmV0dGV0QXY+U2FrIFNha2JlaGFuZGxlcnNlbjwvb3BwcmV0dGV0QXY+CgkJCTxkb2t1bWVudGJlc2tyaXZlbHNlPgoJCQkJPGRva3VtZW50dHlwZT5Eb2t1bWVudGFzam9uPC9kb2t1bWVudHR5cGU+CgkJCQk8ZG9rdW1lbnRzdGF0dXM+RG9rdW1lbnRldCBlciBmZXJkaWdzdGlsdDwvZG9rdW1lbnRzdGF0dXM+CgkJCQk8dGl0dGVsPktsYWdlIHDDpSBzYWtzYmVoYW5kbGluZzwvdGl0dGVsPgoJCQkJPG9wcHJldHRldERhdG8+MjAyNS0wMy0xM1QxMzoxNTozMC4wMTwvb3BwcmV0dGV0RGF0bz4KCQkJCTxvcHByZXR0ZXRBdj5TYWsgU2FrYmVoYW5kbGVyc2VuPC9vcHByZXR0ZXRBdj4KCQkJCTx0aWxrbnl0dGV0UmVnaXN0cmVyaW5nU29tPkhvdmVkZG9rdW1lbnQ8L3RpbGtueXR0ZXRSZWdpc3RyZXJpbmdTb20+CgkJCQk8ZG9rdW1lbnRudW1tZXI+MTwvZG9rdW1lbnRudW1tZXI+CgkJCQk8dGlsa255dHRldERhdG8+MjAyNS0wMy0wNVQxNjoyNDowNS4yNDcrMDE6MDA8L3RpbGtueXR0ZXREYXRvPgoJCQkJPHRpbGtueXR0ZXRBdj5TYWsgU2FrYmVoYW5kbGVyc2VuPC90aWxrbnl0dGV0QXY+CgkJCQk8ZG9rdW1lbnRvYmpla3Q+CgkJCQkJPHZlcnNqb25zbnVtbWVyPjE8L3ZlcnNqb25zbnVtbWVyPgoJCQkJCTx2YXJpYW50Zm9ybWF0PkRva3VtZW50IGh2b3IgZGVsZXIgYXYgaW5uaG9sZGV0IGVyIHNramVybWV0PC92YXJpYW50Zm9ybWF0PgoJCQkJCTxvcHByZXR0ZXREYXRvPjIwMjUtMDMtMTNUMTM6MTU6MzAuMDE8L29wcHJldHRldERhdG8+CgkJCQkJPG9wcHJldHRldEF2PlNhayBTYWtiZWhhbmRsZXJzZW48L29wcHJldHRldEF2PgoJCQkJCTxyZWZlcmFuc2VEb2t1bWVudGZpbD45ODc2NTQzMjEtMTIzNDU2Ny1Eb2t1bWVudCBodm9yIGRlbGVyIGF2IGlubmhvbGRldCBlciBza2plcm1ldC1QREYKCQkJCQk8L3JlZmVyYW5zZURva3VtZW50ZmlsPgoJCQkJPC9kb2t1bWVudG9iamVrdD4KCQkJPC9kb2t1bWVudGJlc2tyaXZlbHNlPgoJCQk8ZG9rdW1lbnRiZXNrcml2ZWxzZT4KCQkJCTxkb2t1bWVudHR5cGU+RG9rdW1lbnRhc2pvbjwvZG9rdW1lbnR0eXBlPgoJCQkJPGRva3VtZW50c3RhdHVzPkRva3VtZW50ZXQgZXIgZmVyZGlnc3RpbHQ8L2Rva3VtZW50c3RhdHVzPgoJCQkJPHRpdHRlbD5Eb2t1bWVudGFzam9uIHRpbCBrbGFnZTwvdGl0dGVsPgoJCQkJPG9wcHJldHRldERhdG8+MjAyNS0wMy0xM1QxMzoxNTozMC4wMTwvb3BwcmV0dGV0RGF0bz4KCQkJCTxvcHByZXR0ZXRBdj5TYWsgU2FrYmVoYW5kbGVyc2VuPC9vcHByZXR0ZXRBdj4KCQkJCTx0aWxrbnl0dGV0UmVnaXN0cmVyaW5nU29tPlZlZGxlZ2c8L3RpbGtueXR0ZXRSZWdpc3RyZXJpbmdTb20+CgkJCQk8ZG9rdW1lbnRudW1tZXI+MjwvZG9rdW1lbnRudW1tZXI+CgkJCQk8dGlsa255dHRldERhdG8+MjAyNS0wMy0wNVQxNjoyNDowNS4yNDcrMDE6MDA8L3RpbGtueXR0ZXREYXRvPgoJCQkJPHRpbGtueXR0ZXRBdj5TYWsgU2FrYmVoYW5kbGVyc2VuPC90aWxrbnl0dGV0QXY+CgkJCQk8ZG9rdW1lbnRvYmpla3Q+CgkJCQkJPHZlcnNqb25zbnVtbWVyPjE8L3ZlcnNqb25zbnVtbWVyPgoJCQkJCTx2YXJpYW50Zm9ybWF0PkFya2l2Zm9ybWF0PC92YXJpYW50Zm9ybWF0PgoJCQkJCTxvcHByZXR0ZXREYXRvPjIwMjUtMDMtMTNUMTM6MTU6MzAuMDE8L29wcHJldHRldERhdG8+CgkJCQkJPG9wcHJldHRldEF2PlNhayBTYWtiZWhhbmRsZXJzZW48L29wcHJldHRldEF2PgoJCQkJCTxyZWZlcmFuc2VEb2t1bWVudGZpbD45ODc2NTQzMjEtNzY1NDMyMS1BcmtpdmZvcm1hdC1KUEVHPC9yZWZlcmFuc2VEb2t1bWVudGZpbD4KCQkJCTwvZG9rdW1lbnRvYmpla3Q+CgkJCTwvZG9rdW1lbnRiZXNrcml2ZWxzZT4KCQkJPHRpdHRlbD5LbGFnZSBww6Ugc2Frc2JlaGFuZGxpbmc8L3RpdHRlbD4KCQkJPGtvcnJlc3BvbmRhbnNlcGFydD4KCQkJCTxrb3JyZXNwb25kYW5zZXBhcnR0eXBlPk1vdHRha2VyPC9rb3JyZXNwb25kYW5zZXBhcnR0eXBlPgoJCQkJPGtvcnJlc3BvbmRhbnNlcGFydE5hdm4+VFJZR0RFUkVUVEVOPC9rb3JyZXNwb25kYW5zZXBhcnROYXZuPgoJCQkJPG9yZ2FuaXNhc2pvbnNudW1tZXI+CgkJCQkJPG9yZ2FuaXNhc2pvbnNudW1tZXI+OTc0NzYxMDg0PC9vcmdhbmlzYXNqb25zbnVtbWVyPgoJCQkJPC9vcmdhbmlzYXNqb25zbnVtbWVyPgoJCQk8L2tvcnJlc3BvbmRhbnNlcGFydD4KCQkJPGtvcnJlc3BvbmRhbnNlcGFydD4KCQkJCTxrb3JyZXNwb25kYW5zZXBhcnR0eXBlPkF2c2VuZGVyPC9rb3JyZXNwb25kYW5zZXBhcnR0eXBlPgoJCQkJPGtvcnJlc3BvbmRhbnNlcGFydE5hdm4+TkFWIEtsYWdlaW5zdGFuczwva29ycmVzcG9uZGFuc2VwYXJ0TmF2bj4KCQkJCTxvcmdhbmlzYXNqb25zbnVtbWVyPgoJCQkJCTxvcmdhbmlzYXNqb25zbnVtbWVyPjk3NDc2MTA4NDwvb3JnYW5pc2Fzam9uc251bW1lcj4KCQkJCTwvb3JnYW5pc2Fzam9uc251bW1lcj4KCQkJPC9rb3JyZXNwb25kYW5zZXBhcnQ+CgkJCTxqb3VybmFscG9zdHR5cGU+VXRnw6VlbmRlIGRva3VtZW50PC9qb3VybmFscG9zdHR5cGU+CgkJCTxqb3VybmFsc3RhdHVzPkVrc3BlZGVydDwvam91cm5hbHN0YXR1cz4KCQkJPGpvdXJuYWxkYXRvPjIwMjUtMDMtMTM8L2pvdXJuYWxkYXRvPgoJCTwvcmVnaXN0cmVyaW5nPgoJCTxzYWtzZGF0bz4yMDI1LTAzLTE1PC9zYWtzZGF0bz4KCQk8YWRtaW5pc3RyYXRpdkVuaGV0Pk5BViBLbGFnZWluc3RhbnM8L2FkbWluaXN0cmF0aXZFbmhldD4KCQk8c2Frc2Fuc3ZhcmxpZz5TYWsgU2FrYmVoYW5kbGVyc2VuPC9zYWtzYW5zdmFybGlnPgoJCTxzYWtzc3RhdHVzPlVuZGVyIGJlaGFuZGxpbmc8L3Nha3NzdGF0dXM+Cgk8L21hcHBlPgo8L2Fya2l2bWVsZGluZz4K"), forsendelseMetadata()),
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
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<arkivmelding>
					<system>dokdisteformidling</system>
					<meldingId>bestillingsId</meldingId>
					<tidspunkt>2025-03-05T16:24:05.247+01:00</tidspunkt>
					<antallFiler>2</antallFiler>
					<mappe xsi:type="saksmappe" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
						<tittel>Dagpenger</tittel>
						<opprettetDato>2025-03-15T13:15:30.01</opprettetDato>
						<opprettetAv>Sak Sakbehandlersen</opprettetAv>
						<virksomhetsspesifikkeMetadata xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema">111111
						</virksomhetsspesifikkeMetadata>
						<part>
							<partNavn>NAV Klageinstans</partNavn>
							<partRolle>AMP</partRolle>
							<organisasjonsnummer>
								<organisasjonsnummer>991078045</organisasjonsnummer>
							</organisasjonsnummer>
							<kontaktperson>Sak Sakbehandlersen</kontaktperson>
						</part>
						<part>
							<partNavn>Bjarne Betjent</partNavn>
							<partRolle>DAP</partRolle>
							<organisasjonsnummer/>
							<foedselsnummer>
								<foedselsnummer>20026900000</foedselsnummer>
							</foedselsnummer>
						</part>
						<registrering xsi:type="journalpost">
							<opprettetDato>2025-03-14T13:15:30.01</opprettetDato>
							<opprettetAv>Sak Sakbehandlersen</opprettetAv>
							<dokumentbeskrivelse>
								<dokumenttype>Dokumentasjon</dokumenttype>
								<dokumentstatus>Dokumentet er ferdigstilt</dokumentstatus>
								<tittel>Klage på saksbehandling</tittel>
								<opprettetDato>2025-03-13T13:15:30.01</opprettetDato>
								<opprettetAv>Sak Sakbehandlersen</opprettetAv>
								<tilknyttetRegistreringSom>Hoveddokument</tilknyttetRegistreringSom>
								<dokumentnummer>1</dokumentnummer>
								<tilknyttetDato>2025-03-05T16:24:05.247+01:00</tilknyttetDato>
								<tilknyttetAv>Sak Sakbehandlersen</tilknyttetAv>
								<dokumentobjekt>
									<versjonsnummer>1</versjonsnummer>
									<variantformat>Dokument hvor deler av innholdet er skjermet</variantformat>
									<opprettetDato>2025-03-13T13:15:30.01</opprettetDato>
									<opprettetAv>Sak Sakbehandlersen</opprettetAv>
									<referanseDokumentfil>987654321-1234567-Dokument hvor deler av innholdet er skjermet-PDF
									</referanseDokumentfil>
								</dokumentobjekt>
							</dokumentbeskrivelse>
							<dokumentbeskrivelse>
								<dokumenttype>Dokumentasjon</dokumenttype>
								<dokumentstatus>Dokumentet er ferdigstilt</dokumentstatus>
								<tittel>Dokumentasjon til klage</tittel>
								<opprettetDato>2025-03-13T13:15:30.01</opprettetDato>
								<opprettetAv>Sak Sakbehandlersen</opprettetAv>
								<tilknyttetRegistreringSom>Vedlegg</tilknyttetRegistreringSom>
								<dokumentnummer>2</dokumentnummer>
								<tilknyttetDato>2025-03-05T16:24:05.247+01:00</tilknyttetDato>
								<tilknyttetAv>Sak Sakbehandlersen</tilknyttetAv>
								<dokumentobjekt>
									<versjonsnummer>1</versjonsnummer>
									<variantformat>Arkivformat</variantformat>
									<opprettetDato>2025-03-13T13:15:30.01</opprettetDato>
									<opprettetAv>Sak Sakbehandlersen</opprettetAv>
									<referanseDokumentfil>987654321-7654321-Arkivformat-JPEG</referanseDokumentfil>
								</dokumentobjekt>
							</dokumentbeskrivelse>
							<tittel>Klage på saksbehandling</tittel>
							<korrespondansepart>
								<korrespondanseparttype>Mottaker</korrespondanseparttype>
								<korrespondansepartNavn>TRYGDERETTEN</korrespondansepartNavn>
								<organisasjonsnummer>
									<organisasjonsnummer>974761084</organisasjonsnummer>
								</organisasjonsnummer>
							</korrespondansepart>
							<korrespondansepart>
								<korrespondanseparttype>Avsender</korrespondanseparttype>
								<korrespondansepartNavn>NAV Klageinstans</korrespondansepartNavn>
								<organisasjonsnummer>
									<organisasjonsnummer>974761084</organisasjonsnummer>
								</organisasjonsnummer>
							</korrespondansepart>
							<journalposttype>Utgående dokument</journalposttype>
							<journalstatus>Ekspedert</journalstatus>
							<journaldato>2025-03-13</journaldato>
						</registrering>
						<saksdato>2025-03-15</saksdato>
						<administrativEnhet>NAV Klageinstans</administrativEnhet>
						<saksansvarlig>Sak Sakbehandlersen</saksansvarlig>
						<saksstatus>Under behandling</saksstatus>
					</mappe>
				</arkivmelding>
				""";
	}
}