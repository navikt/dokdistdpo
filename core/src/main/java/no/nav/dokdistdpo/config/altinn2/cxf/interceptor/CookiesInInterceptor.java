package no.nav.dokdistdpo.config.altinn2.cxf.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class CookiesInInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");

	public CookiesInInterceptor() {
		super(Phase.PRE_PROTOCOL);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
		List<String> cookies = headers.get("Set-Cookie");
		if (cookies != null) {
			secureLog.info("CookiesInInterceptor -- cookie to be stored in cookiestore:{}", cookies.getFirst());
			CookieStore.setRequestCookie(cookies.getFirst());
		}
	}
}
