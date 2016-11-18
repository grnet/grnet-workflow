package gr.cyberstream.workflow.engine.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import gr.cyberstream.workflow.engine.servlet.DocumentServlet;

@Order(1)
public class ServletInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		ServletRegistration.Dynamic documentServlet = servletContext.addServlet("document", new DocumentServlet());
		documentServlet.setLoadOnStartup(0);
		documentServlet.addMapping("/document/*");
	}
}
