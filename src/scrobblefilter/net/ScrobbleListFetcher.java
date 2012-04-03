package scrobblefilter.net;


/**
 * @author pdunham
 *	interface for returning list of scrobbled artists
 */
public interface ScrobbleListFetcher {

	
	/**
	 * returns raw String of json data from last.fm
	 * @param userName TODO
	 * @return String
	 */
	String fetchList(String userName);
	
	
}
