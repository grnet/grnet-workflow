package gr.cyberstream.auth.util;

import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;

public class PasswordUtilLdap implements PasswordUtil {

	LdapShaPasswordEncoder encoder;

	public PasswordUtilLdap() {

		encoder = new LdapShaPasswordEncoder();
	}

	@Override
	public String encode(String password) {

		return encoder.encodePassword(password, null);
	}
}
