package scrobblefilter.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import scrobblefilter.AppConfig;
import scrobblefilter.model.User;

/**
 * Authenticates users by redirecting through Last.fm Web Auth, then confirming
 * their identity via auth.getSession. No ScrobbleFilter password is involved.
 *
 * <p>GET lastfm/signin → redirect to Last.fm (or LASTFM_AUTH_URL override) with
 * api_key + cb.
 * <p>GET lastfm/callback?token=TOKEN → exchange for session name, look up or
 * create User, establish session, redirect to /hello/world.
 */
@Controller
public class LastfmSignInController {

	private static final Logger log = Logger.getLogger(LastfmSignInController.class.getName());

	private static final String LASTFM_AUTH_URL =
			System.getenv("LASTFM_AUTH_URL") != null
					? System.getenv("LASTFM_AUTH_URL")
					: "https://www.last.fm/api/auth/";

	private static final String LASTFM_BASE_URL =
			System.getenv("LASTFM_BASE_URL") != null
					? System.getenv("LASTFM_BASE_URL")
					: "http://ws.audioscrobbler.com/2.0/?";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final HttpClient HTTP = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	@RequestMapping(value = "welcome", method = GET)
	public ModelAndView welcome() {
		return new ModelAndView("newuser");
	}

	@RequestMapping(value = "lastfm/signin", method = GET)
	public void signin(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String apiKey = AppConfig.get("lastfm.api.key");
		String callbackUrl = callbackUrl(req);
		String redirect = LASTFM_AUTH_URL + "?api_key=" + enc(apiKey) + "&cb=" + enc(callbackUrl);
		res.sendRedirect(redirect);
	}

	@RequestMapping(value = "lastfm/callback", method = GET)
	public ModelAndView callback(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String token = req.getParameter("token");
		if (token == null || token.isEmpty()) {
			return new ModelAndView("redirect:/hello/welcome");
		}
		try {
			String apiKey = AppConfig.get("lastfm.api.key");
			String secret = apiSecret();

			Map<String, String> params = new TreeMap<>();
			params.put("api_key", apiKey);
			params.put("method", "auth.getSession");
			params.put("token", token);
			String sig = apiSig(params, secret);

			String url = LASTFM_BASE_URL + "method=auth.getSession"
					+ "&api_key=" + enc(apiKey)
					+ "&token=" + enc(token)
					+ "&api_sig=" + sig
					+ "&format=json";
			String lastfmName = extractSessionName(fetch(url));

			User user = RegistrationController.findUser(lastfmName);
			if (user == null) {
				user = new User();
				user.setLastfmName(lastfmName);
				user.save();
			}

			HttpSession existing = req.getSession(false);
			if (existing != null) existing.invalidate();
			req.getSession(true).setAttribute("user", user);
			return new ModelAndView("redirect:/hello/world");
		} catch (Exception e) {
			if (e instanceof InterruptedException) Thread.currentThread().interrupt();
			log.warning("Last.fm callback failed: " + e.getMessage());
			return new ModelAndView("redirect:/hello/welcome");
		}
	}

	private static String callbackUrl(HttpServletRequest req) {
		StringBuffer url = req.getRequestURL();
		String proto = req.getHeader("X-Forwarded-Proto");
		if (proto != null && url.indexOf("http:") == 0) {
			url.replace(0, 4, proto);
		}
		int idx = url.lastIndexOf("/");
		url.replace(idx, url.length(), "").append("/callback");
		return url.toString();
	}

	/**
	 * Parses an auth.getSession response and returns the Last.fm username.
	 * Error messages deliberately omit the response body, which can carry the
	 * session key. Package-private so it can be unit-tested without a live call.
	 */
	static String extractSessionName(String body) throws IOException {
		JsonNode root = MAPPER.readValue(body, JsonNode.class);
		JsonNode sessionNode = root.get("session");
		if (sessionNode == null) {
			throw new IOException("auth.getSession returned no session");
		}
		JsonNode nameNode = sessionNode.get("name");
		String name = nameNode != null ? nameNode.asText() : null;
		if (name == null || name.isEmpty()) {
			throw new IOException("auth.getSession returned no session name");
		}
		return name;
	}

	static String apiSig(Map<String, String> params, String secret) {
		StringBuilder sb = new StringBuilder();
		new TreeMap<>(params).forEach((k, v) -> sb.append(k).append(v));
		sb.append(secret);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : bytes) hex.append(String.format("%02x", b));
			return hex.toString();
		} catch (Exception e) {
			throw new IllegalStateException("MD5 failed", e);
		}
	}

	private static String fetch(String url) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.timeout(Duration.ofSeconds(5))
				.GET()
				.build();
		HttpResponse<String> resp = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
		if (resp.statusCode() != 200) {
			throw new IOException("auth.getSession HTTP " + resp.statusCode());
		}
		return resp.body();
	}

	private static String apiSecret() {
		String v = System.getenv("LASTFM_API_SECRET");
		if (v != null && !v.trim().isEmpty()) return v.trim();
		v = AppConfig.get("lastfm.api.secret");
		if (v != null && !v.trim().isEmpty()) return v.trim();
		throw new IllegalStateException("LASTFM_API_SECRET env var or lastfm.api.secret property is not set");
	}

	private static String enc(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}
}
