package scrobblefilter.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;


@Entity
public class FilteredArtist {

	@Id
	String id;
	String twitterName;
	String lastfmName;
	String artistName;
	
	@Index String owner;
	
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
	
	public User getOwner() {
		return User.findByName(owner);
	}

	public void setOwner(User owner) {
		this.owner = owner.twitterName;
	}
	
}
