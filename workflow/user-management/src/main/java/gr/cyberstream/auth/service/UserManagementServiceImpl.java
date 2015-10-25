package gr.cyberstream.auth.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;

import gr.cyberstream.auth.dao.RolesDAO;
import gr.cyberstream.auth.dao.UsersDAO;
import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;
import gr.cyberstream.auth.util.MailProcess;

public class UserManagementServiceImpl implements UserManagementService {

	private UsersDAO usersDAO;
	private RolesDAO rolesDAO;
	private MailProcess mailProcess;
	
	private PropertyResourceBundle properties;

	public UserManagementServiceImpl(UsersDAO usersDAO, RolesDAO rolesDAO, MailProcess mailProcess) {

		this.usersDAO = usersDAO;
		this.rolesDAO = rolesDAO;
		this.mailProcess = mailProcess;
		
		properties = (PropertyResourceBundle) ResourceBundle.getBundle("auth");		
	}
	
	@Override
	public boolean createUser(User user, String password) {
		
		if (usersDAO.getUser(user.getEmail()) == null) {
			
			usersDAO.saveUser(user);
			rolesDAO.addUserRole(user, Role.ROLE_USER);
			
			PropertyResourceBundle elLabels = (PropertyResourceBundle) ResourceBundle.getBundle(
					"gr.cyberstream.auth.resources.show", new Locale("el"));
			PropertyResourceBundle enLabels = (PropertyResourceBundle) ResourceBundle.getBundle(
					"gr.cyberstream.auth.resources.show", new Locale("en"));
			
			// Send notification email
			String mailAdmin = properties.getString("mailAdmin");
			String subject = elLabels.getString("notificationEmail.subject") 
					+ " / " + enLabels.getString("notificationEmail.subject");
			
			String content = mailContent(elLabels.getString("companyName"), enLabels.getString("companyName"),
					elLabels.getString("notificationEmail.content1") + " " + user.getEmail() + " " 
							+ elLabels.getString("notificationEmail.content2") + " " + password,
					enLabels.getString("notificationEmail.content1") + " " + user.getEmail() + " " 
							+ enLabels.getString("notificationEmail.content2") + " " + password);
			
			sendMail(mailAdmin, user.getEmail(), subject, content);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void approveUser(User user) {

		PropertyResourceBundle elLabels = (PropertyResourceBundle) ResourceBundle.getBundle(
				"gr.cyberstream.auth.resources.show", new Locale("el"));
		PropertyResourceBundle enLabels = (PropertyResourceBundle) ResourceBundle.getBundle(
				"gr.cyberstream.auth.resources.show", new Locale("en"));
		
		// Set expiration date
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(properties.getString("hoursExpire")));

		user.setExpirationDate(cal.getTime());

		// Create activation key and set it
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String dateString = formatter.format(user.getCreationDate());
		String keyText = dateString + user.getEmail();
		UUID activationKey = UUID.nameUUIDFromBytes(keyText.getBytes());

		user.setActivationKey(activationKey.toString().replaceAll("-", ""));

		// Send request confirmation email
		String mailAdmin = properties.getString("mailAdmin");
		String subject = elLabels.getString("verifyEmail.subject") + " / " + enLabels.getString("verifyEmail.subject");
		String url = properties.getString("appServer") + properties.getString("verifyPath") + "?activation="
				+ user.getActivationKey();
		
		String content = mailContent(elLabels.getString("companyName"), enLabels.getString("companyName"),
				elLabels.getString("verifyEmail.content") + ": " + url,
				enLabels.getString("verifyEmail.content") + ": " + url);
		
		sendMail(mailAdmin, user.getEmail(), subject, content);

		user.setStatus(User.STATUS_PENDING);

		// Persist User
		usersDAO.updateUser(user);
	}
	
	@Override
	public boolean resetPassword(User user) {
		
		PropertyResourceBundle elLabels = (PropertyResourceBundle) ResourceBundle.getBundle(
				"gr.cyberstream.auth.resources.show", new Locale("el"));
		PropertyResourceBundle enLabels = (PropertyResourceBundle) ResourceBundle.getBundle(
				"gr.cyberstream.auth.resources.show", new Locale("en"));
		
		Date now = new Date();
		Date expirationDate;

		// create new unique key and new expiration date
		Calendar cal = Calendar.getInstance();
		
		try {
			
			cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(properties.getString("hoursExpire")));
			expirationDate = cal.getTime();
			user.setExpirationDate(expirationDate);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			cal.add(Calendar.HOUR_OF_DAY, 20);
			expirationDate = cal.getTime();
			user.setExpirationDate(expirationDate);
		}

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String dateString = formatter.format(now);
		
		String keyText = dateString + user.getEmail();
		UUID activationKey = UUID.nameUUIDFromBytes(keyText.getBytes());
		
		user.setActivationKey(activationKey.toString().replaceAll("-", ""));
		
		updateUser(user);
		
		String mailAdmin = properties.getString("mailAdmin");
		String subject = elLabels.getString("passwordResetEmail.subject") 
				+ " / " + enLabels.getString("passwordResetEmail.subject");
		String url = properties.getString("appServer") + properties.getString("resetPath") + "?id="
				+ user.getActivationKey();
		
		String content = mailContent(elLabels.getString("companyName"), enLabels.getString("companyName"),
				elLabels.getString("passwordResetEmail.content") + ": " + url,
				enLabels.getString("passwordResetEmail.content") + ": " + url);
		
		if (mailProcess.sendMail(mailAdmin, user.getEmail(), subject, content))
			return true;
		else
			return false;
	}
	
	public boolean comparePassword(User user, String password) {
		
		return usersDAO.comparePassword(user, password);
	}
		
	public void removeUser(User user) {
		
		rolesDAO.removeRoles(user);
		usersDAO.removeUser(user);
	}

	public void saveUser(User user) {

		usersDAO.saveUser(user);
	}
	
	public void updateUser(User user) {

		usersDAO.updateUser(user);
	}

	public User getUser(String email) {

		return usersDAO.getUser(email);
	}

	public User getUserByKey(String key) {

		return usersDAO.getUserByKey(key);
	}
	
	public List<User> searchUsers(String keyword, String... statusArray) {
		
		return usersDAO.searchUsers(keyword, statusArray);
	}
	
	public List<User> getUsers(String... status) {
		
		return usersDAO.getUsers(status);
	}
	
	public List<Role> getRoles() {
		
		return rolesDAO.getRoles();
	}
	
	public Role getRole(String name) {
		
		return rolesDAO.getRole(name);
	}
	
	public List<User> getRoleMembers(Role role) {

		return usersDAO.getRoleMembers(role);
	}
	
	public List<Role> getUserRoles(User user) {
		
		return rolesDAO.getUserRoles(user);
	}
	
	public void saveRole(Role role) {
		
		rolesDAO.saveRole(role);
	}
	
	public void removeRole(Role role) {
		
		rolesDAO.removeRole(role);
	}
	
	public void addUserRole(User user, String roleName) {
		
		rolesDAO.addUserRole(user, roleName);
	}
	
	public void removeUserRole(User user, Role role) {
		
		rolesDAO.removeUserRole(user, role);
	}
	
	private String mailContent(String elCompanyName, String enCompanyName, String elText, String enText) {
		
		String mailContent = "<table cellspacing=\"5\" cellpadding=\"3\" style=\"table-layout: fixed; font-family: Arial, sans-serif; font-size: 16px; line-height: 18px\">"
				+ "<tr><td>"
				+ elCompanyName + " / " + enCompanyName
				+ "</td></tr>"
				+ "</table>"
				+ "<table cellspacing=\"5\" cellpadding=\"3\" style=\"table-layout: fixed; font-family: Arial, sans-serif; font-size: 14px; line-height: 16px\">"
				+ "<tr><td>"
				+ elText
				+ "</td></tr>"
				+ "<tr><td>"
				+ enText
				+ "</td></tr>"
				+ "</table>";
		
		return mailContent;
	}
	
	public void sendMail(String from, String to, String subject, String content) {

		mailProcess.sendMail(from, to, subject, content);
	}
}
