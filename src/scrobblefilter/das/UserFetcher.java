package scrobblefilter.das;

import scrobblefilter.model.User;
import java.util.List;

public interface UserFetcher {

	List<User> fetchUsersForCronJob();
	
}
