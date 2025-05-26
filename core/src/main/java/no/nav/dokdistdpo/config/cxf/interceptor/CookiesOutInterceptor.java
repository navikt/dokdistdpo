package no.nav.dokdistdpo.config.cxf.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

import java.util.List;
import java.util.Map;

import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.apache.cxf.phase.Phase.PRE_PROTOCOL;

@SuppressWarnings("unchecked")
public class CookiesOutInterceptor extends AbstractPhaseInterceptor {

	public CookiesOutInterceptor() {
		super(PRE_PROTOCOL);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		Map<String, List<Object>> headers = (Map<String, List<Object>>) message.get(PROTOCOL_HEADERS);
		if (CookieStore.getCookie() != null) {
			headers.put("Cookie", List.of(CookieStore.getCookie()));
		}
	}
}
