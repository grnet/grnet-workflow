package gr.cyberstream.auth.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CredentialsValidator {
	
	private Pattern mailPattern, pwdPattern;
	private Matcher mailMatcher, pwdMatcher;
 
	private static final String EMAIL_PATTERN = 
		"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	//at least one number, at least 6 characters,
	//at least one special character, no whitespace allowed
	private static final String PASSWORD_PATTERN = 
		"^(?=.*[0-9])(?=.*[!@#$%^&*+=])(?=\\S+$).{6,}$";	
	
	public CredentialsValidator() {
		mailPattern = Pattern.compile(EMAIL_PATTERN);
		pwdPattern = Pattern.compile(PASSWORD_PATTERN);
				
	}
	
	/*
	 * @return true if valid email 
	 */
	public boolean mailValidate(final String hex) {
		 
		mailMatcher = mailPattern.matcher(hex);
		return mailMatcher.matches();
 
	}
	
	/*
	 * @return true if valid password
	 */
	public boolean pwdValidate(final String hex){
		pwdMatcher = pwdPattern.matcher(hex);
		return pwdMatcher.matches();
	}
}
