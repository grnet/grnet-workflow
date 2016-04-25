package gr.cyberstream.workflow.engine.cmis.test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import org.springframework.core.env.Environment;

import org.springframework.transaction.annotation.EnableTransactionManagement;

import gr.cyberstream.workflow.engine.cmis.CMISSession;

/**
 * Is responsible for configuring the various modules of the underline
 * frameworks and the application itself
 * 
 * @author nlyk
 * 
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = { "gr.cyberstream.workflow.engine.cmis" })
@PropertySource("classpath:workflow-engine.properties")
public class CmisConfiguration {

	@Autowired
	private Environment env;

	// CMIS configuration
	// =================================================================================================
	@Bean(destroyMethod = "cleanUp")
	public CMISSession cmisSession() {
		
		return new CMISSession(env.getProperty("cmis.service.url"), env.getProperty("cmis.repository.id"),
				env.getProperty("cmis.username"), env.getProperty("cmis.password"));
	}
}