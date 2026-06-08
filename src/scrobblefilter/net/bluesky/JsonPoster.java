package scrobblefilter.net.bluesky;

import java.io.IOException;
import java.util.Map;

/**
 * POST seam for {@code application/json} requests (e.g. PDS resource calls like
 * {@code com.atproto.repo.createRecord}), returning the full {@link HttpExchange}
 * so the caller can read a DPoP nonce off a 4xx. Distinct from {@link FormPoster}
 * (the OAuth endpoints use form encoding; resource records use JSON).
 */
public interface JsonPoster {

	HttpExchange post(String url, Map<String, String> headers, String jsonBody) throws IOException;
}
