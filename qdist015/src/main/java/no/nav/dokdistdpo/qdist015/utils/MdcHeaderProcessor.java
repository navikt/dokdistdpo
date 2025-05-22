package no.nav.dokdistdpo.qdist015.utils;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.MDC;

import java.util.UUID;

import static no.nav.dokdistdpo.utils.MdcConstant.CALL_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MdcHeaderProcessor implements Processor {
	@Override
	public void process(Exchange exchange) throws Exception {
		setOrGenerateCallIdToMdc(exchange);
	}

	public static void setOrGenerateCallIdToMdc(Exchange exchange) {
		final String callId = exchange.getIn().getHeader(CALL_ID, String.class);
		if (isBlank(callId)) {
			String newCallId = UUID.randomUUID().toString();
			exchange.getIn().setHeader(CALL_ID, newCallId);
			MDC.put(CALL_ID, newCallId);
		} else {
			MDC.put(CALL_ID, callId);
		}
	}
}
