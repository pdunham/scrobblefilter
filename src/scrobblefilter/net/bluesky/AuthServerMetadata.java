package scrobblefilter.net.bluesky;

/**
 * The subset of an AT Protocol OAuth authorization server's metadata
 * ({@code /.well-known/oauth-authorization-server}) that the connect flow needs.
 */
public class AuthServerMetadata {

	private final String issuer;
	private final String pushedAuthorizationRequestEndpoint;
	private final String authorizationEndpoint;
	private final String tokenEndpoint;

	public AuthServerMetadata(String issuer, String pushedAuthorizationRequestEndpoint,
			String authorizationEndpoint, String tokenEndpoint) {
		this.issuer = issuer;
		this.pushedAuthorizationRequestEndpoint = pushedAuthorizationRequestEndpoint;
		this.authorizationEndpoint = authorizationEndpoint;
		this.tokenEndpoint = tokenEndpoint;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getPushedAuthorizationRequestEndpoint() {
		return pushedAuthorizationRequestEndpoint;
	}

	public String getAuthorizationEndpoint() {
		return authorizationEndpoint;
	}

	public String getTokenEndpoint() {
		return tokenEndpoint;
	}
}
