package scrobblefilter.model;

import javax.persistence.Id;
import com.googlecode.objectify.Key;

public class FilteredArtist {

	@Id
	String id;
	String twitterName;
	String lastfmName;
	String artistName;
	
	private Key<User> owner;
	
	public FilteredArtist() {
		super();
	}
	
	public FilteredArtist(String twitterName, String lastfmName, String artistName) {
		super();
		this.id = twitterName +":" + lastfmName+":"+artistName;
		this.twitterName = twitterName;
		this.lastfmName = lastfmName;
		this.artistName = artistName;
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
	
	public void setOwner(User owner) {
		this.owner = new Key<User>(User.class, owner.getName());
	}

	public Key<User> getOwner() {
		return owner;
	}

	public void setOwner(Key<User> owner) {
		this.owner = owner;
	}
	
}
