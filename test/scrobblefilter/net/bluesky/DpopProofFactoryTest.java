package scrobblefilter.net.bluesky;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.junit.Test;

public class DpopProofFactoryTest {

	private final DpopProofFactory factory = new DpopProofFactory();
	private final ECKey key = DpopKeys.generate();

	private JWTClaimsSet verifyAndParse(String proof) throws Exception {
		SignedJWT jwt = SignedJWT.parse(proof);
		assertTrue("proof must verify against its own public key",
				jwt.verify(new ECDSAVerifier(key.toPublicJWK())));
		assertEquals(JWSAlgorithm.ES256, jwt.getHeader().getAlgorithm());
		assertEquals("dpop+jwt", jwt.getHeader().getType().getType());
		assertEquals("embedded jwk must be the public key", key.toPublicJWK(), jwt.getHeader().getJWK());
		return jwt.getJWTClaimsSet();
	}

	@Test
	public void buildsBasicProofWithStrippedHtu() throws Exception {
		String proof = factory.createProof(key, "POST", "https://as.example/token?foo=bar#frag", null, null);
		JWTClaimsSet claims = verifyAndParse(proof);

		assertEquals("POST", claims.getStringClaim("htm"));
		assertEquals("https://as.example/token", claims.getStringClaim("htu"));
		assertNotNull("jti required", claims.getJWTID());
		assertNotNull("iat required", claims.getIssueTime());
		assertNull(claims.getStringClaim("nonce"));
		assertNull(claims.getStringClaim("ath"));
	}

	@Test
	public void includesNonceWhenProvided() throws Exception {
		JWTClaimsSet claims = verifyAndParse(
				factory.createProof(key, "POST", "https://as.example/par", "nonce-123", null));
		assertEquals("nonce-123", claims.getStringClaim("nonce"));
	}

	@Test
	public void includesAccessTokenHash() throws Exception {
		String accessToken = "access-token-value";
		JWTClaimsSet claims = verifyAndParse(
				factory.createProof(key, "GET", "https://pds.example/xrpc/x", null, accessToken));
		assertEquals(DpopProofFactory.sha256Base64Url(accessToken), claims.getStringClaim("ath"));
	}

	@Test
	public void distinctJtiPerProof() throws Exception {
		String a = SignedJWT.parse(factory.createProof(key, "POST", "https://as.example/token", null, null))
				.getJWTClaimsSet().getJWTID();
		String b = SignedJWT.parse(factory.createProof(key, "POST", "https://as.example/token", null, null))
				.getJWTClaimsSet().getJWTID();
		org.junit.Assert.assertNotEquals(a, b);
	}
}
