package scrobblefilter.web;

import java.util.List;
import java.util.logging.Logger;

import scrobblefilter.model.User;
import scrobblefilter.net.ScrobbleTweeter;
import scrobblefilter.das.UserFetcher;
import twitter4j.TwitterException;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;


@Controller
public class TweeterCronJob {

	@Autowired 
	UserFetcher userFetcher;
	
	ScrobbleTweeter tweeter = new ScrobbleTweeter();
	
	private static final Logger log = Logger.getLogger(TweeterCronJob.class.getName());

	
	
	@RequestMapping(value="cron/sendalltweets", method = GET)
	public void sendAllTweets() {
		
		List<User> users = userFetcher.fetchUsersForCronJob();
		if (users ==null) return;
		for (User u : users) {
			try {
				tweeter.doTweet(u);
			} catch (TwitterException e) {
				log.warning(e.getErrorMessage());
			}
		}
		
	}
	
}
