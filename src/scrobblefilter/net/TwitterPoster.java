package scrobblefilter.net;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import scrobblefilter.model.User;

/**
 * {@link SocialPoster} for Twitter/X. Posts via the v2 tweets endpoint signed
 * with a hand-rolled OAuth 1.0a header ({@link OAuth1Helper}).
 *
 * Formerly {@code ScrobbleTweeter}; the status-text composition moved to
 * {@link StatusComposer} so this class is purely the Twitter posting target.
 */
public class TwitterPoster implements SocialPoster {

	private static final Logger log = Logger.getLogger(TwitterPoster.class.getName());
	private static final String TWEET_URL = "https://api.twitter.com/2/tweets";

	@Override
	public String platform() {
		return "twitter";
	}

	@Override
	public boolean isConnected(User user) {
		return user.getToken() != null && user.getTokenSecret() != null;
	}

	@Override
	public boolean isEnabledFor(User user) {
		return isConnected(user) && user.isCron();
	}

	@Override
	public void post(User user, String statusText) throws SocialPostException {
		if (!isConnected(user)) {
			throw new SocialPostException("token or secret null");
		}
		try {
			Properties props = loadTwitterProperties();
			String consumerKey = props.getProperty("twitter4j.oauth.consumerKey");
			String consumerSecret = props.getProperty("twitter4j.oauth.consumerSecret");
			postTweet(statusText, consumerKey, consumerSecret, user.getToken(), user.getTokenSecret());
		} catch (SocialPostException e) {
			throw e;
		} catch (Exception e) {
			throw new SocialPostException("Failed to send tweet: " + e.getMessage());
		}
	}

	private Properties loadTwitterProperties() throws Exception {
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
			throw new SocialPostException("Tweet API returned " + response.statusCode() + ": " + response.body());
		}
	}

	private String escapeJson(String text) {
		return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}
}
