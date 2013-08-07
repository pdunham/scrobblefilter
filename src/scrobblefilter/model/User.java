package scrobblefilter.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Entity
public class User implements Serializable {

	@Id 
	 String twitterName;	
	 private String token;
	 private String tokenSecret;
	 private String lastfmName;
	 private String preface;
	 private boolean useNumbers;
	 @Index private boolean isRandom;
	 @Index private boolean cron;
	 private String prefixText;
	 private static final long serialVersionUID = 6744250575418616690L;
			 
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
		this.lastfmName = lastfmName;
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
	

	 public static User findByName(String name){
		 return ofy().load().type(User.class).id(name).now();
	 }
 
	public void save(){
		ofy().save().entity(this).now();
	}

	public void addFilteredArtist(FilteredArtist artist) {
		artist.setOwner(this);
		ofy().save().entity(artist).now();
	}
	
	public void addRetweets(List<FilteredArtist> artists){
		 for(FilteredArtist artist: artists){
		  artist.setOwner(this);
		 }

		 ofy().save().entities(artists);
	}
	
	public List<String> getFilteredArtistAsStrings() {
		ArrayList<String> artistStrings = new ArrayList<String>();
		for (FilteredArtist artist : listAllFilteredArtists()) {
			artistStrings.add(artist.getArtistName());
		}
		return artistStrings;
	}
	
	public List<FilteredArtist> listAllFilteredArtists(){
		return ofy().load().type(FilteredArtist.class).filter("owner", this.twitterName).list();		
	}
}