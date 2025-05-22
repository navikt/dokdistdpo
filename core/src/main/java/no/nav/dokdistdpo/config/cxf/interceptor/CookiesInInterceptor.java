package no.nav.dokdistdpo.config.cxf.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class CookiesInInterceptor extends AbstractPhaseInterceptor {

	public CookiesInInterceptor() {
		super(Phase.PRE_PROTOCOL);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
		List<String> cookies = headers.get("Set-Cookie");
		if (cookies != null) {
			CookieStore.setRequestCookie(cookies.getFirst());
		}
	}
}
