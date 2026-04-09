package scrobblefilter.web;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import com.googlecode.objectify.ObjectifyService;

import scrobblefilter.AppConfig;
import scrobblefilter.model.FilteredArtist;
import scrobblefilter.model.User;

public class ContextInitializer implements ServletContextListener {

	 public void contextDestroyed(ServletContextEvent arg) {}

	 public void contextInitialized(ServletContextEvent arg) {
		 ObjectifyService.register(User.class);
		 ObjectifyService.register(FilteredArtist.class);
		 try (InputStream in = arg.getServletContext()
				 .getResourceAsStream("/WEB-INF/scrobblefilter.properties")) {
			 if (in != null) {
				 AppConfig.load(in);
			 }
		 } catch (IOException e) {
			 throw new RuntimeException("Failed to load scrobblefilter.properties", e);
		 }
	 }
}