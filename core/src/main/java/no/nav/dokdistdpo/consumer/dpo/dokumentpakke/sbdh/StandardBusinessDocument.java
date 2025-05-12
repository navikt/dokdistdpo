package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandardBusinessDocument {

	@NotNull
	StandardBusinessDocumentHeader standardBusinessDocumentHeader;

	@NotNull
	@JsonAlias({"avtalt", "status", "arkivmelding"})
	Object any;

}
