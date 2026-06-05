package scrobblefilter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.Base64;

import org.junit.Test;

public class CredentialCryptoTest {

	private static byte[] key(byte fill) {
		byte[] k = new byte[32];
		java.util.Arrays.fill(k, fill);
		return k;
	}

	private final CredentialCrypto crypto = new CredentialCrypto(key((byte) 7));

	@Test
	public void roundTripsPlaintext() {
		String secret = "did:plc:abc123.refresh-token-xyz";
		assertEquals(secret, crypto.decrypt(crypto.encrypt(secret)));
	}

	@Test
	public void roundTripsUnicodeAndEmpty() {
		assertEquals("", crypto.decrypt(crypto.encrypt("")));
		String unicode = "ñoño 🎵 — café";
		assertEquals(unicode, crypto.decrypt(crypto.encrypt(unicode)));
	}

	@Test
	public void samePlaintextEncryptsDifferently() {
		// Random IV per call → distinct ciphertexts, both decrypting correctly.
		String secret = "app-password-or-token";
		String a = crypto.encrypt(secret);
		String b = crypto.encrypt(secret);
		assertNotEquals(a, b);
		assertEquals(secret, crypto.decrypt(a));
		assertEquals(secret, crypto.decrypt(b));
	}

	@Test
	public void tamperedCiphertextIsRejected() {
		byte[] raw = Base64.getDecoder().decode(crypto.encrypt("sensitive"));
		raw[raw.length - 1] ^= 0x01; // flip a bit in the GCM tag
		String tampered = Base64.getEncoder().encodeToString(raw);
		try {
			crypto.decrypt(tampered);
			fail("expected decrypt of tampered ciphertext to throw");
		} catch (IllegalStateException expected) {
			// GCM authentication failure surfaces as IllegalStateException
		}
	}

	@Test
	public void wrongKeyCannotDecrypt() {
		String token = crypto.encrypt("sensitive");
		CredentialCrypto other = new CredentialCrypto(key((byte) 9));
		try {
			other.decrypt(token);
			fail("expected decrypt under a different key to throw");
		} catch (IllegalStateException expected) {
			// authentication failure
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsWrongKeyLength() {
		new CredentialCrypto(new byte[16]); // 128-bit key, not 256
	}
}
