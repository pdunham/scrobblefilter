package scrobblefilter.net.bluesky;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PkceTest {

	@Test
	public void challengeIsS256OfVerifier() {
		Pkce p = Pkce.generate();
		assertEquals(Pkce.s256(p.verifier()), p.challenge());
		assertEquals("S256", p.method());
	}

	@Test
	public void verifierIsUrlSafeAndHighEntropy() {
		String v = Pkce.generate().verifier();
		// 32 random bytes, base64url no padding -> 43 chars, URL-safe alphabet only.
		assertEquals(43, v.length());
		assertTrue("unexpected chars in verifier: " + v, v.matches("[A-Za-z0-9_-]+"));
	}

	@Test
	public void generatesDistinctPairs() {
		assertNotEquals(Pkce.generate().verifier(), Pkce.generate().verifier());
	}

	@Test
	public void s256KnownVector() {
		// RFC 7636 Appendix B worked example.
		assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
				Pkce.s256("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
	}
}
