package scrobblefilter.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Key;

import scrobblefilter.model.DatastoreProvider;
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

	@RequestMapping(value="logout", method = GET)
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response)
	{
		request.getSession().invalidate();
		return new ModelAndView("redirect:/hello/welcome");
	}

	@RequestMapping(value="register", method=POST)
	public ModelAndView welcomeUser(HttpServletRequest request, HttpServletResponse response, User user, BindingResult result, Map<String, Object> model)
	{
		if (user.getLastfmName() == null || user.getLastfmName().isEmpty()) {
			model.put("error", "A Last.fm username is required to register.");
			return new ModelAndView("newuser", "model", model);
		}
		user = findOrCreateUser(user);
		if (user == null) {
			model.put("error", "A Last.fm username is required to register.");
			return new ModelAndView("newuser", "model", model);
		}
		request.getSession().setAttribute("user", user);
		model.put("user", user);
		return new ModelAndView("helloworld", "model", model);
	}

	@RequestMapping(value="updateCronSetting", method=POST)
	public ModelAndView updateCronSetting(HttpServletRequest request, HttpServletResponse response, User user, BindingResult result, Map<String, Object> model)
	{
		User sessionUser = (User) request.getSession().getAttribute("user");
		if (sessionUser == null) return new ModelAndView("redirect:/hello/welcome");
		User foundUser = findUser(sessionUser.getLastfmName());
		if (foundUser == null) return new ModelAndView("redirect:/hello/welcome");
		foundUser.setCron(user.isCron());
		foundUser.save();
		request.getSession().setAttribute("user", foundUser);
		model.put("user", foundUser);
		return new ModelAndView("helloworld", "model", model);
	}

	protected static User findOrCreateUser(User user) {
		if (user.getLastfmName() == null || user.getLastfmName().isEmpty()) {
			return null;
		}
		User maybeUser = User.findByLastfmName(user.getLastfmName());
		if (maybeUser == null) {
			user.save();
			log.fine("saved " + user.getLastfmName());
		} else {
			user = maybeUser;
			log.fine("found " + user.getLastfmName());
		}
		return user;
	}

	protected static User findUser(String lastfmName) {
		return User.findByLastfmName(lastfmName);
	}

@RequestMapping(value="addartist", method={POST,GET})
	public ModelAndView addArtistToFilter(HttpServletRequest request, HttpServletResponse response, Preferences prefs, BindingResult result, Map<String, Object> model)
	{
		User user = findUser(prefs.getLastfmName());
		if (user == null) return new ModelAndView("newuser");
		FilteredArtist artist = new FilteredArtist(user.getLastfmName(), prefs.getArtist());
		user.addFilteredArtist(artist);
		model.put("user", user);
		return new ModelAndView("helloworld", "model", model);
	}

	@RequestMapping(value="removeartist", method=GET)
	public ModelAndView removeArtistFromFilter(HttpServletRequest request, HttpServletResponse response, FilteredArtist artist, BindingResult result, Map<String, Object> model)
	{
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) return new ModelAndView("redirect:/hello/welcome");
		List<FilteredArtist> remaining = user.listAllFilteredArtists().stream()
			.filter(a -> !artist.getId().equals(a.getId()))
			.collect(Collectors.toList());
		Datastore ds = DatastoreProvider.get();
		Key key = ds.newKeyFactory().setKind("FilteredArtist").newKey(artist.getId());
		ds.delete(key);
		model.put("user", user);
		model.put("filteredArtists", remaining);
		return new ModelAndView("helloworld", "model", model);
	}
}
