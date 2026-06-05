package scrobblefilter.net.bluesky;

/** Full result of resolving a handle: the account identity plus its OAuth server metadata. */
public class ResolvedAccount {

	private final BlueskyIdentity identity;
	private final AuthServerMetadata authServer;

	public ResolvedAccount(BlueskyIdentity identity, AuthServerMetadata authServer) {
		this.identity = identity;
		this.authServer = authServer;
	}

	public BlueskyIdentity getIdentity() {
		return identity;
	}

	public AuthServerMetadata getAuthServer() {
		return authServer;
	}
}
