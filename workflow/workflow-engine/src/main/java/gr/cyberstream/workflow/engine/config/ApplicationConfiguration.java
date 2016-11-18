package gr.cyberstream.workflow.engine.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import gr.cyberstream.workflow.engine.cmis.CMISSession;
import gr.cyberstream.workflow.engine.customtypes.ApproveFormType;
import gr.cyberstream.workflow.engine.customtypes.ConversationFormType;
import gr.cyberstream.workflow.engine.customtypes.DocumentFormType;
import gr.cyberstream.workflow.engine.customtypes.EmailFormType;
import gr.cyberstream.workflow.engine.customtypes.MessageFormType;
import gr.cyberstream.workflow.engine.customtypes.PositionFormType;
import gr.cyberstream.workflow.engine.customtypes.TextareaFormType;
import gr.cyberstream.workflow.engine.listeners.CustomTaskFormFields;
import gr.cyberstream.workflow.engine.listeners.ProcessEventListener;
import gr.cyberstream.workflow.engine.listeners.StartEventFormFields;

/**
 * Is responsible for configuring the various modules of the underline
 * frameworks and the application itself
 * 
 * @author nlyk
 *
 */
@Configuration
@EnableScheduling
@EnableTransactionManagement
@ComponentScan(basePackages = { "gr.cyberstream.workflow.engine.config", "gr.cyberstream.workflow.engine.controller",
		"gr.cyberstream.workflow.engine.service", "gr.cyberstream.workflow.engine.persistence",
		"gr.cyberstream.workflow.engine.cmis", "gr.cyberstream.workflow.engine.listeners",
		"gr.cyberstream.workflow.engine.customservicetasks" })
@PropertySource("classpath:workflow-engine.properties")
public class ApplicationConfiguration extends WebMvcConfigurationSupport {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

	@Autowired
	private Environment env;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Override
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();

		handlerMapping.setAlwaysUseFullPath(true);
		handlerMapping.setRemoveSemicolonContent(false);
		return handlerMapping;
	}

	@Override
	protected void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new WebContentInterceptor() {

			@Override
			public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

				response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
				response.setHeader("Pragma", "no-cache");
				response.setDateHeader("Expires", 0);
			}

			@Override
			public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
				
			}
		});
	}

	@Bean
	public DataSource dataSource() {
		DataSource datasource = null;

		try {
			Context ctx = new InitialContext();
			datasource = (DataSource) ctx.lookup(env.getProperty("datasource.name"));

		} catch (NamingException e) {
			logger.error("Unable to find datasource " + env.getProperty("datasource.name") + ". " + e.getMessage());
		}

		return datasource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
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
		jpaProperties.put("hibernate.format_sql", true);
		jpaProperties.put("hibernate.hbm2ddl.auto", "validate");
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
		resolver.setMaxUploadSize(20 * 1024 * 1024);
		return resolver;
	}

	@Bean
	public SettingsStatus settingsStatus() {
		SettingsStatus status = new SettingsStatus();
		return status;
	}

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

		javaMailSender.setHost(env.getProperty("mail.host"));
		javaMailSender.setPort(env.getProperty("mail.port", Integer.class));
		javaMailSender.setUsername(env.getProperty("mail.username"));
		javaMailSender.setPassword(env.getProperty("mail.password"));
		javaMailSender.setDefaultEncoding("UTF-8");

		Properties properties = new Properties();
		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.debug", "false");

		javaMailSender.setJavaMailProperties(properties);

		return javaMailSender;
	}

	// Activiti configuration
	// =================================================================================================
	private @Autowired AutowireCapableBeanFactory beanFactory;

	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration(DataSource dataSource, PlatformTransactionManager txManager) {

		SpringProcessEngineConfiguration speconfig = new SpringProcessEngineConfiguration();
		speconfig.setDataSource(dataSource());
		speconfig.setTransactionManager(txManager);
		speconfig.setDatabaseSchemaUpdate("true");
		speconfig.setJobExecutorActivate(true);

		speconfig.setMailServerDefaultFrom(env.getProperty("mail.username"));
		speconfig.setMailServerHost(env.getProperty("mail.host"));
		speconfig.setMailServerPort(env.getProperty("mail.port", Integer.class));
		speconfig.setMailServerUsername(env.getProperty("mail.username"));
		speconfig.setMailServerPassword(env.getProperty("mail.password"));
		speconfig.setMailServerUseTLS(false);
		speconfig.setMailServerUseSSL(false);
		speconfig.setHistoryLevel(HistoryLevel.FULL);

		// register event listeners
		ProcessEventListener eventListener = new ProcessEventListener();
		beanFactory.autowireBean(eventListener);
		List<ActivitiEventListener> listeners = new ArrayList<ActivitiEventListener>();

		listeners.add(eventListener);
		speconfig.setEventListeners(listeners);

		List<BpmnParseHandler> bpmnParseHandlers = new ArrayList<BpmnParseHandler>();
		bpmnParseHandlers.add(new CustomTaskFormFields());
		bpmnParseHandlers.add(new StartEventFormFields());
		speconfig.setPreBpmnParseHandlers(bpmnParseHandlers);

		// add the custom types to the Activiti engine configuration
		List<AbstractFormType> customFormTypes = new ArrayList<AbstractFormType>();
		customFormTypes.add(new DocumentFormType());
		customFormTypes.add(new ApproveFormType());
		customFormTypes.add(new ConversationFormType());
		customFormTypes.add(new MessageFormType());
		customFormTypes.add(new EmailFormType());
		customFormTypes.add(new TextareaFormType());
		customFormTypes.add(new PositionFormType());
		speconfig.setCustomFormTypes(customFormTypes);

		return speconfig;
	}

	@Bean
	public ProcessEngineFactoryBean processEngineFactoryBean(SpringProcessEngineConfiguration spec) {
		
		ProcessEngineFactoryBean pefbean = new ProcessEngineFactoryBean();
		pefbean.setProcessEngineConfiguration(spec);
		return pefbean;

	}

	/*
	 * @Bean public EndEventListenerImplementation
	 * endEventListener(SpringProcessEngineConfiguration spec){ spec.getBeans()
	 * EndEventListenerImplementation listener = new
	 * EndEventListenerImplementation();
	 * listener.setProcessService(pefb.getObject().get); return listener; }
	 */

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

	@Autowired
	public KeycloakClientRequestFactory keycloakClientRequestFactory;

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public KeycloakRestTemplate keycloakRestTemplate() {
		return new KeycloakRestTemplate(keycloakClientRequestFactory);
	}

	// CMIS configuration
	// =================================================================================================
	@Bean(destroyMethod = "cleanUp")
	public CMISSession cmisSession() {

		return new CMISSession(env.getProperty("cmis.service.url"), env.getProperty("cmis.repository.id"),
				env.getProperty("cmis.username"), env.getProperty("cmis.password"));
	}
}