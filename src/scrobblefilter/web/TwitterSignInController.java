package scrobblefilter.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import scrobblefilter.model.User;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;


@Controller
public class TwitterSignInController {

	private static final Logger log = Logger.getLogger(TwitterSignInController.class.getName());
	
	@RequestMapping(value="twittersignin")
	public void twitterSignIn(HttpServletRequest request, HttpServletResponse response, User user) throws IOException, ServletException {
		if (request.getSession().getAttribute("user")==null) {
			log.warning("no user in session - trying to look one up");
			User foundUser = RegistrationController.findUser(user.getTwitterName());
			if (foundUser==null) {
				log.warning("there is no user in the session for twitter signin");
			} else {
				user = foundUser;
			}
			request.getSession().setAttribute("user", user);
		}
		Twitter twitter = new TwitterFactory().getInstance();
        request.getSession().setAttribute("twitter", twitter);
        try {
            StringBuffer callbackURL = request.getRequestURL();
            int index = callbackURL.lastIndexOf("/");
            callbackURL.replace(index, callbackURL.length(), "").append("/callback");

            RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL.toString());
            request.getSession().setAttribute("requestToken", requestToken);
            log.fine("request url is "+requestToken.getAuthenticationURL());
            response.sendRedirect(requestToken.getAuthenticationURL());

        } catch (TwitterException e) {
            throw new ServletException(e);
        }		
	}
	
	@RequestMapping(value="callback", method=GET)
	public ModelAndView twitterSignInCallback(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
		User user = (User)request.getSession().getAttribute("user");
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        String verifier = request.getParameter("oauth_verifier");
        try {
            twitter.getOAuthAccessToken(requestToken, verifier);
            request.getSession().removeAttribute("requestToken");
    		if (user!=null) {
				user.setToken(twitter.getOAuthAccessToken().getToken());
				user.setTokenSecret(twitter.getOAuthAccessToken().getTokenSecret());
				user.save();
    		} else {
    			log.warning("problem - user is null in callback method");
    		}
        } catch (TwitterException e) {
            throw new ServletException(e);
        }
        
		return new ModelAndView("helloworld");
	}
}
