package scrobblefilter.net.bluesky;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class BlueskyResolverTest {

	/** Canned HttpGetter: exact URL -> response body; unmapped URLs throw (404-like). */
	private static class FakeHttp implements HttpGetter {
		final Map<String, String> responses = new HashMap<>();
		FakeHttp put(String url, String body) { responses.put(url, body); return this; }
		@Override public String get(String url) throws IOException {
			String body = responses.get(url);
			if (body == null) throw new IOException("unexpected GET " + url);
			return body;
		}
	}

	private static final String RESOLVER = "https://mock-resolver";
	private static final String PLC = "https://mock-plc";

	private FakeHttp plcChain() {
		return new FakeHttp()
			.put(RESOLVER + "/xrpc/com.atproto.identity.resolveHandle?handle=alice.test",
					"{\"did\":\"did:plc:abc123\"}")
			.put(PLC + "/did:plc:abc123",
					"{\"service\":[{\"id\":\"#atproto_pds\",\"type\":\"AtprotoPersonalDataServer\","
					+ "\"serviceEndpoint\":\"https://mock-pds\"}]}")
			.put("https://mock-pds/.well-known/oauth-protected-resource",
					"{\"authorization_servers\":[\"https://mock-as\"]}")
			.put("https://mock-as/.well-known/oauth-authorization-server",
					"{\"issuer\":\"https://mock-as\","
					+ "\"pushed_authorization_request_endpoint\":\"https://mock-as/par\","
					+ "\"authorization_endpoint\":\"https://mock-as/authorize\","
					+ "\"token_endpoint\":\"https://mock-as/token\"}");
	}

	private BlueskyResolver resolver(HttpGetter http) {
		return new BlueskyResolver(http, RESOLVER, PLC);
	}

	@Test
	public void resolvesFullPlcChain() throws IOException {
		ResolvedAccount acct = resolver(plcChain()).resolve("alice.test");

		assertEquals("did:plc:abc123", acct.getIdentity().getDid());
		assertEquals("alice.test", acct.getIdentity().getHandle());
		assertEquals("https://mock-pds", acct.getIdentity().getPdsUrl());
		assertEquals("https://mock-as", acct.getAuthServer().getIssuer());
		assertEquals("https://mock-as/par", acct.getAuthServer().getPushedAuthorizationRequestEndpoint());
		assertEquals("https://mock-as/authorize", acct.getAuthServer().getAuthorizationEndpoint());
		assertEquals("https://mock-as/token", acct.getAuthServer().getTokenEndpoint());
	}

	@Test
	public void trailingSlashOnServiceEndpointIsStripped() throws IOException {
		FakeHttp http = plcChain();
		http.put(PLC + "/did:plc:abc123",
				"{\"service\":[{\"type\":\"AtprotoPersonalDataServer\",\"serviceEndpoint\":\"https://mock-pds/\"}]}");
		assertEquals("https://mock-pds", resolver(http).resolveDidToPds("did:plc:abc123"));
	}

	@Test
	public void resolvesDidWebToPds() throws IOException {
		FakeHttp http = new FakeHttp().put(
				"https://example.com/.well-known/did.json",
				"{\"service\":[{\"type\":\"AtprotoPersonalDataServer\",\"serviceEndpoint\":\"https://web-pds\"}]}");
		assertEquals("https://web-pds", resolver(http).resolveDidToPds("did:web:example.com"));
	}

	@Test
	public void handleWithNoDidThrows() {
		FakeHttp http = new FakeHttp().put(
				RESOLVER + "/xrpc/com.atproto.identity.resolveHandle?handle=ghost.test", "{}");
		try {
			resolver(http).resolveHandleToDid("ghost.test");
			fail("expected IOException when no did is returned");
		} catch (IOException expected) { /* ok */ }
	}

	@Test
	public void didDocWithoutPdsServiceThrows() {
		FakeHttp http = new FakeHttp().put(PLC + "/did:plc:abc123",
				"{\"service\":[{\"type\":\"OtherService\",\"serviceEndpoint\":\"https://nope\"}]}");
		try {
			resolver(http).resolveDidToPds("did:plc:abc123");
			fail("expected IOException when no PDS service is present");
		} catch (IOException expected) { /* ok */ }
	}

	@Test
	public void pdsWithoutAuthServersThrows() {
		FakeHttp http = new FakeHttp().put(
				"https://mock-pds/.well-known/oauth-protected-resource", "{\"authorization_servers\":[]}");
		try {
			resolver(http).resolveAuthServer("https://mock-pds");
			fail("expected IOException when PDS advertises no authorization servers");
		} catch (IOException expected) { /* ok */ }
	}

	@Test
	public void unsupportedDidMethodThrows() {
		try {
			resolver(new FakeHttp()).resolveDidToPds("did:example:whatever");
			fail("expected IOException for unsupported DID method");
		} catch (IOException expected) { /* ok */ }
	}
}
