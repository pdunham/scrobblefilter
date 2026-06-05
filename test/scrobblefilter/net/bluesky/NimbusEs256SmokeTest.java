package scrobblefilter.net.bluesky;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.junit.Test;

/**
 * Smoke test for the Nimbus JOSE+JWT dependency: confirm this JDK can generate a
 * P-256 key and produce/verify an ES256 JWS shaped like an AT Protocol DPoP proof
 * (typ=dpop+jwt, embedded public JWK). De-risks the DPoP work in sub-step 3.
 */
public class NimbusEs256SmokeTest {

	@Test
	public void signsAndVerifiesEs256DpopShapedJws() throws Exception {
		ECKey ecKey = new ECKeyGenerator(Curve.P_256).generate();

		SignedJWT jwt = new SignedJWT(
				new JWSHeader.Builder(JWSAlgorithm.ES256)
						.type(new JOSEObjectType("dpop+jwt"))
						.jwk(ecKey.toPublicJWK())
						.build(),
				new JWTClaimsSet.Builder()
						.claim("htm", "POST")
						.claim("htu", "https://bsky.social/oauth/token")
						.build());
		jwt.sign(new ECDSASigner(ecKey));

		SignedJWT parsed = SignedJWT.parse(jwt.serialize());

		assertTrue("ES256 signature should verify", parsed.verify(new ECDSAVerifier(ecKey.toPublicJWK())));
		assertEquals(JWSAlgorithm.ES256, parsed.getHeader().getAlgorithm());
		assertEquals("dpop+jwt", parsed.getHeader().getType().getType());
		assertEquals("POST", parsed.getJWTClaimsSet().getClaim("htm"));
	}
}
