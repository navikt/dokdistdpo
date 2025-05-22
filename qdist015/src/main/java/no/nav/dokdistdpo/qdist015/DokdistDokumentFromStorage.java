package no.nav.dokdistdpo.qdist015;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DokdistDokumentFromStorage {
		private byte[] pdf;
		private String dokumentObjektReferanse;
		private String dokumentInfoId;
}
