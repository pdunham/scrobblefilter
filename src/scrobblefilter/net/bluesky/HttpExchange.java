package scrobblefilter.net.bluesky;

import java.util.Locale;
import java.util.Map;

/**
 * A completed HTTP response: status, body, and (case-insensitive) headers.
 * Returned for any status — including 4xx — so callers can read the
 * {@code DPoP-Nonce} header off a nonce challenge instead of seeing an exception.
 */
public class HttpExchange {

	private final int status;
	private final String body;
	private final Map<String, String> headers; // keys lower-cased

	public HttpExchange(int status, String body, Map<String, String> headers) {
		this.status = status;
		this.body = body;
		this.headers = headers;
	}

	public int status() {
		return status;
	}

	public String body() {
		return body;
	}

	public boolean isSuccess() {
		return status >= 200 && status < 300;
	}

	/** Case-insensitive header lookup; null if absent. */
	public String header(String name) {
		return headers.get(name.toLowerCase(Locale.ROOT));
	}
}
