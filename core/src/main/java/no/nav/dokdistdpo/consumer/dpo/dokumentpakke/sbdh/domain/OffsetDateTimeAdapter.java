package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh.domain;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.OffsetDateTime;

public class OffsetDateTimeAdapter extends XmlAdapter<String, OffsetDateTime> {

	@Override
	public OffsetDateTime unmarshal(String v) {
		return OffsetDateTime.parse(v);
	}

	@Override
	public String marshal(OffsetDateTime v) {
		return v.toString();
	}
}
