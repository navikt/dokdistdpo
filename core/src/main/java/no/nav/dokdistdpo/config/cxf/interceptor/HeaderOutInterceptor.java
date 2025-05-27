package no.nav.dokdistdpo.config.cxf.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

import java.util.List;
import java.util.Map;

import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.apache.cxf.phase.Phase.PRE_PROTOCOL_ENDING;

@Slf4j
public class HeaderOutInterceptor extends AbstractPhaseInterceptor<Message> {

	public HeaderOutInterceptor() {
		super(PRE_PROTOCOL_ENDING);
		getAfter().add(SAAJOutInterceptor.SAAJOutEndingInterceptor.class.getName());
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		log.info("Adding Keep-Alive header");
		Map<String, List<String>> headers = (Map<String, List<String>>) message.get(PROTOCOL_HEADERS);
		headers.put("Connection", List.of("Keep-Alive"));
	}
}
