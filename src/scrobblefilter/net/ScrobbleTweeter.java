package scrobblefilter.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import scrobblefilter.model.ScrobbledArtist;
import scrobblefilter.model.User;
import scrobblefilter.net.impl.NetworkedScrobbleListFetcher;
import twitter4j.TwitterException;

public class ScrobbleTweeter {

	private static final Logger log = Logger.getLogger(ScrobbleTweeter.class.getName());
	private static final String TWEET_URL = "https://api.twitter.com/2/tweets";

	ScrobbleListFetcher scrobbleFetcher = new NetworkedScrobbleListFetcher();

	public void doTweet(User user) throws TwitterException {
		List<ScrobbledArtist> scrobbles = extractFilteredList(user.getLastfmName(), user.getFilteredArtistAsStrings());
		String text = constructTweet(scrobbles);
		if (user.getToken() == null || user.getTokenSecret() == null)
			throw new TwitterException("token or secret null");
		try {
			Properties props = loadTwitterProperties();
			String consumerKey = props.getProperty("twitter4j.oauth.consumerKey");
			String consumerSecret = props.getProperty("twitter4j.oauth.consumerSecret");
			postTweet(text, consumerKey, consumerSecret, user.getToken(), user.getTokenSecret());
		} catch (TwitterException e) {
			throw e;
		} catch (Exception e) {
			throw new TwitterException("Failed to send tweet: " + e.getMessage());
		}
	}

	private Properties loadTwitterProperties() throws IOException {
		Properties props = new Properties();
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("twitter4j.properties")) {
			props.load(is);
		}
		return props;
	}

	private void postTweet(String text, String consumerKey, String consumerSecret,
			String accessToken, String accessTokenSecret) throws Exception {
		String nonce = UUID.randomUUID().toString().replace("-", "");
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

		Map<String, String> oauthParams = new LinkedHashMap<>();
		oauthParams.put("oauth_consumer_key", consumerKey);
		oauthParams.put("oauth_nonce", nonce);
		oauthParams.put("oauth_signature_method", "HMAC-SHA1");
		oauthParams.put("oauth_timestamp", timestamp);
		oauthParams.put("oauth_token", accessToken);
		oauthParams.put("oauth_version", "1.0");

		String authHeader = OAuth1Helper.buildAuthHeader("POST", TWEET_URL, oauthParams, consumerSecret, accessTokenSecret);

		String body = "{\"text\":\"" + escapeJson(text) + "\"}";
		HttpClient client = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NEVER)
				.build();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(TWEET_URL))
				.header("Authorization", authHeader)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		log.info("Tweet response: " + response.statusCode() + " " + response.body());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new TwitterException("Tweet API returned " + response.statusCode() + ": " + response.body());
		}
	}

	private String escapeJson(String text) {
		return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
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

	public String constructTweet(List<ScrobbledArtist> scrobbles) {
		String result = "I've been listening to";
		for (int i = 0; i < 3; i++) {
			ScrobbledArtist scrobble = scrobbles.get(i);
			result = result + (i == 2 ? " and " : " ") + scrobble.getName()
					+ (i == 2 ? "." : ",");
		}
		return result;
	}
}
