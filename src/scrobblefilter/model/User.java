package scrobblefilter.model;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import java.util.ArrayList;
import java.util.List;

public class User {

	private static final String KIND = "User";

	String twitterName;
	private String token;
	private String tokenSecret;
	private String lastfmName;
	private String preface;
	private boolean useNumbers;
	private boolean isRandom;
	private boolean cron;
	private String prefixText;

	public static User fromEntity(Entity e) {
		if (e == null) return null;
		User u = new User();
		u.lastfmName  = e.getKey().getName();
		String rawTwitter = e.contains("twitterName") ? e.getString("twitterName") : null;
		u.twitterName = (rawTwitter != null && !rawTwitter.isEmpty()) ? rawTwitter : null;
		u.token       = e.contains("token")       ? e.getString("token")       : null;
		u.tokenSecret = e.contains("tokenSecret") ? e.getString("tokenSecret") : null;
		u.preface     = e.contains("preface")     ? e.getString("preface")     : null;
		u.prefixText  = e.contains("prefixText")  ? e.getString("prefixText")  : null;
		u.useNumbers  = e.contains("useNumbers")  && e.getBoolean("useNumbers");
		u.isRandom    = e.contains("isRandom")    && e.getBoolean("isRandom");
		u.cron        = e.contains("cron")        && e.getBoolean("cron");
		return u;
	}

	private Entity toEntity() {
		Datastore ds = DatastoreProvider.get();
		Key key = ds.newKeyFactory().setKind(KIND).newKey(lastfmName);
		return Entity.newBuilder(key)
			.set("twitterName", twitterName != null ? twitterName : "")
			.set("token",       token       != null ? token       : "")
			.set("tokenSecret", tokenSecret != null ? tokenSecret : "")
			.set("lastfmName",  lastfmName  != null ? lastfmName  : "")
			.set("preface",     preface     != null ? preface     : "")
			.set("prefixText",  prefixText  != null ? prefixText  : "")
			.set("useNumbers",  useNumbers)
			.set("isRandom",    isRandom)
			.set("cron",        cron)
			.build();
	}

	public static User findByLastfmName(String lastfmName) {
		if (lastfmName == null || lastfmName.isEmpty()) return null;
		Datastore ds = DatastoreProvider.get();
		Key key = ds.newKeyFactory().setKind(KIND).newKey(lastfmName);
		return fromEntity(ds.get(key));
	}

	public void save() {
		if (lastfmName == null || lastfmName.isEmpty()) {
			throw new IllegalStateException("Cannot save User without a lastfmName (it is the primary key)");
		}
		DatastoreProvider.get().put(toEntity());
	}

	public void addFilteredArtist(FilteredArtist artist) {
		artist.setOwner(this);
		DatastoreProvider.get().put(artist.toEntity());
	}

	public void addRetweets(List<FilteredArtist> artists) {
		for (FilteredArtist artist : artists) {
			artist.setOwner(this);
		}
		Entity[] entityArray = artists.stream()
			.map(FilteredArtist::toEntity)
			.toArray(Entity[]::new);
		DatastoreProvider.get().put(entityArray);
	}

	public List<String> getFilteredArtistAsStrings() {
		ArrayList<String> artistStrings = new ArrayList<String>();
		for (FilteredArtist artist : listAllFilteredArtists()) {
			artistStrings.add(artist.getArtistName());
		}
		return artistStrings;
	}

	public List<FilteredArtist> listAllFilteredArtists() {
		Datastore ds = DatastoreProvider.get();
		Query<Entity> query = Query.newEntityQueryBuilder()
			.setKind("FilteredArtist")
			.setFilter(PropertyFilter.eq("owner", this.lastfmName))
			.build();
		QueryResults<Entity> results = ds.run(query);
		List<FilteredArtist> list = new ArrayList<>();
		while (results.hasNext()) {
			list.add(FilteredArtist.fromEntity(results.next()));
		}
		return list;
	}

	public String getPrefixText() {
		return prefixText;
	}

	public void setPrefixText(String prefixText) {
		this.prefixText = prefixText;
	}

	public boolean isCron() {
		return cron;
	}

	public void setCron(boolean cron) {
		this.cron = cron;
	}

	public String getPreface() {
		return preface;
	}

	public void setPreface(String preface) {
		this.preface = preface;
	}

	public boolean isUseNumbers() {
		return useNumbers;
	}

	public void setUseNumbers(boolean useNumbers) {
		this.useNumbers = useNumbers;
	}

	public boolean isRandom() {
		return isRandom;
	}

	public void setRandom(boolean isRandom) {
		this.isRandom = isRandom;
	}

	public String getLastfmName() {
		return lastfmName;
	}

	public void setLastfmName(String lastfmName) {
		this.lastfmName = (lastfmName == null || lastfmName.isEmpty()) ? null : lastfmName;
	}

	public User() {
		super();
	}

	public User(String twitterName, String token, String tokenSecret) {
		super();
		this.twitterName = twitterName;
		this.token = token;
		this.tokenSecret = tokenSecret;
	}

	public String getTwitterName() {
		return twitterName;
	}

	public String getName() {
		return twitterName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public void setTwitterName(String name) {
		this.twitterName = name;
	}

	public void setName(String name) {
		this.twitterName = name;
	}
}
