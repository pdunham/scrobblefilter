package scrobblefilter.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import scrobblefilter.model.User;
import scrobblefilter.net.OAuth1Helper;

@Controller
public class TwitterSignInController {

	private static final Logger log = Logger.getLogger(TwitterSignInController.class.getName());
	private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";

	@RequestMapping(value="twittersignin", method=GET)
	public void twitterSignIn(HttpServletRequest request, HttpServletResponse response, User user)
			throws IOException, ServletException {
		if (request.getSession().getAttribute("user") == null) {
			log.warning("no user in session - trying to look one up");
			user = RegistrationController.findUser(user.getTwitterName());
			if (user == null) {
				log.warning("there is no user in the session for twitter signin");
			} else {
				request.getSession().setAttribute("user", user);
			}
		}
		try {
			Properties props = loadTwitterProperties();
			String consumerKey = props.getProperty("twitter4j.oauth.consumerKey");
			String consumerSecret = props.getProperty("twitter4j.oauth.consumerSecret");

			StringBuffer callbackURL = request.getRequestURL();
			// Cloud Run terminates SSL; honour X-Forwarded-Proto so the callback URL
			// uses https:// to match what is registered in the Twitter developer console.
			String forwardedProto = request.getHeader("X-Forwarded-Proto");
			if (forwardedProto != null && callbackURL.indexOf("http:") == 0) {
				callbackURL.replace(0, 4, forwardedProto);
			}
			int index = callbackURL.lastIndexOf("/");
			callbackURL.replace(index, callbackURL.length(), "").append("/callback");

			Map<String, String> requestToken = OAuth1Helper.getRequestToken(
					consumerKey, consumerSecret, callbackURL.toString());

			request.getSession().setAttribute("oauth_token", requestToken.get("oauth_token"));
			request.getSession().setAttribute("oauth_token_secret", requestToken.get("oauth_token_secret"));

			User sessionUser = (User) request.getSession().getAttribute("user");
			String expectedName = (sessionUser != null) ? sessionUser.getTwitterName() : user.getTwitterName();
			response.sendRedirect(AUTHORIZE_URL + "?oauth_token=" + requestToken.get("oauth_token")
					+ "&force_login=true&screen_name=" + expectedName);
		} catch (Exception e) {
			log.warning("Error getting request token: " + e.getMessage());
			throw new ServletException(e);
		}
	}

	@RequestMapping(value="callback", method=GET)
	public ModelAndView twitterSignInCallback(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		User user = (User) request.getSession().getAttribute("user");
		String requestToken = (String) request.getSession().getAttribute("oauth_token");
		String requestTokenSecret = (String) request.getSession().getAttribute("oauth_token_secret");
		String verifier = request.getParameter("oauth_verifier");
		try {
			Properties props = loadTwitterProperties();
			String consumerKey = props.getProperty("twitter4j.oauth.consumerKey");
			String consumerSecret = props.getProperty("twitter4j.oauth.consumerSecret");

			Map<String, String> accessToken = OAuth1Helper.getAccessToken(
					consumerKey, consumerSecret, requestToken, requestTokenSecret, verifier);

			request.getSession().removeAttribute("oauth_token");
			request.getSession().removeAttribute("oauth_token_secret");

			if (user != null) {
				String returnedName = accessToken.get("screen_name");
				if (returnedName != null && !returnedName.equalsIgnoreCase(user.getTwitterName())) {
					log.warning("OAuth account mismatch: expected @" + user.getTwitterName()
							+ " but got @" + returnedName);
					Map<String, Object> errorModel = new HashMap<>();
					errorModel.put("user", user);
					errorModel.put("authError", "Wrong Twitter account: you authorized @" + returnedName
							+ " but this profile belongs to @" + user.getTwitterName()
							+ ". Please try again and log in with the correct account.");
					return new ModelAndView("helloworld", "model", errorModel);
				}
				user.setToken(accessToken.get("oauth_token"));
				user.setTokenSecret(accessToken.get("oauth_token_secret"));
				user.save();
				request.getSession().setAttribute("user", user);
			} else {
				log.warning("problem - user is null in callback method");
			}
		} catch (Exception e) {
			log.warning("Error exchanging access token: " + e.getMessage());
			throw new ServletException(e);
		}
		return new ModelAndView("helloworld");
	}

	private Properties loadTwitterProperties() throws IOException {
		Properties props = new Properties();
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("twitter4j.properties")) {
			props.load(is);
		}
		return props;
	}
}
