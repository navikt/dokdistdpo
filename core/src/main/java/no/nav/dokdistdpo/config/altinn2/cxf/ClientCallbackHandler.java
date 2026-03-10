package no.nav.dokdistdpo.config.altinn2.cxf;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

public class ClientCallbackHandler implements CallbackHandler {

	private final String password;

	public ClientCallbackHandler(String password) {
		this.password = password;
	}

	@Override
	public void handle(Callback[] callbacks) {
		WSPasswordCallback wsPasswordCallback = (WSPasswordCallback) callbacks[0];
		wsPasswordCallback.setPassword(password);
	}
}
