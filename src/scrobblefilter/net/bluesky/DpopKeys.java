package scrobblefilter.net.bluesky;

import java.text.ParseException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;

/**
 * Helpers for the per-account DPoP key (an EC P-256 keypair). The key is created
 * when a user connects Bluesky and reused for the life of the session; its JSON
 * (which includes the private {@code d}) is encrypted via CredentialCrypto and
 * stored on the User so tokens can be refreshed later.
 */
public final class DpopKeys {

	private DpopKeys() {
	}

	/** Generate a fresh P-256 signing key. */
	public static ECKey generate() {
		try {
			return new ECKeyGenerator(Curve.P_256)
					.keyUse(KeyUse.SIGNATURE)
					.keyIDFromThumbprint(true)
					.generate();
		} catch (JOSEException e) {
			throw new IllegalStateException("failed to generate DPoP key", e);
		}
	}

	/** Full JWK JSON including the private key — encrypt before persisting. */
	public static String toJson(ECKey key) {
		return key.toJSONString();
	}

	/** Parse a private EC JWK previously produced by {@link #toJson}. */
	public static ECKey fromJson(String json) {
		try {
			return ECKey.parse(json);
		} catch (ParseException e) {
			throw new IllegalArgumentException("invalid EC JWK JSON", e);
		}
	}
}
