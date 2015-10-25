package gr.cyberstream.auth.util;


public interface MailManager {
	
	public boolean sendMail(String from, String to, String subject, String content);
}
