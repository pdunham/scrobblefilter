package scrobblefilter.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Assume;
import org.junit.Test;

public class CredentialCryptoProviderTest {

	@Test
	public void constructionNeverFails() {
		// Boot safety: building the provider must not read the key or throw,
		// even when CRED_ENC_KEY is absent.
		assertNotNull(new CredentialCryptoProvider());
	}

	@Test
	public void getSurfacesMissingKeyLazily() {
		// Only meaningful when the key is absent (the usual test/CI environment).
		Assume.assumeTrue(System.getenv("CRED_ENC_KEY") == null);
		try {
			new CredentialCryptoProvider().get();
			fail("expected IllegalStateException when CRED_ENC_KEY is unset");
		} catch (IllegalStateException expected) {
			// the key check happens on first use, not at construction
		}
	}
}
