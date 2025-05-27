package no.nav.dokdistdpo.consumer.dpo.dokumentpakke;

import no.nav.dokdistdpo.consumer.dpo.altinnbrokerservice.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.manifest.Avsender;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.manifest.HovedDokument;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.manifest.Manifest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.manifest.Mottaker;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.manifest.Organisasjon;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;

public class XmlManifestCreator {

	private static final String HOVEDDOKUMENT = "Hoveddokument";
	private static final String HOVEDDOKUMENT_LANG = "no";

	public String createManifest(final AltinnDpoRequest altinnDpoRequest) {
		NavDokumentpakke navDokumentpakke = altinnDpoRequest.navDokumentpakke();
		Avsender avsender = new Avsender(new Organisasjon(NAV_ORGNUMMER));
		Mottaker mottaker = new Mottaker(new Organisasjon(altinnDpoRequest.forsendelse().mottakerId()));
		HovedDokument hoveddokumentXml = HovedDokument.builder()
				.href(navDokumentpakke.navDokument().filnavn())
				.mime(navDokumentpakke.navDokument().mimeType())
				.tittel(HovedDokument.Tittel.builder()
						.tittel(HOVEDDOKUMENT)
						.lang(HOVEDDOKUMENT_LANG)
						.build())
				.build();

		Manifest xmlManifest = new Manifest(mottaker, avsender, hoveddokumentXml);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		MarshalManifest.marshal(xmlManifest, os);

		return os.toString(UTF_8);
	}
}
