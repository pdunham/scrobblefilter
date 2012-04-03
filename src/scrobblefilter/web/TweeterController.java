package scrobblefilter.web;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.RequestMapping;

import scrobblefilter.model.ScrobbledArtist;
import scrobblefilter.model.User;
import scrobblefilter.net.ScrobbleListFetcher;
import scrobblefilter.net.ScrobbleListParser;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Controller
public class TweeterController {

	@Autowired
	ScrobbleListFetcher scrobbleFetcher;
	private static final Logger log = Logger.getLogger(TweeterController.class.getName());

	
	@RequestMapping(value="tweet", method=GET)
	public void tweetFilteredList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
        User user = (User)request.getSession().getAttribute("user");
        if (user==null) {
        	response.sendRedirect("welcome");
        	return;
        }
        //wrap the whole next section in a try/catch block and put an error in the session when you do the response.sendRedirect if anything fails
        try {
	        List<ScrobbledArtist> scrobbles = extractFilteredList(user.getLastfmName(), user.getFilteredArtistAsStrings());
	        String text = constructTweet(scrobbles);	        
	        AccessToken token = new AccessToken(user.getToken(), user.getTokenSecret());
        	Twitter twitter = new TwitterFactory().getInstance(token);
            twitter.updateStatus(text);
        } catch (Exception e) {
        	request.getSession().setAttribute("error", e);
        }
        response.sendRedirect("filter");
        
	}
	
	private List<ScrobbledArtist> extractFilteredList(String lastfmName,
			List<String> filteredArtists) {
		if (lastfmName==null) log.warning("in extractFilteredList lastfmname is null");
		if (filteredArtists == null ) log.warning("in extractFilteredList filteredArtist is null");
		List<ScrobbledArtist> artists = ScrobbleListParser.parseList( scrobbleFetcher.fetchList(lastfmName) );
		for (String filtered : filteredArtists) {
			artists.remove( new ScrobbledArtist(filtered, 0) );
		}
		return artists;
	}
	
	private String constructTweet(List<ScrobbledArtist> scrobbles) {
		String result = "The past week I've listened to";
		for (int i = 0; i < 3; i++) {
			ScrobbledArtist scrobble = scrobbles.get(i);
			result = result + " " + scrobble.getName() + " (" + scrobble.getPlayCount() + ")";
		}
		return result;
	}
	
	

}
