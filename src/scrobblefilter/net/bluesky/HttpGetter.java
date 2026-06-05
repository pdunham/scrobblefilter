package scrobblefilter.net.bluesky;

import java.io.IOException;

/**
 * Minimal HTTP GET seam so the AT Protocol resolution/OAuth code can be unit
 * tested against canned JSON without real network calls.
 */
public interface HttpGetter {

	/** GET the URL and return the response body; throws on transport error or non-2xx. */
	String get(String url) throws IOException;
}
