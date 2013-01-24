package scrobblefilter.das.impl;

import java.util.List;
import java.util.ArrayList;

import scrobblefilter.das.UserFetcher;
import scrobblefilter.model.User;

public class UserFetcherDefaultImpl implements UserFetcher {

	@Override
	public List<User> fetchUsersForCronJob() {
		List<User> result = new ArrayList<User>();
		User user = User.findByName("pervprogrammer");
		if (user==null) return null;
		result.add(user);
		return result;
	}

}
