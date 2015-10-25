package gr.cyberstream.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

	public UserDetailsServiceImpl() {
	}

	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		
		Map<String, Object> attributes = token.getAssertion().getPrincipal().getAttributes();
		
		if (attributes.containsKey("role")) {
		
			if (attributes.get("role") instanceof String) {
				
				authorities.add(new SimpleGrantedAuthority("ROLE_" + getCN((String) attributes.get("role"))));
				
			} else if (attributes.get("role") instanceof List) {
				
				@SuppressWarnings("unchecked")
				List<String> roles = (List<String>) attributes.get("role");
				
				for (String role : roles) {
					
					authorities.add(new SimpleGrantedAuthority("ROLE_" + getCN(role)));
				}	
			}	
		}
		
		return new User(token.getName(), "", authorities);
	}

	private String getCN(String role) {
		
		int start = role.indexOf('=');
		int end = role.indexOf(',');
		
		if (start != -1 && end != -1 && start <= end)
			return role.substring(role.indexOf('=') + 1, end);
		else
			return role;
	}
}
