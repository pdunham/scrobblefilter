package scrobblefilter.net.bluesky;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.SignedJWT;

import org.junit.Test;

public class BlueskyOAuthClientTest {

	/** Records each call and returns scripted responses in order. */
	private static class ScriptedPoster implements FormPoster {
		final Deque<HttpExchange> responses = new ArrayDeque<>();
		final List<Map<String, String>> sentHeaders = new ArrayList<>();
		final List<Map<String, String>> sentForms = new ArrayList<>();
		final List<String> urls = new ArrayList<>();

		ScriptedPoster queue(HttpExchange r) { responses.add(r); return this; }

		@Override
		public HttpExchange post(String url, Map<String, String> headers, Map<String, String> form) {
			urls.add(url);
			sentHeaders.add(headers);
			sentForms.add(form);
			if (responses.isEmpty()) throw new IllegalStateException("no scripted response for " + url);
			return responses.poll();
		}
	}

	private static HttpExchange resp(int status, String body, Map<String, String> headers) {
		return new HttpExchange(status, body, headers);
	}

	private static final AuthServerMetadata AS = new AuthServerMetadata(
			"https://as.example", "https://as.example/par",
			"https://as.example/authorize", "https://as.example/token");

	private final ECKey key = DpopKeys.generate();

	private BlueskyOAuthClient client(ScriptedPoster poster) {
		return new BlueskyOAuthClient(poster, new DpopProofFactory(),
				"https://app.example/client-metadata.json", "https://app.example/hello/bluesky/callback");
	}

	@Test
	public void parReturnsRequestUriAndSendsExpectedParams() throws IOException {
		ScriptedPoster poster = new ScriptedPoster().queue(
				resp(201, "{\"request_uri\":\"urn:ietf:params:oauth:request_uri:abc\",\"expires_in\":60}",
						Collections.emptyMap()));

		String requestUri = client(poster).pushAuthorizationRequest(
				AS, key, Pkce.generate(), "state-xyz", "alice.test");

		assertEquals("urn:ietf:params:oauth:request_uri:abc", requestUri);
		assertEquals("https://as.example/par", poster.urls.get(0));
		Map<String, String> form = poster.sentForms.get(0);
		assertEquals("code", form.get("response_type"));
		assertEquals("https://app.example/client-metadata.json", form.get("client_id"));
		assertEquals("https://app.example/hello/bluesky/callback", form.get("redirect_uri"));
		assertEquals("atproto transition:generic", form.get("scope"));
		assertEquals("S256", form.get("code_challenge_method"));
		assertEquals("state-xyz", form.get("state"));
		assertEquals("alice.test", form.get("login_hint"));
		// A DPoP proof header is attached.
		org.junit.Assert.assertNotNull(poster.sentHeaders.get(0).get("DPoP"));
	}

	@Test
	public void retriesOnceWithServerNonce() throws Exception {
		ScriptedPoster poster = new ScriptedPoster()
				.queue(resp(400, "{\"error\":\"use_dpop_nonce\"}",
						Map.of("dpop-nonce", "server-nonce-1")))
				.queue(resp(201, "{\"request_uri\":\"urn:req:ok\"}", Collections.emptyMap()));

		String requestUri = client(poster).pushAuthorizationRequest(AS, key, Pkce.generate(), "s", null);

		assertEquals("urn:req:ok", requestUri);
		assertEquals("client should retry exactly once", 2, poster.urls.size());

		// First proof carries no nonce; the retry proof carries the server nonce.
		assertNull(nonceClaim(poster.sentHeaders.get(0).get("DPoP")));
		assertEquals("server-nonce-1", nonceClaim(poster.sentHeaders.get(1).get("DPoP")));
	}

	@Test
	public void exchangeCodeParsesTokenSet() throws IOException {
		ScriptedPoster poster = new ScriptedPoster().queue(resp(200,
				"{\"access_token\":\"at-1\",\"token_type\":\"DPoP\",\"refresh_token\":\"rt-1\","
				+ "\"sub\":\"did:plc:abc\",\"scope\":\"atproto transition:generic\",\"expires_in\":3600}",
				Collections.emptyMap()));

		TokenSet tokens = client(poster).exchangeCode(AS, key, "auth-code", "verifier-123");

		assertEquals("at-1", tokens.getAccessToken());
		assertEquals("rt-1", tokens.getRefreshToken());
		assertEquals("did:plc:abc", tokens.getDid());
		assertEquals(3600L, tokens.getExpiresInSeconds());
		assertEquals("https://as.example/token", poster.urls.get(0));
		Map<String, String> form = poster.sentForms.get(0);
		assertEquals("authorization_code", form.get("grant_type"));
		assertEquals("auth-code", form.get("code"));
		assertEquals("verifier-123", form.get("code_verifier"));
	}

	@Test
	public void refreshSendsRefreshGrant() throws IOException {
		ScriptedPoster poster = new ScriptedPoster().queue(resp(200,
				"{\"access_token\":\"at-2\",\"refresh_token\":\"rt-2\",\"sub\":\"did:plc:abc\"}",
				Collections.emptyMap()));

		TokenSet tokens = client(poster).refresh(AS, key, "old-refresh");

		assertEquals("at-2", tokens.getAccessToken());
		assertEquals("rt-2", tokens.getRefreshToken());
		Map<String, String> form = poster.sentForms.get(0);
		assertEquals("refresh_token", form.get("grant_type"));
		assertEquals("old-refresh", form.get("refresh_token"));
	}

	@Test
	public void tokenErrorThrowsTypedAuthExceptionCarryingErrorCode() throws IOException {
		ScriptedPoster poster = new ScriptedPoster().queue(
				resp(400, "{\"error\":\"invalid_grant\",\"error_description\":\"Session expired\"}",
						Collections.emptyMap()));
		try {
			client(poster).refresh(AS, key, "dead-refresh");
			fail("expected BlueskyAuthException on token error");
		} catch (BlueskyAuthException e) {
			assertEquals(400, e.status());
			assertEquals("invalid_grant", e.error());
			assertTrue("invalid_grant on 400 means a dead session", e.isInvalidGrant());
		}
	}

	@Test
	public void transientTokenErrorIsNotInvalidGrant() throws IOException {
		ScriptedPoster poster = new ScriptedPoster().queue(
				resp(500, "{\"error\":\"server_error\"}", Collections.emptyMap()));
		try {
			client(poster).refresh(AS, key, "refresh");
			fail("expected BlueskyAuthException on token error");
		} catch (BlueskyAuthException e) {
			assertEquals(500, e.status());
			org.junit.Assert.assertFalse("a 5xx is transient, not a dead session", e.isInvalidGrant());
		}
	}

	private static String nonceClaim(String dpopProof) throws Exception {
		return SignedJWT.parse(dpopProof).getJWTClaimsSet().getStringClaim("nonce");
	}
}
