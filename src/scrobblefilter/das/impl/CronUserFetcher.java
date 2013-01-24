package scrobblefilter.das.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.DatastoreService;

import scrobblefilter.das.UserFetcher;
import scrobblefilter.model.User;

public class CronUserFetcher implements UserFetcher {

	private static final Logger log = Logger.getLogger(CronUserFetcher.class.getName());

	@Override
	public List<User> fetchUsersForCronJob() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter isCronFilter = new FilterPredicate("cron", Query.FilterOperator.EQUAL, true);
		Query cronQuery = new Query("User");
		cronQuery.setFilter(isCronFilter);
		cronQuery.setKeysOnly();
		PreparedQuery pq = datastore.prepare(cronQuery);
		List<User> users = new ArrayList<User>();
		for (Entity result: pq.asIterable()) {
			log.info(result.getKey().getName());
			User u = User.findByName( result.getKey().getName());
			if (u.getLastfmName()!=null && u.getToken()!=null) {
				users.add(u);
			}
		}
		return users;
	}

}
