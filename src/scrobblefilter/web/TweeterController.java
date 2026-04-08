package scrobblefilter.web;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.RequestMapping;

import scrobblefilter.model.User;
import scrobblefilter.net.ScrobbleTweeter;

@Controller
public class TweeterController {

	private static final Logger log = Logger.getLogger(TweeterController.class.getName());

	ScrobbleTweeter tweeter = new ScrobbleTweeter();

	
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
	        tweeter.doTweet(user);
        } catch (Exception e) {
        	request.getSession().setAttribute("error", e.getMessage());
        	log.warning(e.getMessage());
        }
        response.sendRedirect("filter");
        
	}

	
	
	

}
