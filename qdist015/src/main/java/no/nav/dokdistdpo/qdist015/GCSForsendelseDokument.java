package no.nav.dokdistdpo.qdist015;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GCSForsendelseDokument {
	private byte[] pdf;
	private String dokumentObjektReferanse;
	private String dokumentInfoId;
	private String journalpostId;
}
