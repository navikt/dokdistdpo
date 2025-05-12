package no.nav.dokdistdpo.constant;

import java.time.ZoneId;
import java.util.TimeZone;
import java.util.UUID;

public final class DokdistdpoConstant {
	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Europe/Oslo");
	public static final ZoneId DEFAULT_ZONE_ID = DEFAULT_TIME_ZONE.toZoneId();

	public static final String NAV_ORGNUMMER = "889640782";

	public static final String AVTALTMELDING_XML = "avtaltmelding.xml";
	public static final String AVTALTMELDING_DOCUMENT_IDENTIFICATOR = "urn:no:difi:avtalt:xsd::avtalt";
	public static final String SCOPE_CONVERSATION_ID_AVTALT_PROCESS_IDENTIFIER = "urn:no:difi:profile:avtalt:avtalt:ver1.0";

	public static final UUID MESSAGE_CHANNEL_INSTANCE_IDENTIFIER = UUID.randomUUID();

	public static final String ARKIVMELDING_XML = "avtaltmelding.xml";
	public static final String ARKIVMELDING_DOCUMENT_IDENTIFICATOR = "urn:no:difi:arkivmelding:xsd::arkivmelding";
	public static final String SCOPE_CONVERSATION_ID_ARKIVMELDING_PROCESS_IDENTIFIER = "urn:no:difi:profile:arkivmelding:administrasjon:ver1.0";

	public static final String VARIANTFORMAT_SLADDET = "SLADDET";
	public static final String VARIANTFORMAT_ARKIV = "ARKIV";
	public static final String VARIANTFORMAT_PRODUKSJON = "PRODUKSJON";

	private DokdistdpoConstant() {
	}
}
