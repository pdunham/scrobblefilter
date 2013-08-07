package scrobblefilter.net;

import java.util.List;
import java.util.logging.Logger;

import scrobblefilter.model.ScrobbledArtist;
import scrobblefilter.model.User;
import scrobblefilter.net.impl.NetworkedScrobbleListFetcher;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class ScrobbleTweeter {

	private static final Logger log = Logger.getLogger(ScrobbleTweeter.class.getName());

	ScrobbleListFetcher scrobbleFetcher = new NetworkedScrobbleListFetcher();
	
	public void doTweet(User user) throws TwitterException {
		List<ScrobbledArtist> scrobbles = extractFilteredList(user.getLastfmName(), user.getFilteredArtistAsStrings());
		String text = constructTweet(scrobbles);	        
		if (user.getToken()==null || user.getTokenSecret() == null) throw new TwitterException("token or secret null");
		AccessToken token = new AccessToken(user.getToken(), user.getTokenSecret());
		Twitter twitter = new TwitterFactory().getInstance(token);
		twitter.updateStatus(text);
	}
	
	/**
	 * The whole point of this app boils down to this method.
	 * 
	 * @param lastfmName
	 * @param filteredArtists
	 * @return a List of ScrobbledArtists, scrubbed of the filtered artists.
	 */
	public List<ScrobbledArtist> extractFilteredList(String lastfmName,
			List<String> filteredArtists) {
		if (lastfmName==null) log.warning("in extractFilteredList lastfmname is null");
		if (filteredArtists == null ) log.warning("in extractFilteredList filteredArtist is null");
		List<ScrobbledArtist> artists = ScrobbleListParser.parseList( scrobbleFetcher.fetchList(lastfmName) );
		for (String filtered : filteredArtists) {
			artists.remove( new ScrobbledArtist(filtered, 0) );
		}
		return artists;
	}
	
	public String constructTweet(List<ScrobbledArtist> scrobbles) {
		String result = "I've been listening to";
		for (int i = 0; i < 3; i++) {
			ScrobbledArtist scrobble = scrobbles.get(i);
			result = result +  ( i==2?" and ":" ") + scrobble.getName() 
					/*+ " (" + scrobble.getPlayCount() + ")"*/
					+  ( i==2?".":",");
		}
		return result;
	}
	
}
