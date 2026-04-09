package scrobblefilter.das.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import scrobblefilter.das.UserFetcher;
import scrobblefilter.model.DatastoreProvider;
import scrobblefilter.model.User;

public class CronUserFetcher implements UserFetcher {

	public List<User> fetchUsersForCronJob() {
		Datastore ds = DatastoreProvider.get();
		Query<Entity> query = Query.newEntityQueryBuilder()
			.setKind("User")
			.setFilter(PropertyFilter.eq("cron", true))
			.build();
		QueryResults<Entity> results = ds.run(query);
		List<User> list = new ArrayList<>();
		while (results.hasNext()) {
			list.add(User.fromEntity(results.next()));
		}
		return list;
	}

}
