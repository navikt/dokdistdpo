package no.nav.dokdistdpo.azure;

import no.nav.dokdistdpo.consumer.dpo.maskinporten.MaskinportenConsumer;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.UUID;

import static no.nav.dokdistdpo.constant.MDCConstant.CALL_ID;
import static no.nav.dokdistdpo.constant.NavHeaders.NAV_CALLID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MaskinportenRequestInterceptor implements ClientHttpRequestInterceptor {

	private final MaskinportenConsumer maskinportenConsumer;

	public MaskinportenRequestInterceptor(MaskinportenConsumer maskinportenConsumer) {
		this.maskinportenConsumer = maskinportenConsumer;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		request.getHeaders().setBearerAuth(maskinportenConsumer.getMaskinportenToken());
		request.getHeaders().set(NAV_CALLID, getCallId());
		return execution.execute(request, body);
	}

	private static String getCallId() {
		String callId = MDC.get(CALL_ID);
		return isNotBlank(callId) ? callId : UUID.randomUUID().toString();
	}
}
