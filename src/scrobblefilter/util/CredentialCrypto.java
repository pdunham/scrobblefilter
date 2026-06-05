package scrobblefilter.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Symmetric authenticated encryption for per-user secrets stored in Datastore
 * (e.g. Bluesky OAuth refresh token + DPoP private key). AES-256-GCM with a
 * random 96-bit IV and 128-bit auth tag.
 *
 * <p>The 256-bit key is supplied as a base64 string in the {@code CRED_ENC_KEY}
 * environment variable, sourced from Secret Manager and injected on Cloud Run
 * the same way as {@code CRON_TOKEN} / {@code MIGRATE_TOKEN} (see
 * {@link scrobblefilter.web.AdminAuth}). Wire-format of {@link #encrypt} output:
 * {@code base64(iv ‖ ciphertext ‖ tag)} — the GCM tag is appended to the
 * ciphertext by the JCE provider.
 */
public class CredentialCrypto {

	private static final String ENV_VAR = "CRED_ENC_KEY";
	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int KEY_BYTES = 32;   // AES-256
	private static final int IV_BYTES = 12;    // 96-bit nonce, recommended for GCM
	private static final int TAG_BITS = 128;

	private final SecretKeySpec key;
	private final SecureRandom random = new SecureRandom();

	/** Production: read the base64-encoded 32-byte key from {@code CRED_ENC_KEY}. */
	public CredentialCrypto() {
		this(keyFromEnv());
	}

	/** Test seam / explicit key: supply the raw 32-byte key directly. */
	public CredentialCrypto(byte[] keyBytes) {
		if (keyBytes == null || keyBytes.length != KEY_BYTES) {
			throw new IllegalArgumentException(
					ENV_VAR + " must decode to " + KEY_BYTES + " bytes (AES-256); got "
							+ (keyBytes == null ? "null" : keyBytes.length));
		}
		this.key = new SecretKeySpec(keyBytes, "AES");
	}

	private static byte[] keyFromEnv() {
		String b64 = System.getenv(ENV_VAR);
		if (b64 == null || b64.isEmpty()) {
			throw new IllegalStateException(ENV_VAR + " is not set");
		}
		return Base64.getDecoder().decode(b64);
	}

	/** Encrypt UTF-8 plaintext; returns base64(iv ‖ ciphertext ‖ tag). */
	public String encrypt(String plaintext) {
		if (plaintext == null) throw new IllegalArgumentException("plaintext is null");
		try {
			byte[] iv = new byte[IV_BYTES];
			random.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
			byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

			byte[] out = new byte[iv.length + ct.length];
			System.arraycopy(iv, 0, out, 0, iv.length);
			System.arraycopy(ct, 0, out, iv.length, ct.length);
			return Base64.getEncoder().encodeToString(out);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("encrypt failed", e);
		}
	}

	/**
	 * Decrypt a value produced by {@link #encrypt}. Throws if the ciphertext was
	 * tampered with or was encrypted under a different key (GCM authentication).
	 */
	public String decrypt(String encoded) {
		if (encoded == null) throw new IllegalArgumentException("ciphertext is null");
		try {
			byte[] in = Base64.getDecoder().decode(encoded);
			if (in.length <= IV_BYTES) {
				throw new IllegalArgumentException("ciphertext too short");
			}
			byte[] iv = Arrays.copyOfRange(in, 0, IV_BYTES);
			byte[] ct = Arrays.copyOfRange(in, IV_BYTES, in.length);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
			return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("decrypt failed", e);
		}
	}
}
