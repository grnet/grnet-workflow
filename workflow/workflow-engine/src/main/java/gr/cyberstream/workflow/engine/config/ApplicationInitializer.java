/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * It initializes the application using the <br>
 * <a href=
 * "http://docs.oracle.com/javaee/7/api/javax/servlet/ServletContainerInitializer.html"
 * >ServletContainerInitializer</a><br>
 * standard implemented by Spring
 * 
 * @author nlyk
 *
 */
@Order(1)
public class ApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	final static Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

	@Override
	protected Class<?>[] getRootConfigClasses() {
		logger.info("warming-up workflow-engine ...");
		return null;
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] { ApplicationConfiguration.class };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/api/*" };
	}

}
