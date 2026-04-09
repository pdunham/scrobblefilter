package scrobblefilter.das.impl;

import java.util.List;
import java.util.ArrayList;

import scrobblefilter.AppConfig;
import scrobblefilter.das.UserFetcher;
import scrobblefilter.model.User;

public class UserFetcherDefaultImpl implements UserFetcher {

	public List<User> fetchUsersForCronJob() {
		List<User> result = new ArrayList<User>();
		User user = User.findByName(AppConfig.get("dev.default.user"));
		if (user==null) return null;
		result.add(user);
		return result;
	}

}
