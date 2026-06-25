package scrobblefilter.net.bluesky;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.nimbusds.jose.jwk.ECKey;

/**
 * AT Protocol OAuth client for a public client (client_id = the hosted
 * client-metadata URL, no client secret). Performs the PAR → authorize → token
 * flow and refresh, attaching a DPoP proof to every request and retrying once
 * with the server-issued nonce on a {@code use_dpop_nonce} challenge.
 *
 * Scope is the standard {@code atproto transition:generic}. HTTP is behind
 * {@link FormPoster} so the flow is unit-testable against canned responses.
 */
public class BlueskyOAuthClient {

	public static final String SCOPE = "atproto transition:generic";

	private final FormPoster poster;
	private final DpopProofFactory proofFactory;
	private final String clientId;
	private final String redirectUri;
	private final ObjectMapper mapper = new ObjectMapper();

	public BlueskyOAuthClient(FormPoster poster, DpopProofFactory proofFactory,
			String clientId, String redirectUri) {
		this.poster = poster;
		this.proofFactory = proofFactory;
		this.clientId = clientId;
		this.redirectUri = redirectUri;
	}

	/**
	 * Push an authorization request (RFC 9126) and return its {@code request_uri}.
	 * The caller then redirects the user to
	 * {@code authorization_endpoint?client_id=...&request_uri=...}.
	 */
	public String pushAuthorizationRequest(AuthServerMetadata as, ECKey dpopKey, Pkce pkce,
			String state, String loginHintHandle) throws IOException {
		Map<String, String> form = new LinkedHashMap<>();
		form.put("response_type", "code");
		form.put("client_id", clientId);
		form.put("redirect_uri", redirectUri);
		form.put("scope", SCOPE);
		form.put("state", state);
		form.put("code_challenge", pkce.challenge());
		form.put("code_challenge_method", pkce.method());
		if (loginHintHandle != null && !loginHintHandle.isEmpty()) {
			form.put("login_hint", loginHintHandle);
		}

		HttpExchange resp = postWithDpop(as.getPushedAuthorizationRequestEndpoint(), dpopKey, form);
		if (!resp.isSuccess()) {
			throw new IOException("PAR failed (" + resp.status() + "): " + resp.body());
		}
		String requestUri = text(parse(resp.body()), "request_uri");
		if (requestUri == null) {
			throw new IOException("PAR response missing request_uri: " + resp.body());
		}
		return requestUri;
	}

	/** Exchange an authorization code for DPoP-bound access + refresh tokens. */
	public TokenSet exchangeCode(AuthServerMetadata as, ECKey dpopKey, String code, String codeVerifier)
			throws IOException {
		Map<String, String> form = new LinkedHashMap<>();
		form.put("grant_type", "authorization_code");
		form.put("code", code);
		form.put("redirect_uri", redirectUri);
		form.put("client_id", clientId);
		form.put("code_verifier", codeVerifier);
		return token(as, dpopKey, form);
	}

	/** Use a refresh token to obtain a fresh access token (refresh may rotate). */
	public TokenSet refresh(AuthServerMetadata as, ECKey dpopKey, String refreshToken) throws IOException {
		Map<String, String> form = new LinkedHashMap<>();
		form.put("grant_type", "refresh_token");
		form.put("refresh_token", refreshToken);
		form.put("client_id", clientId);
		return token(as, dpopKey, form);
	}

	private TokenSet token(AuthServerMetadata as, ECKey dpopKey, Map<String, String> form) throws IOException {
		HttpExchange resp = postWithDpop(as.getTokenEndpoint(), dpopKey, form);
		if (!resp.isSuccess()) {
			throw new BlueskyAuthException(resp.status(), errorCode(resp.body()),
					"token request failed (" + resp.status() + "): " + resp.body());
		}
		JsonNode body = parse(resp.body());
		String accessToken = text(body, "access_token");
		if (accessToken == null) {
			throw new IOException("token response missing access_token: " + resp.body());
		}
		long expiresIn = body.has("expires_in") ? body.get("expires_in").asLong(0L) : 0L;
		return new TokenSet(accessToken, text(body, "refresh_token"), text(body, "sub"),
				text(body, "scope"), expiresIn);
	}

	/**
	 * POST with a DPoP proof, retrying once with the server nonce when the AS
	 * answers a 4xx carrying a {@code DPoP-Nonce} header (the {@code use_dpop_nonce}
	 * challenge). Authorization-server requests bind no access token, so {@code ath}
	 * is omitted.
	 */
	private HttpExchange postWithDpop(String url, ECKey dpopKey, Map<String, String> form) throws IOException {
		String proof = proofFactory.createProof(dpopKey, "POST", url, null, null);
		HttpExchange resp = poster.post(url, dpopHeader(proof), form);

		String nonce = resp.header("dpop-nonce");
		if (!resp.isSuccess() && resp.status() >= 400 && resp.status() < 500 && nonce != null) {
			String retryProof = proofFactory.createProof(dpopKey, "POST", url, nonce, null);
			resp = poster.post(url, dpopHeader(retryProof), form);
		}
		return resp;
	}

	private static Map<String, String> dpopHeader(String proof) {
		Map<String, String> h = new LinkedHashMap<>();
		h.put("DPoP", proof);
		return h;
	}

	private JsonNode parse(String json) throws IOException {
		return mapper.readValue(json, JsonNode.class);
	}

	/** Best-effort extraction of the OAuth {@code error} code from a response body. */
	private String errorCode(String body) {
		try {
			return text(parse(body), "error");
		} catch (IOException e) {
			return null;
		}
	}

	private static String text(JsonNode node, String field) {
		JsonNode v = node.get(field);
		if (v == null || v.isNull()) return null;
		String s = v.asText();
		return (s == null || s.isEmpty()) ? null : s;
	}
}
