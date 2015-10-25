package gr.cyberstream.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtilDB implements PasswordUtil {

	private BCryptPasswordEncoder encoder;
	
	public PasswordUtilDB() {

		encoder = new BCryptPasswordEncoder();
	}

	@Override
	public String encode(String password) {

		return encoder.encode(password);
	}

}
