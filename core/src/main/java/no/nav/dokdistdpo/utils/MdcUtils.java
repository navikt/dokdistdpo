package no.nav.dokdistdpo.utils;

import org.slf4j.MDC;

import java.util.UUID;

import static no.nav.dokdistdpo.constant.MDCConstant.CALL_ID;

public final class MdcUtils {

	private MdcUtils() {
	}

	public static void generateNewCallId() {
		MDC.put(CALL_ID, UUID.randomUUID().toString());
	}

	public static void clearMDC() {
		MDC.clear();
	}
}
