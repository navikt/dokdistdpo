package no.nav.dokdistdpo.qdist015.utils;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.language.xpath.XPathBuilder;
import org.slf4j.MDC;

import java.util.UUID;

import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_FORSENDELSE_ID;
import static no.nav.dokdistdpo.constant.MDCConstant.CALL_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MdcHeaderProcessor implements Processor {
	@Override
	public void process(Exchange exchange) {
		setOrGenerateCallIdToMdc(exchange);
		setForsendelseIdAsProperty(exchange);
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

	private static void setForsendelseIdAsProperty(Exchange exchange) {
		String forsendelseId = XPathBuilder.xpath("//forsendelseId/text()").evaluate(exchange, String.class);

		if (isNotBlank(forsendelseId)) {
			exchange.setProperty(PROPERTY_FORSENDELSE_ID, forsendelseId);
		}
	}
}
