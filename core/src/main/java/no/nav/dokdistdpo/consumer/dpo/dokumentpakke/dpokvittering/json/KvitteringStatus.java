package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.dpokvittering.json;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;

@Builder
public record KvitteringStatus(@JsonValue String status) {
}
