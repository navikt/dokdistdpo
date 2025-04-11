package no.nav.dokdistdpo.constant;

import java.time.ZoneId;
import java.util.TimeZone;

public final class DokdistdpoConstant {
	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Europe/Oslo");
	public static final ZoneId DEFAULT_ZONE_ID = DEFAULT_TIME_ZONE.toZoneId();

	public static final String NAV_ORGNUMMER = "991078045";
	public static final String AVTALTMELDING_PROCESS = "urn:no:difi:profile:avtalt:avtalt:ver1.0";

	public static final String DPO_ARKIVMELDING = "DPO_ARKIVMELDING";
	public static final String DPO_AVTALEMELDING = "DPO_AVTALEMELDING";

	private DokdistdpoConstant() {
	}
}
