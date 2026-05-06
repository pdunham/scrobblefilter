package scrobblefilter.net;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OAuth1Helper {

	private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
	private static final String ACCESS_TOKEN_URL  = "https://api.twitter.com/oauth/access_token";

	/**
	 * Step 1 of sign-in: exchange consumer credentials + callback for a request token.
	 * Returns map with oauth_token, oauth_token_secret, oauth_callback_confirmed.
	 */
	public static Map<String, String> getRequestToken(
			String consumerKey, String consumerSecret, String callbackURL) throws Exception {
		String nonce = UUID.randomUUID().toString().replace("-", "");
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

		Map<String, String> params = new LinkedHashMap<>();
		params.put("oauth_callback", callbackURL);
		params.put("oauth_consumer_key", consumerKey);
		params.put("oauth_nonce", nonce);
		params.put("oauth_signature_method", "HMAC-SHA1");
		params.put("oauth_timestamp", timestamp);
		params.put("oauth_version", "1.0");

		// Signing key has no token secret yet
		String signature = computeSignature("POST", REQUEST_TOKEN_URL, params, consumerSecret, "");
		params.put("oauth_signature", signature);

		String authHeader = toAuthHeader(params);
		HttpResponse<String> response = oauthPost(REQUEST_TOKEN_URL, authHeader);
		if (response.statusCode() != 200) {
			throw new Exception("Request token failed " + response.statusCode() + ": " + response.body());
		}
		return parseForm(response.body());
	}

	/**
	 * Step 3 of sign-in: exchange request token + verifier for an access token.
	 * Returns map with oauth_token, oauth_token_secret, user_id, screen_name.
	 */
	public static Map<String, String> getAccessToken(
			String consumerKey, String consumerSecret,
			String requestToken, String requestTokenSecret, String verifier) throws Exception {
		String nonce = UUID.randomUUID().toString().replace("-", "");
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

		Map<String, String> params = new LinkedHashMap<>();
		params.put("oauth_consumer_key", consumerKey);
		params.put("oauth_nonce", nonce);
		params.put("oauth_signature_method", "HMAC-SHA1");
		params.put("oauth_timestamp", timestamp);
		params.put("oauth_token", requestToken);
		params.put("oauth_verifier", verifier);
		params.put("oauth_version", "1.0");

		String signature = computeSignature("POST", ACCESS_TOKEN_URL, params, consumerSecret, requestTokenSecret);
		params.put("oauth_signature", signature);

		String authHeader = toAuthHeader(params);
		HttpResponse<String> response = oauthPost(ACCESS_TOKEN_URL, authHeader);
		if (response.statusCode() != 200) {
			throw new Exception("Access token failed " + response.statusCode() + ": " + response.body());
		}
		return parseForm(response.body());
	}

	/**
	 * Builds a complete OAuth Authorization header for an arbitrary request.
	 * Callers supply the base OAuth params (without oauth_signature); this method
	 * computes and appends the signature.
	 */
	public static String buildAuthHeader(String method, String url,
			Map<String, String> oauthParams, String consumerSecret, String tokenSecret) throws Exception {
		Map<String, String> params = new LinkedHashMap<>(oauthParams);
		String signature = computeSignature(method, url, params, consumerSecret, tokenSecret);
		params.put("oauth_signature", signature);
		return toAuthHeader(params);
	}

	static String computeSignature(String method, String url, Map<String, String> params,
			String consumerSecret, String tokenSecret) throws Exception {
		String paramString = params.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(e -> percentEncode(e.getKey()) + "=" + percentEncode(e.getValue()))
				.collect(Collectors.joining("&"));
		String baseString = method + "&" + percentEncode(url) + "&" + percentEncode(paramString);
		String signingKey = percentEncode(consumerSecret) + "&" + percentEncode(tokenSecret);
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
		return Base64.getEncoder().encodeToString(mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8)));
	}

	static String percentEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8)
				.replace("+", "%20")
				.replace("*", "%2A")
				.replace("%7E", "~");
	}

	private static String toAuthHeader(Map<String, String> params) {
		return "OAuth " + params.entrySet().stream()
				.map(e -> percentEncode(e.getKey()) + "=\"" + percentEncode(e.getValue()) + "\"")
				.collect(Collectors.joining(", "));
	}

	private static HttpResponse<String> oauthPost(String url, String authHeader) throws Exception {
		HttpClient client = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NEVER)
				.build();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Authorization", authHeader)
				.POST(HttpRequest.BodyPublishers.noBody())
				.build();
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private static Map<String, String> parseForm(String body) {
		Map<String, String> map = new HashMap<>();
		for (String pair : body.split("&")) {
			String[] kv = pair.split("=", 2);
			if (kv.length == 2) {
				map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
						URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
			}
		}
		return map;
	}
}
