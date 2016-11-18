package gr.cyberstream.workflow.engine.config;

import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends KeycloakWebSecurityConfigurerAdapter {

	/**
	 * Registers the KeycloakAuthenticationProvider with the authentication
	 * manager
	 * 
	 * @param auth
	 * @throws Exception
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		auth.authenticationProvider(keycloakAuthenticationProvider());
	}

	/**
	 * Defines the session authentication strategy.
	 */
	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		// return new RegisterSessionAuthenticationStrategy(new
		// SessionRegistryImpl());
		return new NullAuthenticatedSessionStrategy();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		source.registerCorsConfiguration("/**", config);
		CorsFilter filter = new CorsFilter(source);

		super.configure(http);
		http.addFilterBefore(filter, ChannelProcessingFilter.class).csrf().disable().authorizeRequests()
				.regexMatchers(HttpMethod.GET, "/api/process/[0-9]+/diagram(\\?task=[^&]+)?").permitAll()
				.regexMatchers(HttpMethod.GET, "/api/instance/[0-9]+/diagram").permitAll().antMatchers("/api/public/**")
				.permitAll().antMatchers("/api/v2/public/**").permitAll().antMatchers("/api/getstatus").permitAll()
				.antMatchers("/api/**").authenticated();
	}
}
