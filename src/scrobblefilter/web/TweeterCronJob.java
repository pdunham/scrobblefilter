package scrobblefilter.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import scrobblefilter.model.User;
import scrobblefilter.net.ScrobbleTweeter;
import scrobblefilter.das.UserFetcher;
import twitter4j.TwitterException;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.web.bind.annotation.RequestMethod.*;


@Controller
public class TweeterCronJob {

	@Autowired 
	UserFetcher userFetcher;
	
	ScrobbleTweeter tweeter = new ScrobbleTweeter();
	
	private static final Logger log = Logger.getLogger(TweeterCronJob.class.getName());

	
	
	@RequestMapping(value="cron/sendalltweets", method = GET)
	public void sendAllTweets(HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (!AdminAuth.valid(req, "CRON_TOKEN", "X-Cron-Token")) {
			res.sendError(HttpServletResponse.SC_FORBIDDEN, "forbidden");
			return;
		}
		List<User> users = userFetcher.fetchUsersForCronJob();
		log.info("fetched " + users.size() + " users");
		if (users ==null) return;
		for (User u : users) {
			try {
				tweeter.doTweet(u);
				// Log both the platform handle and the lastfmName. lastfmName is the
				// entity key (always present) and stays stable as we add per-platform
				// posting (bluesky, fb, ...); twitterName may be null for legacy users
				// whose Twitter screen name was never captured.
				log.info("posted on twitter as " + u.getTwitterName() + " for " + u.getLastfmName());
			} catch (TwitterException e) {
				log.warning(e.getMessage());
			}
		}
	}

	@RequestMapping(value="cron/listallcronmembers", method = GET)
	public ModelAndView listAllCronUSers(HttpServletRequest req, HttpServletResponse res, Map<String, Object> model) throws IOException {
		if (!AdminAuth.valid(req, "CRON_TOKEN", "X-Cron-Token")) {
			res.sendError(HttpServletResponse.SC_FORBIDDEN, "forbidden");
			return null;
		}
		List<User> users = userFetcher.fetchUsersForCronJob();
		model.put("users", users);
		return new ModelAndView("cron/sendalltweets","model",model);
	}
}
