package scrobblefilter.net.bluesky;

/** Tokens returned by the AT Protocol authorization server's token endpoint. */
public class TokenSet {

	private final String accessToken;
	private final String refreshToken;
	private final String did;       // the "sub" claim — the account DID
	private final String scope;
	private final long expiresInSeconds;

	public TokenSet(String accessToken, String refreshToken, String did, String scope, long expiresInSeconds) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.did = did;
		this.scope = scope;
		this.expiresInSeconds = expiresInSeconds;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getDid() {
		return did;
	}

	public String getScope() {
		return scope;
	}

	public long getExpiresInSeconds() {
		return expiresInSeconds;
	}
}
