package no.nav.dokdistdpo.qdist015;

import no.nav.dokdistdpo.consumer.dokdistadmin.domain.HentForsendelseResponse;
import no.nav.dokdistdpo.consumer.gcloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpo.exception.functional.KunneIkkeDeserialisereBucketJsonPayloadFunctionalException;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;
import static no.nav.dokdistdpo.qdist015.utils.JsonSerializer.deserialize;

@Component
public class GStorageDokumentService {

	private final EncryptedBucketStorage encryptedBucketStorage;

	public GStorageDokumentService(EncryptedBucketStorage encryptedBucketStorage) {
		this.encryptedBucketStorage = encryptedBucketStorage;
	}

	public List<GCSForsendelseDokument> hentDokumentFromGCStorage(HentForsendelseResponse response) {
		return response.dokumenter().stream()
				.map(dokument -> {
					String jsonPayload = encryptedBucketStorage.downloadObject(dokument.dokumentObjektReferanse(), response.bestillingsId());
					GCSForsendelseDokument dokumentFromStorage = deserializeJsonPayloadToDokument(jsonPayload, dokument.dokumentObjektReferanse());
					dokumentFromStorage.setDokumentInfoId(dokument.arkivDokumentInfoId());
					dokumentFromStorage.setJournalpostId(response.arkivInformasjon().arkivId());
					return dokumentFromStorage;
				}).toList();
	}

	private GCSForsendelseDokument deserializeJsonPayloadToDokument(String jsonPayload, String objektReferanse) {
		try {
			GCSForsendelseDokument dokumentFromStorage = deserialize(jsonPayload, GCSForsendelseDokument.class);
			dokumentFromStorage.setDokumentObjektReferanse(objektReferanse);
			return dokumentFromStorage;
		} catch (IllegalStateException e) {
			throw new KunneIkkeDeserialisereBucketJsonPayloadFunctionalException(format("Feil ved deserialisering av JSON-payload for dokument med dokumentobjektreferanse=%s. " +
					"Sørg for at payloaden er gyldig JSON format!", objektReferanse));
		}
	}
}
