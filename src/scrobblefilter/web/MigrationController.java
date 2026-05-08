package scrobblefilter.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import scrobblefilter.model.DatastoreProvider;

@Controller
public class MigrationController {

	private static final Logger log = Logger.getLogger(MigrationController.class.getName());

	@RequestMapping(value = "admin/migrate", method = GET)
	public ModelAndView migrate(HttpServletRequest req, HttpServletResponse res, Map<String, Object> model) throws IOException {
		if (!AdminAuth.valid(req, "MIGRATE_TOKEN", "X-Admin-Token")) {
			res.sendError(HttpServletResponse.SC_FORBIDDEN, "forbidden");
			return null;
		}
		Datastore ds = DatastoreProvider.get();
		List<String> migrated = new ArrayList<>();
		List<String> merged = new ArrayList<>();
		List<String> orphansDeleted = new ArrayList<>();
		int alreadyInNewFormat = 0;
		int migratedArtistCount = 0;
		int mergedArtistCount = 0;
		int deletedArtistCount = 0;

		Query<Entity> userQuery = Query.newEntityQueryBuilder().setKind("User").build();
		QueryResults<Entity> users = ds.run(userQuery);

		while (users.hasNext()) {
			Entity oldUser = users.next();
			String oldKeyName = oldUser.getKey().getName();
			String storedLastfm = oldUser.contains("lastfmName") ? oldUser.getString("lastfmName") : null;

			// Already in new format: key matches lastfmName property — leave alone.
			if (storedLastfm != null && !storedLastfm.isEmpty() && oldKeyName.equals(storedLastfm)) {
				alreadyInNewFormat++;
				continue;
			}

			// No lastfmName: cannot be migrated. Delete the user and any artists they own.
			if (storedLastfm == null || storedLastfm.isEmpty()) {
				int n = deleteFilteredArtistsForOwner(ds, oldKeyName);
				deletedArtistCount += n;
				ds.delete(oldUser.getKey());
				String summary = "@" + oldKeyName + " (no lastfmName) — deleted user and " + n + " artists";
				log.info("migrate: " + summary);
				orphansDeleted.add(summary);
				continue;
			}

			Key newKey = ds.newKeyFactory().setKind("User").newKey(storedLastfm);
			Entity existingTarget = ds.get(newKey);

			if (existingTarget != null) {
				// Target lastfmName user already exists — merge this user's artists into them
				// and delete the duplicate. Existing target's properties win; we don't overwrite them.
				int n = migrateFilteredArtists(ds, oldKeyName, storedLastfm);
				mergedArtistCount += n;
				ds.delete(oldUser.getKey());
				String summary = "@" + oldKeyName + " → " + storedLastfm + " — merged " + n + " artists into existing user";
				log.info("migrate: " + summary);
				merged.add(summary);
				continue;
			}

			// Fresh migration: create new user keyed by lastfmName, copy properties, migrate artists.
			// Note: Objectify-era entities can have properties set to null (not just absent),
			// so coerce every string getter through str() to avoid Datastore.set rejecting null.
			Entity newUser = Entity.newBuilder(newKey)
				.set("twitterName", oldKeyName != null ? oldKeyName : "")
				.set("lastfmName",  storedLastfm)
				.set("token",       str(oldUser, "token"))
				.set("tokenSecret", str(oldUser, "tokenSecret"))
				.set("preface",     str(oldUser, "preface"))
				.set("prefixText",  str(oldUser, "prefixText"))
				.set("useNumbers",  oldUser.contains("useNumbers")  && oldUser.getBoolean("useNumbers"))
				.set("isRandom",    oldUser.contains("isRandom")    && oldUser.getBoolean("isRandom"))
				.set("cron",        oldUser.contains("cron")        && oldUser.getBoolean("cron"))
				.build();
			ds.put(newUser);

			int n = migrateFilteredArtists(ds, oldKeyName, storedLastfm);
			migratedArtistCount += n;
			ds.delete(oldUser.getKey());

			String summary = "@" + oldKeyName + " → " + storedLastfm + " — migrated user and " + n + " artists";
			log.info("migrate: " + summary);
			migrated.add(summary);
		}

		model.put("migrated", migrated);
		model.put("merged", merged);
		model.put("orphansDeleted", orphansDeleted);
		model.put("migratedCount", migrated.size());
		model.put("mergedCount", merged.size());
		model.put("orphansDeletedCount", orphansDeleted.size());
		model.put("alreadyInNewFormatCount", alreadyInNewFormat);
		model.put("migratedArtistCount", migratedArtistCount);
		model.put("mergedArtistCount", mergedArtistCount);
		model.put("deletedArtistCount", deletedArtistCount);
		return new ModelAndView("admin/migrate", "model", model);
	}

	private static String str(Entity e, String prop) {
		if (!e.contains(prop)) return "";
		String v = e.getString(prop);
		return v != null ? v : "";
	}

	private int deleteFilteredArtistsForOwner(Datastore ds, String owner) {
		Query<Entity> artistQuery = Query.newEntityQueryBuilder()
			.setKind("FilteredArtist")
			.setFilter(PropertyFilter.eq("owner", owner))
			.build();
		QueryResults<Entity> artists = ds.run(artistQuery);
		int count = 0;
		while (artists.hasNext()) {
			ds.delete(artists.next().getKey());
			count++;
		}
		return count;
	}

	private int migrateFilteredArtists(Datastore ds, String oldOwner, String newOwner) {
		Query<Entity> artistQuery = Query.newEntityQueryBuilder()
			.setKind("FilteredArtist")
			.setFilter(PropertyFilter.eq("owner", oldOwner))
			.build();
		QueryResults<Entity> artists = ds.run(artistQuery);
		int count = 0;
		while (artists.hasNext()) {
			Entity oldArtist = artists.next();
			String artistName = oldArtist.contains("artistName") ? oldArtist.getString("artistName") : null;
			if (artistName == null || artistName.isEmpty()) continue;

			String newId = newOwner + ":" + artistName;
			Key newArtistKey = ds.newKeyFactory().setKind("FilteredArtist").newKey(newId);
			Entity newArtist = Entity.newBuilder(newArtistKey)
				.set("lastfmName", newOwner)
				.set("artistName", artistName)
				.set("owner",      newOwner)
				.build();
			ds.put(newArtist);
			ds.delete(oldArtist.getKey());
			count++;
		}
		return count;
	}
}
