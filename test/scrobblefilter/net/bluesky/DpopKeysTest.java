package scrobblefilter.net.bluesky;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;

import org.junit.Test;

public class DpopKeysTest {

	@Test
	public void generatesPrivateP256Key() throws Exception {
		ECKey key = DpopKeys.generate();
		assertTrue("generated key should be private", key.isPrivate());
		assertEquals("P-256", key.getCurve().getName());
		// Usable as an ES256 signer.
		new ECDSASigner(key);
	}

	@Test
	public void jsonRoundTripPreservesKey() {
		ECKey key = DpopKeys.generate();
		String json = DpopKeys.toJson(key);
		assertTrue("serialized JWK must include the private d", json.contains("\"d\""));

		ECKey restored = DpopKeys.fromJson(json);
		assertTrue(restored.isPrivate());
		assertEquals(key, restored);
		// Public projection is what goes in the DPoP header — must match.
		assertEquals(key.toPublicJWK(), restored.toPublicJWK());
	}

	@Test
	public void publicJwkHasNoPrivateMaterial() {
		assertFalse(DpopKeys.generate().toPublicJWK().isPrivate());
	}
}
