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


	@RequestMapping(value="tweet", method=GET)
	public void tweetFilteredList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
        User user = (User)request.getSession().getAttribute("user");
        if (user==null) {
        	response.sendRedirect("welcome");
        	return;
        }
        // Manual "tweet it" is an explicit, Twitter-specific action: post to
        // Twitter regardless of the cron opt-in, surfacing any failure to the user.
        try {
	        String statusText = composer.compose(user);
	        for (SocialPoster poster : posters) {
	        	if ("twitter".equals(poster.platform())) {
	        		poster.post(user, statusText);
	        	}
	        }
        } catch (Exception e) {
        	request.getSession().setAttribute("error", e.getMessage());
        	log.warning(e.getMessage());
        }
        response.sendRedirect("filter");
        
	}

	
	
	

}
