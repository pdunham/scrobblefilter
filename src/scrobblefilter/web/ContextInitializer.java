package scrobblefilter.web;

import java.io.InputStream;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import scrobblefilter.AppConfig;

public class ContextInitializer implements ServletContextListener {

	 public void contextDestroyed(ServletContextEvent arg) {}

	 public void contextInitialized(ServletContextEvent arg) {
		 try (InputStream in = arg.getServletContext()
				 .getResourceAsStream("/WEB-INF/scrobblefilter.properties")) {
			 if (in != null) {
				 AppConfig.load(in);
			 }
		 } catch (Exception e) {
			 System.err.println("STARTUP ERROR: " + e);
			 e.printStackTrace();
			 throw new RuntimeException("Failed to load scrobblefilter.properties", e);
		 }
	 }
}
