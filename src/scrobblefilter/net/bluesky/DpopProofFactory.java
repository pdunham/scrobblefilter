package scrobblefilter.net.bluesky;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Builds DPoP proof JWTs (RFC 9449) for AT Protocol OAuth requests: an ES256 JWS
 * with {@code typ=dpop+jwt} and the public JWK in the header, claiming the HTTP
 * method/URI, a unique {@code jti}, and {@code iat}. Includes the server-issued
 * {@code nonce} and, for resource requests, the access-token hash {@code ath}.
 */
public class DpopProofFactory {

	private static final JOSEObjectType DPOP_JWT = new JOSEObjectType("dpop+jwt");

	/**
	 * @param key         the account's DPoP keypair (private)
	 * @param htm         HTTP method, e.g. "POST"
	 * @param htu         target URI; its query and fragment are stripped per RFC 9449
	 * @param nonce       server-issued DPoP nonce, or null on the first attempt
	 * @param accessToken access token to bind via {@code ath}, or null for token-endpoint use
	 */
	public String createProof(ECKey key, String htm, String htu, String nonce, String accessToken) {
		try {
			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
					.type(DPOP_JWT)
					.jwk(key.toPublicJWK())
					.build();

			JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
					.jwtID(UUID.randomUUID().toString())
					.claim("htm", htm)
					.claim("htu", normalizeHtu(htu))
					.issueTime(Date.from(Instant.now()));
			if (nonce != null && !nonce.isEmpty()) {
				claims.claim("nonce", nonce);
			}
			if (accessToken != null && !accessToken.isEmpty()) {
				claims.claim("ath", sha256Base64Url(accessToken));
			}

			SignedJWT jwt = new SignedJWT(header, claims.build());
			jwt.sign(new ECDSASigner(key));
			return jwt.serialize();
		} catch (JOSEException e) {
			throw new IllegalStateException("failed to build DPoP proof", e);
		}
	}

	/** Strip query and fragment from the htu, keeping scheme/authority/path. */
	static String normalizeHtu(String htu) {
		try {
			URI u = URI.create(htu);
			return new URI(u.getScheme(), u.getAuthority(), u.getPath(), null, null).toString();
		} catch (Exception e) {
			return htu;
		}
	}

	/** base64url(SHA-256(value)), no padding — used for the {@code ath} claim. */
	static String sha256Base64Url(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.US_ASCII));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
		} catch (Exception e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}
}
