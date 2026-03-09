package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.Arkivmelding;
import no.nav.dokdistdpo.consumer.dpo.dokumentpakke.avtaltmelding.AvtaltMelding;

import java.io.IOException;

public class StandardBusinessDocumentSerializer extends StdSerializer<StandardBusinessDocument> {

    protected StandardBusinessDocumentSerializer() {
        super(StandardBusinessDocument.class);
    }

    @Override
    public void serialize(StandardBusinessDocument value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("standardBusinessDocumentHeader");
        gen.writeObject(value.getStandardBusinessDocumentHeader());
        if (value.getAny() instanceof AvtaltMelding) {
            gen.writeFieldName("avtalt");
        } else if (value.getAny() instanceof Arkivmelding) {
            gen.writeFieldName("arkivmelding");
        }
        else {
            throw new UnsupportedOperationException("Kun avtaltmelding og arkivmelding er støttet.");
        }
        gen.writeObject(value.getAny());
        gen.writeEndObject();
    }
}
