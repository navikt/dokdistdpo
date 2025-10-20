package no.nav.dokdistdpo.constant;

import java.time.ZoneId;
import java.util.TimeZone;
import java.util.UUID;

public final class DokdistdpoConstant {

	public static final String NAV_ORGNUMMER = "889640782";

	public static final String FORSENDELSE_STATUS_KLAR_FOR_DIST = "KLAR_FOR_DIST";
	public static final String FORSENDELSE_STATUS_OVERSENDT = "OVERSENDT";

	public static final String PROPERTY_KONVERSASJON_ID = "konversasjonId";
	public static final String PROPERTY_BESTILLINGS_ID = "bestillingsId";
	public static final String PROPERTY_FORSENDELSE_ID = "forsendelseId";

	public static final String AVTALTMELDING_XML = "avtaltmelding.xml";
	public static final String AVTALTMELDING_DOCUMENT_IDENTIFICATOR = "urn:no:difi:avtalt:xsd::avtalt";
	public static final String AVTALTMELDING_PROCESS_IDENTIFIER = "urn:no:difi:profile:avtalt:avtalt:ver1.0";

	public static final UUID MESSAGE_CHANNEL_INSTANCE_IDENTIFIER = UUID.randomUUID();

	public static final String ARKIVMELDING_XML = "arkivmelding.xml";

	public static final String ARKIVMELDING_DOCUMENT_IDENTIFICATOR = "urn:no:difi:arkivmelding:xsd::arkivmelding";
	public static final String ARKIVMELDING_PROCESS_IDENTIFIER = "urn:no:difi:profile:arkivmelding:administrasjon:ver1.0";

	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Europe/Oslo");
	public static final ZoneId DEFAULT_ZONE_ID = DEFAULT_TIME_ZONE.toZoneId();

	public static final String MANIFEST_XML = "manifest.xml";
	public static final String SBD_JSON = "sbd.json";

	private DokdistdpoConstant() {
	}
}
