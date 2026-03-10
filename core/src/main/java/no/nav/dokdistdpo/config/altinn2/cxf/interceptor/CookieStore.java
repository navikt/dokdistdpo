package no.nav.dokdistdpo.config.altinn2.cxf.interceptor;

public class CookieStore {

	private static final ThreadLocal<Object> requestCookie = new ThreadLocal<>();

	public CookieStore() {
		throw new AssertionError("Instantiating cookie class.");
	}

	public static void setRequestCookie(Object cookie) {
		requestCookie.set(cookie);
	}

	public static Object getCookie() {
		return requestCookie.get();
	}
}
