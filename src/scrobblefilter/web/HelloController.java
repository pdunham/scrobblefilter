package scrobblefilter.web;

import java.util.Map;
import java.util.List;

import scrobblefilter.model.Preferences;
import scrobblefilter.model.User;
import scrobblefilter.net.ScrobbleListFetcher;
import scrobblefilter.net.ScrobbleListParser;
import scrobblefilter.model.ScrobbledArtist;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.validation.BindingResult;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class HelloController {

	@Autowired
	ScrobbleListFetcher scrobbleFetcher;
	
	@RequestMapping(value="world", method = GET)
	public ModelAndView helloWorld(HttpServletRequest request, HttpServletResponse response) 
	{
		return new ModelAndView("helloworld");
	}
	
	@RequestMapping(value="filter", method=POST)
	public ModelAndView retrieveFilteredList(HttpServletRequest request, HttpServletResponse response, Preferences prefs, BindingResult result, Map<String, Object> model)
	{
		
		model.put("prefs", prefs);
		String lastfmName = prefs.getLastfmName();
		List<String> filteredArtists = prefs.getFilteredArtists();
		List<ScrobbledArtist> artists = extractFilteredList(lastfmName,
				filteredArtists);
		model.put("list", artists);
		return new ModelAndView("filteredlist", "model", model);
		
		
	}

	private List<ScrobbledArtist> extractFilteredList(String lastfmName,
			List<String> filteredArtists) {
		List<ScrobbledArtist> artists = ScrobbleListParser.parseList( scrobbleFetcher.fetchList(lastfmName) );
		for (String filtered : filteredArtists) {
			artists.remove( new ScrobbledArtist(filtered, 0) );
		}
		return artists;
	}
	
	String getUnfilteredList() {
		
		return scrobbleFetcher.fetchList(null);
		
	}
	
	@RequestMapping(value="filter", method=GET)
	public ModelAndView retrieveFilteredList(HttpServletRequest request, HttpServletResponse response, User user, BindingResult result, Map<String, Object> model) 
	{
		if (user.getName()==null) user = (User)request.getSession().getAttribute("user");
		user = RegistrationController.findUser(user.getTwitterName());
		List<String> filteredArtistsAsStrings = user.getFilteredArtistAsStrings();
		Preferences prefs = new Preferences();
		prefs.setTwitterName(user.getTwitterName());
		prefs.setLastfmName(user.getLastfmName());
		prefs.setFilteredArtists(filteredArtistsAsStrings);
		model.put("prefs", prefs);
		model.put("list", extractFilteredList(user.getLastfmName(), filteredArtistsAsStrings));
		checkForErrors(request, model);
		return new ModelAndView("filteredlist", "model", model);
	}
	
	private void checkForErrors(HttpServletRequest request, Map<String, Object> model) {
		Object error = request.getSession().getAttribute("error");
		if (error!=null) {
			request.getSession().removeAttribute("error");
			model.put("error","I'm afraid I can't do that, Dave.");
		}
		
	}
}
