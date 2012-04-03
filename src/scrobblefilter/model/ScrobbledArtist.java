package scrobblefilter.model;

public class ScrobbledArtist {

	String name;
	int playCount;
	
	public ScrobbledArtist(String name, int playcount) {
		super();
		this.name = name;
		this.playCount = playcount;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPlayCount() {
		return playCount;
	}
	public void setPlayCount(int plays) {
		this.playCount = plays;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScrobbledArtist other = (ScrobbledArtist) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
