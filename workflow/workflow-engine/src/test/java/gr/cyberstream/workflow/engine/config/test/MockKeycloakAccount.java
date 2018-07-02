package gr.cyberstream.workflow.engine.config.test;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;

public class MockKeycloakAccount implements OidcKeycloakAccount {

	private String name;
	private String email;
	private Set<String> roles;
	private List<String> groups;
	
	public MockKeycloakAccount(String name, String email, Set<String> roles, List<String> groups) {
		
		this.name = name;
		this.email = email;
		this.roles = roles;
		this.groups = groups;
	}
	
	@Override
	public Set<String> getRoles() {
		
		return roles;
	}
	
	@Override
	public Principal getPrincipal() {
		
		return new Principal() {
			
			@Override
			public String getName() {
				
				return name;
			}
		};
	}
	
	@Override
	public KeycloakSecurityContext getKeycloakSecurityContext() {
		
		String tokenString = "";
		
		AccessToken accessToken = new AccessToken();
		accessToken.setName(name);
		accessToken.setEmail(email);
		accessToken.setOtherClaims("groups", groups);
		
		String idTokenString = "";
		IDToken idToken = new IDToken();
		
		return new KeycloakSecurityContext(tokenString, accessToken, idTokenString, idToken);
	}

}
