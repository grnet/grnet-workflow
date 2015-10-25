package gr.cyberstream.auth.service;

import java.util.ArrayList;
import java.util.List;

import gr.cyberstream.auth.dao.UsersDAO;
import gr.cyberstream.auth.model.User;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthenticationProviderImpl implements AuthenticationProvider {

	private UsersDAO usersDAO;

	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {

		String email = (String) auth.getPrincipal();

		User user = usersDAO.getUser(email);
		
		if (user != null) {
			
			if (user.getStatus().equals(User.STATUS_DISABLED)) {

				throw new DisabledException("User " + user.getEmail() + " has been disabled.");
			}

			if (user.getStatus().equals(User.STATUS_PENDING) || user.getStatus().equals(User.STATUS_MODERATED)) {

				throw new LockedException("User " + user.getEmail() + " is not yet verified.");
			}

			if (user.getStatus().equals(User.STATUS_BANNED)) {

				throw new LockedException("User " + user.getEmail() + " has been banned.");
			}

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			
			if (auth.getCredentials() != null && encoder.matches((String) auth.getCredentials(), user.getPassword())) {
				
				List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
								
				auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),
						auth.getCredentials(), authorities);
			}
			
		} else {
			
			throw new AuthenticationCredentialsNotFoundException("Email " + email + " is not found.");
		}
		
		return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {

		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public UsersDAO getUsersDAO() {
	
		return usersDAO;
	}

	public void setUsersDAO(UsersDAO usersDAO) {
	
		this.usersDAO = usersDAO;
	}

}
