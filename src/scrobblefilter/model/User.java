package scrobblefilter.model;

import javax.persistence.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

public class User implements Serializable {
	private static final long serialVersionUID = 6744250575418616689L;
	@Id
	 private String twitterName;	
	 private String token;
	 private String tokenSecret;
	 private String lastfmName;
	 private String preface;
	 private boolean useNumbers;
	 private boolean isRandom;
	 private boolean cron;
	 private String prefixText;
	 
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
	
	private static Objectify getService() {
	  return ObjectifyService.begin();
	 }

	 public static User findByName(String name){
		 Objectify service = getService();
		 return service.get(User.class, name);
	 }
 
	public void save(){
		 Objectify service = getService();
		 service.put(this);
	}

	public void addFilteredArtist(FilteredArtist artist) {
		artist.setOwner(this);
		Objectify service = getService();
		service.put(artist);
	}
	
	public void addRetweets(List<FilteredArtist> artists){
		 for(FilteredArtist artist: artists){
		  artist.setOwner(this);
		 }

		 Objectify service = getService();
		 service.put(artists);
	}
	
	public List<String> getFilteredArtistAsStrings() {
		ArrayList<String> artistStrings = new ArrayList<String>();
		for (FilteredArtist artist : listAllFilteredArtists()) {
			artistStrings.add(artist.getArtistName());
		}
		return artistStrings;
	}
	
	public List<FilteredArtist> listAllFilteredArtists(){
		 Objectify service = getService();
		 return service.query(FilteredArtist.class).filter("owner", this).list();
	}
}