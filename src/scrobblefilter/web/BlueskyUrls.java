package scrobblefilter.web;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Derives this deployment's absolute OAuth URLs from the incoming request,
 * honouring {@code X-Forwarded-Proto} (Cloud Run terminates TLS). The
 * {@code client_id} must equal the URL the client-metadata document is served
 * from, and the redirect URI must share its origin.
 */
public final class BlueskyUrls {

	private BlueskyUrls() {
	}

	public static String baseUrl(HttpServletRequest req) {
		String forwardedProto = req.getHeader("X-Forwarded-Proto");
		String scheme = (forwardedProto != null && !forwardedProto.isEmpty()) ? forwardedProto : req.getScheme();
		String authority = URI.create(req.getRequestURL().toString()).getAuthority();
		return scheme + "://" + authority;
	}

	public static String clientId(HttpServletRequest req) {
		// In production, BLUESKY_CLIENT_ID pins the client_id to the hosted
		// client-metadata URL so the connect flow and the cron refresh (which reads
		// the same env in BlueskyPoster) present an identical client_id to the
		// authorization server. Falls back to the request-derived URL for local/dev.
		String configured = System.getenv("BLUESKY_CLIENT_ID");
		return (configured != null && !configured.isEmpty()) ? configured : baseUrl(req) + "/hello/client-metadata.json";
	}

	public static String redirectUri(HttpServletRequest req) {
		return baseUrl(req) + "/hello/bluesky/callback";
	}
}
