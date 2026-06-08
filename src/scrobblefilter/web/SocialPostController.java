package scrobblefilter.web;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.RequestMapping;

import scrobblefilter.model.User;
import scrobblefilter.net.SocialPoster;
import scrobblefilter.net.StatusComposer;

@Controller
public class SocialPostController {

	private static final Logger log = Logger.getLogger(SocialPostController.class.getName());

	@Autowired
	List<SocialPoster> posters;

	@Autowired
	StatusComposer composer;


	/** Manual "post now" to a specific platform (?platform=twitter|bluesky). */
	@RequestMapping(value="post", method=GET)
	public void post(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String platform = request.getParameter("platform");
		postNow(request, response, platform != null ? platform : "twitter");
	}

	/** Back-compat: the original Twitter-only "tweet it" link. */
	@RequestMapping(value="tweet", method=GET)
	public void tweetFilteredList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		postNow(request, response, "twitter");
	}

	// Manual posting is an explicit action, so it posts to a connected account
	// regardless of the weekly cron opt-in, surfacing any failure to the user.
	private void postNow(HttpServletRequest request, HttpServletResponse response, String platform)
			throws IOException {
		request.setCharacterEncoding("UTF-8");
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect("welcome");
			return;
		}
		try {
			SocialPoster target = null;
			for (SocialPoster poster : posters) {
				if (poster.platform().equals(platform)) target = poster;
			}
			if (target == null) {
				request.getSession().setAttribute("error", "Unknown platform: " + platform);
			} else if (!target.isConnected(user)) {
				request.getSession().setAttribute("error", "Your " + platform + " account is not linked.");
			} else {
				target.post(user, composer.compose(user));
				request.getSession().removeAttribute("error");
			}
		} catch (Exception e) {
			request.getSession().setAttribute("error", e.getMessage());
			log.warning(e.getMessage());
		}
		response.sendRedirect("filter");
	}

}
