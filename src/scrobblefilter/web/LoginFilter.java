package scrobblefilter.web;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import scrobblefilter.model.User;

public class LoginFilter implements Filter {

	public void init(FilterConfig config) {}
	
	public void destroy() {}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
		throws ServletException, IOException
	{
		System.out.println("doFilter invoked");
/*		HttpSession session =
		          ((HttpServletRequest)req).getSession(false);
		      User currentUser = (User)session.getAttribute("user");
	*/
		chain.doFilter(req, res);
	}

}