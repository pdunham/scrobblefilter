package scrobblefilter.net;

import scrobblefilter.model.User;

/**
 * Posts a status update to a single social platform on behalf of a user.
 *
 * Implementations own the platform-specific auth and HTTP; the status text is
 * built once (platform-agnostic) by {@link StatusComposer} and passed in, so a
 * weekly run hits Last.fm once regardless of how many targets are enabled.
 */
public interface SocialPoster {

	/** Stable platform identifier, e.g. "twitter" or "bluesky". Used in logs. */
	String platform();

	/** True when the user has stored credentials for this platform. */
	boolean isConnected(User user);

	/** True when the user is connected AND has opted in to posting here. */
	boolean isEnabledFor(User user);

	/**
	 * Publish {@code statusText} to the user's account on this platform.
	 *
	 * @throws SocialPostException on auth, network, or API failure
	 */
	void post(User user, String statusText) throws SocialPostException;
}
