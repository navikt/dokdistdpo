package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.Arkivmelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.AvtaltMelding;

public class StandardBusinessDocumentSerializer extends StdSerializer<StandardBusinessDocument> {

    protected StandardBusinessDocumentSerializer() {
        super(StandardBusinessDocument.class);
    }

    @Override
    public void serialize(StandardBusinessDocument value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        gen.writeStartObject();
        gen.writeName("standardBusinessDocumentHeader");
        gen.writePOJO(value.getStandardBusinessDocumentHeader());
        if (value.getAny() instanceof AvtaltMelding) {
            gen.writeName("avtalt");
        } else if (value.getAny() instanceof Arkivmelding) {
            gen.writeName("arkivmelding");
        }
        else {
            throw new UnsupportedOperationException("Kun avtaltmelding og arkivmelding er støttet.");
        }
        gen.writePOJO(value.getAny());
        gen.writeEndObject();
    }
}
