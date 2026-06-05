package scrobblefilter.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import scrobblefilter.net.bluesky.BlueskyOAuthClient;

/**
 * Serves the AT Protocol OAuth client-metadata document. For a public client the
 * {@code client_id} must be the HTTPS URL this document is served from, and the
 * redirect URI must share its origin — both are derived from the request so the
 * document is correct regardless of deploy host. Served at
 * {@code /hello/client-metadata.json} (the DispatcherServlet owns {@code /hello/*}).
 */
@Controller
public class BlueskyMetadataController {

	private final ObjectMapper mapper = new ObjectMapper();

	@RequestMapping(value = "client-metadata.json", method = GET)
	public void clientMetadata(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String base = baseUrl(req);
		Map<String, Object> doc = new LinkedHashMap<>();
		doc.put("client_id", base + "/hello/client-metadata.json");
		doc.put("client_name", "ScrobbleFilter");
		doc.put("client_uri", base);
		doc.put("redirect_uris", Arrays.asList(base + "/hello/bluesky/callback"));
		doc.put("grant_types", Arrays.asList("authorization_code", "refresh_token"));
		doc.put("response_types", Arrays.asList("code"));
		doc.put("scope", BlueskyOAuthClient.SCOPE);
		doc.put("token_endpoint_auth_method", "none");
		doc.put("application_type", "web");
		doc.put("dpop_bound_access_tokens", true);

		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
		mapper.writeValue(res.getWriter(), doc);
	}

	/** Scheme+authority of this deployment, honouring X-Forwarded-Proto (Cloud Run terminates TLS). */
	private static String baseUrl(HttpServletRequest req) {
		String forwardedProto = req.getHeader("X-Forwarded-Proto");
		String scheme = (forwardedProto != null && !forwardedProto.isEmpty()) ? forwardedProto : req.getScheme();
		String authority = URI.create(req.getRequestURL().toString()).getAuthority();
		return scheme + "://" + authority;
	}
}
