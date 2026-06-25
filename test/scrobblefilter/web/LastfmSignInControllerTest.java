package scrobblefilter.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class LastfmSignInControllerTest {

	@Test
	public void apiSigMatchesKnownMd5Vector() {
		// MD5("api_key" + "abc" + "method" + "auth.getSession" + "token" + "xyz" + "testsecret")
		Map<String, String> params = new LinkedHashMap<>();
		params.put("api_key", "abc");
		params.put("method", "auth.getSession");
		params.put("token", "xyz");

		assertEquals("a7f6c73d8151180d406cb5d590cd2e50",
				LastfmSignInController.apiSig(params, "testsecret"));
	}

	@Test
	public void apiSigIsIndependentOfInsertionOrder() {
		// Last.fm requires the params be sorted by key before signing, so the
		// signature must not depend on how the map was populated.
		Map<String, String> a = new LinkedHashMap<>();
		a.put("token", "xyz");
		a.put("api_key", "abc");
		a.put("method", "auth.getSession");

		Map<String, String> b = new LinkedHashMap<>();
		b.put("api_key", "abc");
		b.put("method", "auth.getSession");
		b.put("token", "xyz");

		assertEquals(LastfmSignInController.apiSig(a, "s"),
				LastfmSignInController.apiSig(b, "s"));
	}

	@Test
	public void extractSessionNameReturnsName() throws IOException {
		String body = "{\"session\":{\"name\":\"alice\",\"key\":\"deadbeef\",\"subscriber\":0}}";
		assertEquals("alice", LastfmSignInController.extractSessionName(body));
	}

	@Test
	public void extractSessionNameThrowsWhenSessionMissing() {
		// A Last.fm error response (e.g. invalid token) has no "session" object.
		String body = "{\"error\":4,\"message\":\"Invalid authentication token\"}";
		IOException e = assertThrows(IOException.class,
				() -> LastfmSignInController.extractSessionName(body));
		// The session key must never leak into the message — only the body would
		// carry it, so the message must not echo the response.
		org.junit.Assert.assertEquals("auth.getSession returned no session", e.getMessage());
	}

	@Test
	public void extractSessionNameThrowsWhenNameMissing() {
		String body = "{\"session\":{\"key\":\"deadbeef\",\"subscriber\":0}}";
		IOException e = assertThrows(IOException.class,
				() -> LastfmSignInController.extractSessionName(body));
		assertEquals("auth.getSession returned no session name", e.getMessage());
	}
}
