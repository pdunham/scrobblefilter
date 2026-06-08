package scrobblefilter.net.bluesky;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.SignedJWT;

import org.junit.Test;

import scrobblefilter.model.User;
import scrobblefilter.net.SocialPostException;
import scrobblefilter.util.CredentialCrypto;
import scrobblefilter.util.CredentialCryptoProvider;

public class BlueskyPosterTest {

	// --- fakes -------------------------------------------------------------

	private static class FakeHttp implements HttpGetter {
		final Map<String, String> responses = new HashMap<>();
		FakeHttp put(String url, String body) { responses.put(url, body); return this; }
		@Override public String get(String url) throws IOException {
			String b = responses.get(url);
			if (b == null) throw new IOException("unexpected GET " + url);
			return b;
		}
	}

	private static class ScriptedForm implements FormPoster {
		final Deque<HttpExchange> responses = new ArrayDeque<>();
		ScriptedForm queue(HttpExchange r) { responses.add(r); return this; }
		@Override public HttpExchange post(String url, Map<String, String> h, Map<String, String> f) {
			return responses.poll();
		}
	}

	private static class ScriptedJson implements JsonPoster {
		final Deque<HttpExchange> responses = new ArrayDeque<>();
		final List<String> urls = new ArrayList<>();
		final List<Map<String, String>> headers = new ArrayList<>();
		final List<String> bodies = new ArrayList<>();
		ScriptedJson queue(HttpExchange r) { responses.add(r); return this; }
		@Override public HttpExchange post(String url, Map<String, String> h, String body) {
			urls.add(url); headers.add(h); bodies.add(body);
			return responses.poll();
		}
	}

	private static class CapturingPoster extends BlueskyPoster {
		User saved;
		CapturingPoster(BlueskyResolver r, FormPoster fp, JsonPoster jp, DpopProofFactory pf, CredentialCryptoProvider c) {
			super(r, fp, jp, pf, c);
		}
		@Override protected void persist(User user) { this.saved = user; }
	}

	// --- fixtures ----------------------------------------------------------

	private final CredentialCrypto crypto = new CredentialCrypto(new byte[32]);
	private final CredentialCryptoProvider cryptoProvider = new CredentialCryptoProvider(crypto);
	private final ECKey dpopKey = DpopKeys.generate();

	private BlueskyResolver resolver() {
		FakeHttp http = new FakeHttp()
			.put("https://r/xrpc/com.atproto.identity.resolveHandle?handle=alice.test",
					"{\"did\":\"did:plc:mock\"}")
			.put("https://plc/did:plc:mock",
					"{\"service\":[{\"type\":\"AtprotoPersonalDataServer\",\"serviceEndpoint\":\"https://pds.mock\"}]}")
			.put("https://pds.mock/.well-known/oauth-protected-resource",
					"{\"authorization_servers\":[\"https://as.mock\"]}")
			.put("https://as.mock/.well-known/oauth-authorization-server",
					"{\"issuer\":\"https://as.mock\","
					+ "\"pushed_authorization_request_endpoint\":\"https://as.mock/par\","
					+ "\"authorization_endpoint\":\"https://as.mock/authorize\","
					+ "\"token_endpoint\":\"https://as.mock/token\"}");
		return new BlueskyResolver(http, "https://r", "https://plc");
	}

	private User connectedUser() {
		User u = new User();
		u.setLastfmName("alice");
		u.setBlueskyHandle("alice.test");
		u.setBlueskyDid("did:plc:mock");
		u.setBlueskyCron(true);
		u.setBlueskyDpopKeyEnc(crypto.encrypt(DpopKeys.toJson(dpopKey)));
		u.setBlueskyRefreshTokenEnc(crypto.encrypt("refresh-1"));
		return u;
	}

	private static HttpExchange ok(String body) {
		return new HttpExchange(200, body, Collections.emptyMap());
	}

	private static String tokenJson(String refresh) {
		return "{\"access_token\":\"at-1\",\"token_type\":\"DPoP\",\"refresh_token\":\"" + refresh
				+ "\",\"sub\":\"did:plc:mock\",\"scope\":\"atproto transition:generic\",\"expires_in\":3600}";
	}

	// --- tests -------------------------------------------------------------

	@Test
	public void postRefreshesCreatesRecordAndRotatesRefreshToken() throws Exception {
		ScriptedForm form = new ScriptedForm().queue(ok(tokenJson("refresh-2")));
		ScriptedJson json = new ScriptedJson().queue(ok("{\"uri\":\"at://did:plc:mock/app.bsky.feed.post/abc\"}"));
		CapturingPoster poster = new CapturingPoster(resolver(), form, json, new DpopProofFactory(), cryptoProvider);

		User user = connectedUser();
		poster.post(user, "I've been listening to A, B, and C.");

		// createRecord hit the PDS with the access token and a well-formed record.
		assertEquals("https://pds.mock/xrpc/com.atproto.repo.createRecord", json.urls.get(0));
		assertEquals("DPoP at-1", json.headers.get(0).get("Authorization"));
		assertNotNull(json.headers.get(0).get("DPoP"));
		String body = json.bodies.get(0);
		assertTrue(body.contains("\"repo\":\"did:plc:mock\""));
		assertTrue(body.contains("\"collection\":\"app.bsky.feed.post\""));
		assertTrue(body.contains("\"$type\":\"app.bsky.feed.post\""));
		assertTrue(body.contains("I've been listening to A, B, and C."));

		// Rotated refresh token persisted (re-encrypted).
		assertNotNull("rotated refresh token should be persisted", poster.saved);
		assertEquals("refresh-2", crypto.decrypt(user.getBlueskyRefreshTokenEnc()));

		// The createRecord DPoP proof binds the access token via ath.
		String ath = SignedJWT.parse(json.headers.get(0).get("DPoP")).getJWTClaimsSet().getStringClaim("ath");
		assertEquals(DpopProofFactory.sha256Base64Url("at-1"), ath);
	}

	@Test
	public void createRecordRetriesOnceWithNonce() throws Exception {
		ScriptedForm form = new ScriptedForm().queue(ok(tokenJson("refresh-1"))); // no rotation
		ScriptedJson json = new ScriptedJson()
				.queue(new HttpExchange(400, "{\"error\":\"use_dpop_nonce\"}", Map.of("dpop-nonce", "pds-nonce-1")))
				.queue(ok("{\"uri\":\"at://ok\"}"));
		CapturingPoster poster = new CapturingPoster(resolver(), form, json, new DpopProofFactory(), cryptoProvider);

		poster.post(connectedUser(), "hello");

		assertEquals("createRecord should retry once", 2, json.urls.size());
		assertNull(SignedJWT.parse(json.headers.get(0).get("DPoP")).getJWTClaimsSet().getStringClaim("nonce"));
		assertEquals("pds-nonce-1",
				SignedJWT.parse(json.headers.get(1).get("DPoP")).getJWTClaimsSet().getStringClaim("nonce"));
	}

	@Test
	public void createRecordErrorWithoutNonceThrows() {
		ScriptedForm form = new ScriptedForm().queue(ok(tokenJson("refresh-1")));
		ScriptedJson json = new ScriptedJson().queue(new HttpExchange(400, "{\"error\":\"InvalidRequest\"}", Collections.emptyMap()));
		BlueskyPoster poster = new CapturingPoster(resolver(), form, json, new DpopProofFactory(), cryptoProvider);
		try {
			poster.post(connectedUser(), "hello");
			fail("expected SocialPostException");
		} catch (SocialPostException expected) { /* ok */ }
	}

	@Test
	public void enablementReflectsConnectionAndOptIn() {
		BlueskyPoster poster = new BlueskyPoster(resolver(), new ScriptedForm(), new ScriptedJson(),
				new DpopProofFactory(), cryptoProvider);
		assertEquals("bluesky", poster.platform());

		User connected = connectedUser();
		assertTrue(poster.isConnected(connected));
		assertTrue(poster.isEnabledFor(connected));

		connected.setBlueskyCron(false);
		assertTrue(poster.isConnected(connected));
		assertFalse("opt-out disables posting", poster.isEnabledFor(connected));

		User noCreds = new User();
		noCreds.setBlueskyHandle("x.test");
		assertFalse(poster.isConnected(noCreds));
		assertFalse(poster.isEnabledFor(noCreds));
	}
}
