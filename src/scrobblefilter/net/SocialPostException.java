package scrobblefilter.net;

/**
 * Thrown when a {@link SocialPoster} cannot publish a status (auth, network, or
 * platform API error). Platform-agnostic so callers can fan out over multiple
 * posters and handle a single failure type per target.
 */
public class SocialPostException extends Exception {

	public SocialPostException(String message) {
		super(message);
	}

	public SocialPostException(String message, Throwable cause) {
		super(message, cause);
	}
}
