package gr.cyberstream.auth.util;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class MailProcess implements MailManager {

	private JavaMailSender mailSender;

	public boolean sendMail(String from, String to, String subject, String content) {

		MimeMessage msg = mailSender.createMimeMessage();

		try {

			MimeMessageHelper helper = new MimeMessageHelper(msg, true);

			helper.setTo(to);
			helper.setFrom(from);
			helper.setSubject(subject);
			helper.setText(content, true);
			
			this.mailSender.send(msg);
			
			return true;

		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public JavaMailSender getMailSender() {

		return mailSender;
	}

	public void setMailSender(JavaMailSender mailSender) {

		this.mailSender = mailSender;
	};
}
