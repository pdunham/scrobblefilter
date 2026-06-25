package scrobblefilter.model;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
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

	// Bluesky (AT Protocol). did/handle are non-secret; the refresh token and
	// DPoP private key are stored encrypted (see CredentialCrypto) and verbatim
	// here — decryption happens in the poster, not the model.
	private String blueskyDid;
	private String blueskyHandle;
	private String blueskyRefreshTokenEnc;
	private String blueskyDpopKeyEnc;
	private boolean blueskyCron;

	public static User fromEntity(Entity e) {
		if (e == null) return null;
		User u = new User();
		u.lastfmName  = e.getKey().getName();
		String rawTwitter = e.contains("twitterName") ? e.getString("twitterName") : null;
		u.twitterName = (rawTwitter != null && !rawTwitter.isEmpty()) ? rawTwitter : null;
		String rawToken = e.contains("token") ? e.getString("token") : null;
		u.token       = (rawToken != null && !rawToken.isEmpty()) ? rawToken : null;
		String rawTokenSecret = e.contains("tokenSecret") ? e.getString("tokenSecret") : null;
		u.tokenSecret = (rawTokenSecret != null && !rawTokenSecret.isEmpty()) ? rawTokenSecret : null;
		u.preface     = e.contains("preface")     ? e.getString("preface")     : null;
		u.prefixText  = e.contains("prefixText")  ? e.getString("prefixText")  : null;
		u.useNumbers  = e.contains("useNumbers")  && e.getBoolean("useNumbers");
		u.isRandom    = e.contains("isRandom")    && e.getBoolean("isRandom");
		u.cron        = e.contains("cron")        && e.getBoolean("cron");
		String rawBskyDid = e.contains("blueskyDid") ? e.getString("blueskyDid") : null;
		u.blueskyDid = (rawBskyDid != null && !rawBskyDid.isEmpty()) ? rawBskyDid : null;
		String rawBskyHandle = e.contains("blueskyHandle") ? e.getString("blueskyHandle") : null;
		u.blueskyHandle = (rawBskyHandle != null && !rawBskyHandle.isEmpty()) ? rawBskyHandle : null;
		String rawBskyRefresh = e.contains("blueskyRefreshTokenEnc") ? e.getString("blueskyRefreshTokenEnc") : null;
		u.blueskyRefreshTokenEnc = (rawBskyRefresh != null && !rawBskyRefresh.isEmpty()) ? rawBskyRefresh : null;
		String rawBskyDpop = e.contains("blueskyDpopKeyEnc") ? e.getString("blueskyDpopKeyEnc") : null;
		u.blueskyDpopKeyEnc = (rawBskyDpop != null && !rawBskyDpop.isEmpty()) ? rawBskyDpop : null;
		u.blueskyCron = e.contains("blueskyCron") && e.getBoolean("blueskyCron");
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
			.set("blueskyDid",    blueskyDid    != null ? blueskyDid    : "")
			.set("blueskyHandle", blueskyHandle != null ? blueskyHandle : "")
			// Encrypted credential blobs can exceed Datastore's 1500-byte indexed
			// string limit and never need indexing — store them unindexed.
			.set("blueskyRefreshTokenEnc", StringValue.newBuilder(blueskyRefreshTokenEnc != null ? blueskyRefreshTokenEnc : "")
					.setExcludeFromIndexes(true).build())
			.set("blueskyDpopKeyEnc", StringValue.newBuilder(blueskyDpopKeyEnc != null ? blueskyDpopKeyEnc : "")
					.setExcludeFromIndexes(true).build())
			.set("blueskyCron", blueskyCron)
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

	public String getBlueskyDid() {
		return blueskyDid;
	}

	public void setBlueskyDid(String blueskyDid) {
		this.blueskyDid = blueskyDid;
	}

	public String getBlueskyHandle() {
		return blueskyHandle;
	}

	public void setBlueskyHandle(String blueskyHandle) {
		this.blueskyHandle = blueskyHandle;
	}

	public String getBlueskyRefreshTokenEnc() {
		return blueskyRefreshTokenEnc;
	}

	public void setBlueskyRefreshTokenEnc(String blueskyRefreshTokenEnc) {
		this.blueskyRefreshTokenEnc = blueskyRefreshTokenEnc;
	}

	public String getBlueskyDpopKeyEnc() {
		return blueskyDpopKeyEnc;
	}

	public void setBlueskyDpopKeyEnc(String blueskyDpopKeyEnc) {
		this.blueskyDpopKeyEnc = blueskyDpopKeyEnc;
	}

	public boolean isBlueskyCron() {
		return blueskyCron;
	}

	public void setBlueskyCron(boolean blueskyCron) {
		this.blueskyCron = blueskyCron;
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
