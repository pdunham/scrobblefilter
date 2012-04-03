package scrobblefilter.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.googlecode.objectify.ObjectifyService;

import scrobblefilter.model.FilteredArtist;
import scrobblefilter.model.User;

public class ContextInitializer implements ServletContextListener {

	 public void contextDestroyed(ServletContextEvent arg) {}
	
	 public void contextInitialized(ServletContextEvent arg) {
		 ObjectifyService.register(User.class);
		 ObjectifyService.register(FilteredArtist.class);
	 }
}