package scrobblefilter.net.bluesky;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PKCE (RFC 7636) pair for the authorization-code flow: a high-entropy
 * {@code code_verifier} and its S256 {@code code_challenge}.
 */
public final class Pkce {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final String verifier;
	private final String challenge;

	private Pkce(String verifier, String challenge) {
		this.verifier = verifier;
		this.challenge = challenge;
	}

	public static Pkce generate() {
		byte[] raw = new byte[32];
		RANDOM.nextBytes(raw);
		String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
		return new Pkce(verifier, s256(verifier));
	}

	public String verifier() {
		return verifier;
	}

	public String challenge() {
		return challenge;
	}

	public String method() {
		return "S256";
	}

	/** base64url(SHA-256(input)), no padding — the S256 transform. */
	static String s256(String input) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(input.getBytes(StandardCharsets.US_ASCII));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
		} catch (Exception e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}
}
