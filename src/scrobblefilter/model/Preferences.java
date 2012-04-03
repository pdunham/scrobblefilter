package scrobblefilter.model;

import java.util.ArrayList;
import java.util.List;

public class Preferences {

	List<String> filteredArtists = new ArrayList<String>();

	String userName;
	String twitterName;
	
	
	public String getTwitterName() {
		return twitterName;
	}

	public void setTwitterName(String twitterName) {
		this.twitterName = twitterName;
	}

	String artist;
	
	public String getLastfmName() {
		return userName;
	}

	public void setLastfmName(String userName) {
		this.userName = userName;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist1) {
		this.artist = artist1;
		addFilteredArtist(artist1);
	}

	public List<String> getFilteredArtists() {
		return filteredArtists;
	}

	public void setFilteredArtists(List<String> filteredArtists) {
		this.filteredArtists = filteredArtists;
	}
	
	public void addFilteredArtist(String artist) {
		filteredArtists.add(artist);
	}
	
	
	
}
