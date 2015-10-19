package gr.cyberstream.workflow.engine.config;

import gr.cyberstream.workflow.engine.cmis.CMISSession;
import gr.cyberstream.workflow.engine.cmis.OpenCMISSessionFactory;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.chemistry.opencmis.client.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Is responsible for configuring the various modules of the underline
 * frameworks and the application itself
 * 
 * @author nlyk
 * 
 */
@Configuration
@EnableWebMvc
@EnableTransactionManagement
@ComponentScan(basePackages = { "gr.cyberstream.workflow.engine.controller", "gr.cyberstream.workflow.engine.service",
		"gr.cyberstream.workflow.engine.persistence" })
@PropertySource("classpath:workflow-engine.properties")
public class ApplicationConfiguration {

	final static Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

	@Autowired
	private Environment env;

	@Bean
	public DataSource dataSource() {

		DriverManagerDataSource dataSource = new DriverManagerDataSource();

		dataSource.setDriverClassName(env.getProperty("database.driver"));
		dataSource.setUrl(env.getProperty("database.url"));
		dataSource.setUsername(env.getProperty("database.user"));
		dataSource.setPassword(env.getProperty("database.password"));

		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
			JpaVendorAdapter jpaVendorAdapter) {

		LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();

		emfb.setDataSource(dataSource);
		emfb.setJpaVendorAdapter(jpaVendorAdapter);
		emfb.setPackagesToScan("gr.cyberstream.workflow.engine.model");

		// add special JPA properties
		Map<String, Object> jpaProperties;
		jpaProperties = emfb.getJpaPropertyMap();
		jpaProperties.put("hibernate.show_sql", env.getProperty("database.showsql", Boolean.class));
		jpaProperties.put("hibernate.format_sql", env.getProperty("database.showsql", Boolean.class));
		jpaProperties.put("hibernate.use_sql_comments", env.getProperty("database.showsql", Boolean.class));
		emfb.setJpaPropertyMap(jpaProperties);

		return emfb;
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {

		HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();

		adapter.setDatabasePlatform(env.getProperty("database.type"));
		adapter.setShowSql(env.getProperty("database.showsql", Boolean.class));
		adapter.setGenerateDdl(false);
		adapter.setDatabasePlatform(env.getProperty("database.dialect"));

		return adapter;
	}

	@Bean
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {

		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);

		return transactionManager;
	}

	@Bean
	public BeanPostProcessor persistenceTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("utf-8");
		resolver.setMaxUploadSize(5 * 1025 * 1024);
		return resolver;
	}

	// Activiti configuration
	// =================================================================================================
	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration(DriverManagerDataSource dataSource,
			PlatformTransactionManager txManager) {

		SpringProcessEngineConfiguration speconfig = new SpringProcessEngineConfiguration();
		speconfig.setDataSource(dataSource());
		speconfig.setTransactionManager(txManager);
		speconfig.setDatabaseSchemaUpdate("true");
		speconfig.setJobExecutorActivate(false);

		return speconfig;
	}

	@Bean
	public ProcessEngineFactoryBean processEngineFactoryBean(SpringProcessEngineConfiguration spec) {
		ProcessEngineFactoryBean pefbean = new ProcessEngineFactoryBean();
		pefbean.setProcessEngineConfiguration(spec);
		return pefbean;

	}

	@Bean
	public RepositoryService repositoryService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getRepositoryService();
	}

	@Bean
	public RuntimeService runtimeService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getRuntimeService();
	}

	@Bean
	public HistoryService historyService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getHistoryService();
	}

	@Bean
	public ManagementService managementService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getManagementService();
	}

	@Bean
	public IdentityService identityService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getIdentityService();
	}

	@Bean
	public FormService formService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getFormService();
	}

	@Bean
	public TaskService taskService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getTaskService();
	}

	// CMIS configuration
	// =================================================================================================
	@Bean(destroyMethod = "cleanUp")
	public CMISSession cmisSession() {
		Session session;

		String cmisServerUrl = env.getProperty("cmis.service.url");
		String repository = env.getProperty("cmis.repository.id");
		String username = env.getProperty("cmis.username");
		String password = env.getProperty("cmis.password");
		
		CMISSession sessionBean = new CMISSession();
		session = OpenCMISSessionFactory.createOpenCMISSession(cmisServerUrl, repository, username, password);
		sessionBean.setSession(session);

		logger.info("CMIS connection successed - repository: " + session.getRepositoryInfo().getName());
		
		return sessionBean;
	}
}