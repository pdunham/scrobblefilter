package scrobblefilter.net.bluesky;

import java.io.IOException;

/**
 * Thrown when an AT Protocol OAuth token request is rejected by the
 * authorization server. Carries the HTTP status and the OAuth {@code error}
 * code so callers can distinguish a permanently dead session
 * ({@code invalid_grant} — the refresh token has expired or been revoked) from
 * transient or other failures, and react accordingly (e.g. drop the stored
 * credentials only when the session is genuinely gone, never on a network blip).
 */
public class BlueskyAuthException extends IOException {

	private final int status;
	private final String error;

	public BlueskyAuthException(int status, String error, String message) {
		super(message);
		this.status = status;
		this.error = error;
	}

	public int status() {
		return status;
	}

	/** The OAuth {@code error} code from the response body, or null if absent. */
	public String error() {
		return error;
	}

	/**
	 * True when the authorization server reported {@code invalid_grant} on a
	 * 400 — i.e. the refresh token is permanently dead and the user must
	 * re-establish the connection.
	 */
	public boolean isInvalidGrant() {
		return status == 400 && "invalid_grant".equals(error);
	}
}
