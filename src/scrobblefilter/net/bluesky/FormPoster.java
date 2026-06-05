package scrobblefilter.net.bluesky;

import java.io.IOException;
import java.util.Map;

/**
 * POST seam for {@code application/x-www-form-urlencoded} requests, returning the
 * full {@link HttpExchange} (no throw on non-2xx). Lets the OAuth client be unit
 * tested against canned responses and read the DPoP nonce off a 4xx.
 */
public interface FormPoster {

	HttpExchange post(String url, Map<String, String> headers, Map<String, String> formParams) throws IOException;
}
