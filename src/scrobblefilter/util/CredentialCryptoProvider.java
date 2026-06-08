package scrobblefilter.util;

/**
 * Lazily builds the shared {@link CredentialCrypto} on first use.
 *
 * <p>Wiring {@code CredentialCrypto} as an eager bean would read {@code CRED_ENC_KEY}
 * at startup and fail app boot wherever the key isn't set. Bluesky is opt-in, so a
 * missing key must not break boot or the Twitter path — it should only surface when
 * someone actually exercises Bluesky. This provider defers construction (and thus
 * the key check) until the first {@link #get()}.
 */
public class CredentialCryptoProvider {

	private volatile CredentialCrypto instance;

	public CredentialCryptoProvider() {
	}

	/** Explicit/test seam: provide a ready instance (e.g. with a known key). */
	public CredentialCryptoProvider(CredentialCrypto instance) {
		this.instance = instance;
	}

	/** Returns the shared instance, constructing it on first call. Throws if CRED_ENC_KEY is unset. */
	public CredentialCrypto get() {
		CredentialCrypto local = instance;
		if (local == null) {
			synchronized (this) {
				if (instance == null) {
					instance = new CredentialCrypto();
				}
				local = instance;
			}
		}
		return local;
	}
}
