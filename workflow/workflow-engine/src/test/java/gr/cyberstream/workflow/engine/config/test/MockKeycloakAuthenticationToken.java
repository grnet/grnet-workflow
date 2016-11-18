package gr.cyberstream.workflow.engine.config.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class MockKeycloakAuthenticationToken extends KeycloakAuthenticationToken {

	private static final long serialVersionUID = 2296646695111745995L;

	public MockKeycloakAuthenticationToken(KeycloakAccount account) {
		super(account);

		this.setAuthenticated(true);
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		for (String role : getAccount().getRoles()) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		return authorities;
	}
}
