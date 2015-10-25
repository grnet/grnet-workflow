package gr.cyberstream.auth.servlet;

import gr.cyberstream.auth.model.User;
import gr.cyberstream.auth.service.UserManagementService;

import java.io.IOException;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class VerifyUserServlet extends HttpServlet {

	private static final long serialVersionUID = -66370338796075137L;

	private UserManagementService userManagementService;
	private PropertyResourceBundle properties;

	public VerifyUserServlet() {

		properties = (PropertyResourceBundle) ResourceBundle.getBundle("auth");
	}

	public void init(ServletConfig config) throws ServletException {

		super.init(config);
		
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		userManagementService = (UserManagementService) ctx.getBean("userManagementService");
	}

	public void destroy() {

	}

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		if (request.getParameterMap().containsKey("activation")) {

			String key = request.getParameter("activation");
			User user = userManagementService.getUserByKey(key);

			if (user != null && user.getStatus().equals(User.STATUS_PENDING)) {

				// check if expiration time is over
				Date currentDate = new Date();

				if (currentDate.compareTo(user.getExpirationDate()) < 0) {

					user.setStatus(User.STATUS_VERIFIED);
					user.setExpirationDate(currentDate);

					userManagementService.updateUser(user);

					// success verification page
					response.sendRedirect("/auth/successVer.jsf?url=" + properties.getString("applicationUrl"));

				} else {
					
					userManagementService.approveUser(user);
					
					response.sendRedirect("/auth/expired.jsf");

				}
			} else
				response.sendRedirect("/auth/invalid.jsf");

		} else {
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}

	}

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		processRequest(request, response);
	}

	/*
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo() {

		return "Verifies User and redirects to a page";
	}
}
