package scrobblefilter.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import scrobblefilter.model.FilteredArtist;
import scrobblefilter.model.Preferences;
import scrobblefilter.model.User;

@Controller
public class RegistrationController {
	
	private static final Logger log = Logger.getLogger(RegistrationController.class.getName());


	@RequestMapping(value="welcome", method = GET)
	public ModelAndView helloWorld(HttpServletRequest request, HttpServletResponse response) 
	{
		return new ModelAndView("newuser");
	}
	
	@RequestMapping(value="register", method=POST)
	public ModelAndView welcomeUser(HttpServletRequest request, HttpServletResponse response, User user, BindingResult result, Map<String, Object> model)
	{
		user = findOrCreateUser(user);
		request.getSession().setAttribute("user", user);
		model.put("user", user);
		return new ModelAndView("helloworld", "model", model);
		
	}

	protected static User findOrCreateUser(User user) {
		User maybeUser = null;
		try {
			maybeUser = User.findByName(user.getTwitterName());
		} catch (NotFoundException nfe) {
			log.fine("could not find "+user.getTwitterName());	
		}
		if (maybeUser==null) {
			user.save();
			log.fine("saved "+user.getTwitterName());
		} else {
			user = maybeUser;
			log.fine("found "+user.getTwitterName());
		}
		return user;
	}
	
	protected static User findUser(String twitterName) {
		
		User user = null;
		try {
			user = User.findByName(twitterName);
		} catch (NotFoundException e) {
			System.out.println("could not find "+twitterName);
		}
		return user;
		
	}
	
	@RequestMapping(value="updateLastfmName", method=POST)
	public ModelAndView updateLastFm(HttpServletRequest request, HttpServletResponse response, User user, BindingResult result, Map<String, Object> model)
	{
		User datastoreuser = findOrCreateUser(user);
		datastoreuser.setLastfmName(user.getLastfmName());
		datastoreuser.save();
		model.put("user", datastoreuser);
		return new ModelAndView("helloworld", "model", model);
	}
	
	@RequestMapping(value="addartist", method={POST,GET})
	public ModelAndView addArtistToFilter(HttpServletRequest request, HttpServletResponse response, Preferences prefs, BindingResult result, Map<String, Object> model)
	{
		User user = findUser(prefs.getTwitterName());
		if (user==null) return new ModelAndView("newuser");
		FilteredArtist artist = new FilteredArtist(prefs.getTwitterName(), prefs.getLastfmName(),prefs.getArtist());
		user.addFilteredArtist(artist);
		model.put("user",user);
		return new ModelAndView("helloworld", "model", model);
	}
	
	@RequestMapping(value="removeartist", method=GET)
	public ModelAndView removeArtistFromFilter(HttpServletRequest request, HttpServletResponse response, FilteredArtist artist, BindingResult result, Map<String, Object> model)
	{
		User user = findUser(artist.getTwitterName());
		Objectify ofy = ObjectifyService.begin();
		ofy.delete(artist);
		model.put("user",user);
		return new ModelAndView("helloworld", "model", model);
	}
}
