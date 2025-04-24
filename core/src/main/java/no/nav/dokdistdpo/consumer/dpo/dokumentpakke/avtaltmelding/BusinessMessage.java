package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding;

import lombok.Data;

@Data
public abstract class BusinessMessage<T extends BusinessMessage<T>> {
	private Integer sikkerhetsnivaa;
	private String hoveddokument;
}
