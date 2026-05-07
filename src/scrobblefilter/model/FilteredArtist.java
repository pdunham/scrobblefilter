package scrobblefilter.model;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

public class FilteredArtist {

	private static final String KIND = "FilteredArtist";

	String id;
	String twitterName;
	String lastfmName;
	String artistName;
	String owner;

	public FilteredArtist() {
		super();
	}

	public FilteredArtist(String twitterName, String lastfmName, String artistName) {
		super();
		this.id = twitterName + ":" + lastfmName + ":" + artistName;
		this.twitterName = twitterName;
		this.lastfmName = lastfmName;
		this.artistName = artistName;
	}

	static FilteredArtist fromEntity(Entity e) {
		if (e == null) return null;
		FilteredArtist fa = new FilteredArtist();
		fa.id          = e.getKey().getName();
		fa.twitterName = e.contains("twitterName") ? e.getString("twitterName") : null;
		fa.lastfmName  = e.contains("lastfmName")  ? e.getString("lastfmName")  : null;
		fa.artistName  = e.contains("artistName")  ? e.getString("artistName")  : null;
		fa.owner       = e.contains("owner")       ? e.getString("owner")       : null;
		return fa;
	}

	Entity toEntity() {
		Datastore ds = DatastoreProvider.get();
		Key key = ds.newKeyFactory().setKind(KIND).newKey(id);
		return Entity.newBuilder(key)
			.set("twitterName", twitterName != null ? twitterName : "")
			.set("lastfmName",  lastfmName  != null ? lastfmName  : "")
			.set("artistName",  artistName  != null ? artistName  : "")
			.set("owner",       owner       != null ? owner       : "")
			.build();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTwitterName() {
		return twitterName;
	}

	public void setTwitterName(String twitterName) {
		this.twitterName = twitterName;
	}

	public String getLastfmName() {
		return lastfmName;
	}

	public void setLastfmName(String lastfmName) {
		this.lastfmName = lastfmName;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public User getOwner() {
		return User.findByName(owner);
	}

	public void setOwner(User owner) {
		this.owner = owner.twitterName;
	}

}
