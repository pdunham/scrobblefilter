package scrobblefilter.model;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

public class FilteredArtist {

	private static final String KIND = "FilteredArtist";

	String id;
	String lastfmName;
	String artistName;
	String owner;
	// Legacy: only populated when reading old-format entities during migration
	String twitterName;

	public FilteredArtist() {
		super();
	}

	public FilteredArtist(String lastfmName, String artistName) {
		super();
		this.id = lastfmName + ":" + artistName;
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

	public String getOwner() {
		return owner;
	}

	public User getOwnerUser() {
		return User.findByLastfmName(owner);
	}

	public void setOwner(User owner) {
		this.owner = owner.getLastfmName();
	}

}
