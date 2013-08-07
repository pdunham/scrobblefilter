package scrobblefilter.das.impl;

import java.util.List;

import scrobblefilter.das.UserFetcher;
import scrobblefilter.model.User;
import static com.googlecode.objectify.ObjectifyService.ofy;


public class CronUserFetcher implements UserFetcher {

	public List<User> fetchUsersForCronJob() {
			return ofy().load().type(User.class).filter("cron", true).list();
	}	
		
}
