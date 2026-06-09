package scrobblefilter.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Salted, peppered password hashing for ScrobbleFilter accounts.
 *
 * <p>PBKDF2WithHmacSHA256 over a per-user random salt; the password is prefixed
 * with an app-wide secret <em>pepper</em> (from the {@code PASSWORD_PEPPER} env,
 * sourced from Secret Manager like {@code CRED_ENC_KEY}) so a Datastore-only leak
 * can't be brute-forced offline. The pepper is never stored.
 *
 * <p>Stored format (the salt is embedded; the pepper is not):
 * {@code pbkdf2$<iterations>$<base64 salt>$<base64 hash>}.
 *
 * <p>The pepper is read lazily so a missing value surfaces at login/registration
 * rather than crashing app boot (mirrors {@link CredentialCryptoProvider}); a test
 * constructor injects an explicit pepper.
 */
public class PasswordHasher {

	private static final String ENV_VAR = "PASSWORD_PEPPER";
	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final String PREFIX = "pbkdf2";
	private static final int ITERATIONS = 210_000; // OWASP-ish for PBKDF2-HMAC-SHA256
	private static final int SALT_BYTES = 16;
	private static final int HASH_BITS = 256;

	private final SecureRandom random = new SecureRandom();
	private final String explicitPepper; // null => read from env lazily

	/** Production: read the pepper from {@code PASSWORD_PEPPER} on first use. */
	public PasswordHasher() {
		this.explicitPepper = null;
	}

	/** Test/explicit seam: supply the pepper directly. */
	public PasswordHasher(String pepper) {
		this.explicitPepper = pepper;
	}

	/** Hash a password; returns {@code pbkdf2$iterations$salt$hash}. */
	public String hash(String password) {
		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("password is empty");
		}
		byte[] salt = new byte[SALT_BYTES];
		random.nextBytes(salt);
		byte[] hash = pbkdf2(password, salt, ITERATIONS);
		return PREFIX + "$" + ITERATIONS + "$"
				+ Base64.getEncoder().encodeToString(salt) + "$"
				+ Base64.getEncoder().encodeToString(hash);
	}

	/** Verify a password against a stored {@code pbkdf2$…} value (constant-time). */
	public boolean verify(String password, String stored) {
		if (password == null || stored == null) return false;
		String[] parts = stored.split("\\$");
		if (parts.length != 4 || !PREFIX.equals(parts[0])) return false;
		try {
			int iterations = Integer.parseInt(parts[1]);
			byte[] salt = Base64.getDecoder().decode(parts[2]);
			byte[] expected = Base64.getDecoder().decode(parts[3]);
			byte[] actual = pbkdf2(password, salt, iterations);
			return MessageDigest.isEqual(expected, actual);
		} catch (RuntimeException e) {
			return false; // malformed stored value
		}
	}

	private byte[] pbkdf2(String password, byte[] salt, int iterations) {
		// Prefix the pepper so a Datastore-only leak lacks the secret needed to crack.
		char[] peppered = (pepper() + password).toCharArray();
		try {
			KeySpec spec = new PBEKeySpec(peppered, salt, iterations, HASH_BITS);
			return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
		} catch (Exception e) {
			throw new IllegalStateException("PBKDF2 failed", e);
		}
	}

	private String pepper() {
		if (explicitPepper != null) return explicitPepper;
		String v = System.getenv(ENV_VAR);
		if (v == null || v.trim().isEmpty()) {
			throw new IllegalStateException(ENV_VAR + " is not set");
		}
		return v.trim();
	}
}
