package no.nav.dokdistdpo.consumer.dpo.packaging.asice;

import lombok.extern.slf4j.Slf4j;
import no.difi.asic.AsicWriter;
import no.difi.asic.AsicWriterFactory;
import no.difi.asic.SignatureHelper;
import no.nav.dokdistdpo.certificate.AppCertificate;
import no.nav.dokdistdpo.consumer.dpo.NavDokument;
import no.nav.dokdistdpo.consumer.dpo.NavDokumentpakke;
import no.nav.dokdistdpo.consumer.dpo.altinn3.AltinnDpoRequest;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.XmlManifestCreator;
import no.nav.dokdistdpo.exception.functional.DokumentpakkingException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static no.difi.asic.MimeType.XML;
import static no.difi.asic.MimeType.forString;
import static no.nav.dokdistdpo.utils.DokdistdpoUtils.toBufferedStream;

@Slf4j
@Component
public class AsiceCreator {

	static final String MANIFEST_XML = "manifest.xml";

	private final XmlManifestCreator xmlManifestCreator;

	public AsiceCreator() {
		this.xmlManifestCreator = new XmlManifestCreator();
	}

	public ByteArrayOutputStream createAsiceStreamed(AltinnDpoRequest altinnDpoRequest,
													 AppCertificate appCertificate) throws IOException {
		ByteArrayOutputStream asicArchive = new ByteArrayOutputStream();
		String manifest = xmlManifestCreator.createManifest(altinnDpoRequest);
		NavDokumentpakke navDokumentpakke = altinnDpoRequest.navDokumentpakke();

		AsicWriter asicWriter = AsicWriterFactory.newFactory()
				.newContainer(asicArchive)
				.add(toBufferedStream(manifest.getBytes()), MANIFEST_XML, XML);

		try (InputStream meldingInputstream = toBufferedStream(navDokumentpakke.navDokument().innhold())) {
			asicWriter.add(meldingInputstream, navDokumentpakke.navDokument().filnavn(), forString(navDokumentpakke.navDokument().mimeType()));
			navDokumentpakke.navDokumenter().forEach(dok -> addDocumentToAsic(asicWriter, dok));
		}

		asicWriter.sign(new DefaultSignatureHelper(appCertificate));
		return asicArchive;
	}

	private void addDocumentToAsic(AsicWriter asicWriter, NavDokument dokument) {
		try (InputStream inputStream = toBufferedStream(dokument.innhold())) {
			if (log.isDebugEnabled()) {
				log.debug("Adding file {} of type {}", dokument.filnavn(), dokument.mimeType());
			}
			asicWriter.add(inputStream, dokument.filnavn(), forString(dokument.mimeType()));
		} catch (IOException e) {
			throw new DokumentpakkingException("Kunne ikke pakke navDokument: " + dokument.filnavn(), e);
		}
	}

	private static class DefaultSignatureHelper extends SignatureHelper {
		DefaultSignatureHelper(AppCertificate appCertificate) {
			super(appCertificate.getKeyStore(),
					appCertificate.getCredentials().alias(),
					appCertificate.getCredentials().password());
		}
	}

}
