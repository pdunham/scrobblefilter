package scrobblefilter.net;

import java.util.List;
import java.util.logging.Logger;

import scrobblefilter.model.ScrobbledArtist;
import scrobblefilter.model.User;
import scrobblefilter.net.impl.NetworkedScrobbleListFetcher;

/**
 * Builds the platform-agnostic status text ("I've been listening to A, B, and
 * C.") from a user's recent Last.fm scrobbles minus their filtered artists.
 *
 * Extracted from the former ScrobbleTweeter so the text is composed once and
 * handed to every enabled {@link SocialPoster}, rather than each poster
 * re-fetching from Last.fm.
 */
public class StatusComposer {

	private static final Logger log = Logger.getLogger(StatusComposer.class.getName());

	private final ScrobbleListFetcher scrobbleFetcher;

	public StatusComposer() {
		this(new NetworkedScrobbleListFetcher());
	}

	/** Test seam: inject a fake fetcher. */
	public StatusComposer(ScrobbleListFetcher scrobbleFetcher) {
		this.scrobbleFetcher = scrobbleFetcher;
	}

	public String compose(User user) {
		List<ScrobbledArtist> scrobbles =
				extractFilteredList(user.getLastfmName(), user.getFilteredArtistAsStrings());
		return constructStatus(scrobbles);
	}

	public List<ScrobbledArtist> extractFilteredList(String lastfmName, List<String> filteredArtists) {
		if (lastfmName == null) log.warning("in extractFilteredList lastfmname is null");
		if (filteredArtists == null) log.warning("in extractFilteredList filteredArtist is null");
		List<ScrobbledArtist> artists = ScrobbleListParser.parseList(scrobbleFetcher.fetchList(lastfmName));
		for (String filtered : filteredArtists) {
			artists.remove(new ScrobbledArtist(filtered, 0));
		}
		return artists;
	}

	public String constructStatus(List<ScrobbledArtist> scrobbles) {
		String result = "I've been listening to";
		for (int i = 0; i < 3; i++) {
			ScrobbledArtist scrobble = scrobbles.get(i);
			result = result + (i == 2 ? " and " : " ") + scrobble.getName()
					+ (i == 2 ? "." : ",");
		}
		return result;
	}
}
