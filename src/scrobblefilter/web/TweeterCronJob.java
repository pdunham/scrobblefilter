package scrobblefilter.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import scrobblefilter.model.User;
import scrobblefilter.net.SocialPoster;
import scrobblefilter.net.SocialPostException;
import scrobblefilter.net.StatusComposer;
import scrobblefilter.das.UserFetcher;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.web.bind.annotation.RequestMethod.*;


@Controller
public class TweeterCronJob {

	@Autowired
	UserFetcher userFetcher;

	@Autowired
	List<SocialPoster> posters;

	@Autowired
	StatusComposer composer;

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
			String statusText;
			try {
				statusText = composer.compose(u);
			} catch (Exception e) {
				// One user's compose failure (e.g. <3 artists, Last.fm hiccup) must
				// not abort the whole batch.
				log.warning("could not compose status for " + u.getLastfmName() + ": " + e.getMessage());
				continue;
			}
			for (SocialPoster poster : posters) {
				if (!poster.isEnabledFor(u)) continue;
				try {
					poster.post(u, statusText);
					// lastfmName is the entity key (always present); twitterName may be
					// null for legacy users whose screen name was never captured.
					log.info("posted on " + poster.platform() + " as "
							+ (u.getTwitterName() != null ? u.getTwitterName() : "?")
							+ " for " + u.getLastfmName());
				} catch (SocialPostException e) {
					log.warning(poster.platform() + " post failed for " + u.getLastfmName() + ": " + e.getMessage());
				}
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
