package scrobblefilter.das.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import scrobblefilter.das.UserFetcher;
import scrobblefilter.model.DatastoreProvider;
import scrobblefilter.model.User;

public class CronUserFetcher implements UserFetcher {

	/**
	 * Users opted into the weekly post on any platform: {@code cron} (Twitter) OR
	 * {@code blueskyCron}. Run as two equality queries and union by lastfmName (the
	 * key) — robust across Datastore versions / the emulator without relying on
	 * disjunction support. The per-platform poster re-checks its own opt-in.
	 */
	public List<User> fetchUsersForCronJob() {
		Datastore ds = DatastoreProvider.get();
		Map<String, User> byKey = new LinkedHashMap<>();
		collect(ds, "cron", byKey);
		collect(ds, "blueskyCron", byKey);
		return new ArrayList<>(byKey.values());
	}

	private static void collect(Datastore ds, String flag, Map<String, User> byKey) {
		Query<Entity> query = Query.newEntityQueryBuilder()
			.setKind("User")
			.setFilter(PropertyFilter.eq(flag, true))
			.build();
		QueryResults<Entity> results = ds.run(query);
		while (results.hasNext()) {
			Entity e = results.next();
			byKey.putIfAbsent(e.getKey().getName(), User.fromEntity(e));
		}
	}

}
