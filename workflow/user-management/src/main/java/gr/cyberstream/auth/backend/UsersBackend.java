package gr.cyberstream.auth.backend;

import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;
import gr.cyberstream.auth.service.UserManagementService;
import gr.cyberstream.auth.util.CredentialsValidator;
import gr.cyberstream.auth.util.FacebookAuthHelper;
import gr.cyberstream.auth.util.GoogleAuthHelper;
import gr.cyberstream.auth.util.PasswordUtil;

import java.io.IOException;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

@SessionScoped
@ManagedBean(name = "usersBackend")
public class UsersBackend {

	@ManagedProperty(value = "#{userManagementService}")
	private UserManagementService userManagementService;

	@ManagedProperty(value = "#{passwordUtil}")
	private PasswordUtil passwordUtil;

	private PropertyResourceBundle show;
	private PropertyResourceBundle properties;

	private String registerPassword;
	private String confRegisterPassword;

	private String passEmail;

	private String resetPassword;
	private String confResetPassword;

	private String password;
	private String newPassword;
	private String confNewPassword;

	private User editUser;
	private User user;

	private CredentialsValidator validator;

	private String successMessage;
	private String resetStatus;

	private boolean registeredGoogle;
	private boolean registeredFacebook;

	private GoogleAuthHelper googleHelper;
	private FacebookAuthHelper fbHelper;

	private GoogleTokenResponse token;

	private String userInfo;

	private String returnURL;

	public UsersBackend() {

		validator = new CredentialsValidator();
		properties = (PropertyResourceBundle) ResourceBundle.getBundle("auth");
		show = (PropertyResourceBundle) ResourceBundle.getBundle("gr.cyberstream.auth.resources.show", FacesContext
				.getCurrentInstance().getViewRoot().getLocale());

		googleHelper = new GoogleAuthHelper();
		fbHelper = new FacebookAuthHelper();

		editUser = new User();

		resetStatus = "invalid";
	}

	// ---- Actions ----//
	/**
	 * Checks all requirements about registration, creates a User, and a unique
	 * registration key. If the registration is unmoderated a verification email
	 * is sent automatically.
	 */
	public String registerUser() {

		if (!validateRegister("registerForm", editUser, registerPassword, confRegisterPassword)) {

			return null;
		}

		editUser.setAccountType(User.SIMPLE_ACCOUNT);
		editUser.setPassword(passwordUtil.encode(registerPassword));

		Date date = new Date();

		// Set creation date
		editUser.setCreationDate(date);

		if (!properties.getString("moderatedRegistration").equalsIgnoreCase("true")) {

			userManagementService.saveUser(editUser);
			userManagementService.addUserRole(editUser, Role.ROLE_USER);

			userManagementService.approveUser(editUser);

			successMessage = show.getString("mailSent") + " " + editUser.getEmail() + " " + show.getString("verifyAcc");

		} else {

			editUser.setStatus(User.STATUS_MODERATED);

			// Persist User
			userManagementService.saveUser(editUser);
			userManagementService.addUserRole(editUser, Role.ROLE_USER);

			successMessage = show.getString("requestPending1") + " " + editUser.getEmail() + " "
					+ show.getString("requestPending2");
		}

		clearRegFields();

		return "success";
	}

	/**
	 * Given the email, creates a new activation key and a new expiration date
	 * and sends email with a URL for the reset password page
	 * 
	 * @return success or fail page
	 */
	public String requestPwdReset() {

		if (validateEmail("requestPassForm", passEmail)) {

			User user = userManagementService.getUser(passEmail);

			if (user == null) {

				FacesContext.getCurrentInstance().addMessage("requestPassForm:email",
						new FacesMessage(show.getString("error_email_incorrect")));
				return null;
			}

			if (user.getStatus().equals(User.STATUS_VERIFIED)) {

				userManagementService.resetPassword(user);
				successMessage = show.getString("requestPending1") + " " + passEmail + " "
						+ show.getString("resetPassMail");

				return "success";

			} else {

				FacesContext.getCurrentInstance().addMessage("requestPassForm:email",
						new FacesMessage(show.getString("notVerifyAcc")));
				return null;
			}
		}

		return null;
	}

	public String saveProfile() {

		if (validateName("profileForm", user)) {

			userManagementService.updateUser(user);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(show.getString("editProfileComp")));
		}

		return null;
	}

	/**
	 * resets the password of the User
	 * 
	 * @return success
	 */
	public String resetPwd() {

		if (validatePassword("resetForm", resetPassword, confResetPassword)) {

			user.setPassword(passwordUtil.encode(resetPassword));
			userManagementService.updateUser(user);

			successMessage = user.getFirstname() + " " + user.getLastname() + " " + show.getString("resetSucc");

			return "success";
		}

		return null;
	}

	public String changePassword() {

		return "/account/changePass";
	}

	public String savePassword() {

		if (validateUserPassword("changePassForm", user, password)
				&& validatePassword("changePassForm", newPassword, confNewPassword)) {
			
			user = userManagementService.getUser(user.getEmail());
			user.setPassword(passwordUtil.encode(newPassword));
			
			userManagementService.updateUser(user);

			successMessage = user.getFirstname() + " " + user.getLastname() + " " + show.getString("changeSucc");

			clearChangePwdFields();

			return "/success";
		}

		clearChangePwdFields();

		return null;
	}

	/**
	 * clears registration form fields
	 */
	public void clearRegFields() {

		editUser = new User();

		registerPassword = "";
		confRegisterPassword = "";
	}

	/**
	 * clears change password form fields
	 */
	public void clearChangePwdFields() {

		password = "";
		newPassword = "";
		confNewPassword = "";
	}

	/**
	 * @return true if all fields are not empty, password and email are valid
	 */
	public boolean validateRegister(String formID, User user, String password, String confPassword) {

		boolean valid = true;

		if (!validateName(formID, user)) {

			valid = false;
		}

		if (!validateEmail(formID, user.getEmail())) {

			valid = false;

		} else if (userManagementService.getUser(user.getEmail()) != null) {

			valid = false;
			FacesContext.getCurrentInstance().addMessage(formID + ":email",
					new FacesMessage(show.getString("error_email_exists")));
		}

		if (!validatePassword(formID, password, confPassword)) {

			valid = false;
		}

		return valid;
	}

	/**
	 * @param passEmail
	 * @return true if email is not empty and invalid
	 */
	public boolean validateEmail(String formID, String email) {

		if (email.length() == 0) {

			FacesContext.getCurrentInstance().addMessage(formID + ":email",
					new FacesMessage(show.getString("error_email_empty")));
			return false;

		} else if (!validator.mailValidate(email)) {

			FacesContext.getCurrentInstance().addMessage(formID + ":email",
					new FacesMessage(show.getString("error_email_invalid")));
			return false;
		}

		return true;
	}

	/**
	 * @param password
	 * @param confPassword
	 * @return true if new password is not empty and invalid
	 */
	public boolean validatePassword(String formID, String password, String confPassword) {

		if (password.length() == 0 || !validator.pwdValidate(password) || confPassword.length() == 0) {

			if (password.length() == 0) {

				FacesContext.getCurrentInstance().addMessage(formID + ":password",
						new FacesMessage(show.getString("error_password_empty")));

			} else if (!validator.pwdValidate(password)) {

				FacesContext.getCurrentInstance().addMessage(formID + ":password",
						new FacesMessage(show.getString("error_password_invalid")));
			}

			if (confPassword.length() == 0) {

				FacesContext.getCurrentInstance().addMessage(formID + ":confPassword",
						new FacesMessage(show.getString("error_confirmation_empty")));

			} else if (!password.equals(confPassword)) {

				FacesContext.getCurrentInstance().addMessage(formID + ":confPassword",
						new FacesMessage(show.getString("pwdNotMatch")));
			}

			return false;

		} else if (!password.equals(confPassword)) {

			FacesContext.getCurrentInstance().addMessage(formID + ":confPassword",
					new FacesMessage(show.getString("pwdNotMatch")));

			return false;
		}

		return true;
	}

	/**
	 * @param user
	 * @return true if authenticated user has firstname and lastname
	 */
	public boolean validateName(String formID, User user) {

		if (user.getFirstname().length() == 0 || user.getFirstname() == null || user.getLastname().length() == 0
				|| user.getLastname() == null) {

			if (user.getFirstname().length() == 0 || user.getFirstname() == null) {

				FacesContext.getCurrentInstance().addMessage(formID + ":firstname",
						new FacesMessage(show.getString("error_firstname_empty")));
			}

			if (user.getLastname().length() == 0 || user.getLastname() == null) {

				FacesContext.getCurrentInstance().addMessage(formID + ":lastname",
						new FacesMessage(show.getString("error_lastname_empty")));
			}

			return false;
		}
		return true;
	}

	public boolean validateUserPassword(String formID, User user, String password) {

		if (!userManagementService.comparePassword(user, passwordUtil.encode(password))) {

			FacesContext.getCurrentInstance().addMessage(formID + ":oldPassword",
					new FacesMessage(show.getString("error_oldPwd_incorrect")));

			return false;
		}

		return true;
	}

	public void setUser(PhaseEvent phaseEvent) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth.isAuthenticated()) {

			org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) auth
					.getPrincipal();

			if (user == null || !user.getEmail().equals(principal.getUsername())) {

				user = userManagementService.getUser(principal.getUsername());
			}
		}
	}

	/**
	 * checks the id parameter of reset page and instantiates the User
	 */
	public void checkUser(PhaseEvent phaseEvent) {

		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		String activationKey = request.getParameter("id");

		Date date = new Date();

		if (user == null && activationKey != null) {

			user = userManagementService.getUserByKey(activationKey);

			if (user != null && (date.compareTo(user.getExpirationDate()) < 0)) {

				resetStatus = "valid";

			} else if (user != null && (date.compareTo(user.getExpirationDate()) > 0)) {

				resetStatus = "expired";

			} else {

				resetStatus = "invalid";
			}
		}
	}

	/****************** GOOGLE Actions ******************/

	/**
	 * Sends Authentication Request to Google OAuth
	 */
	public void googleAuth() {

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) externalContext.getSession(false);

		session.setAttribute("state", googleHelper.getStateToken());

		try {
			externalContext.redirect(googleHelper.buildLoginUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param event
	 */
	public void checkGoogleUser(PhaseEvent event) {

		FacesMessage message = new FacesMessage();

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		HttpSession session = (HttpSession) externalContext.getSession(false);
		// TEST
		// String code=request.getParameter("code");
		// String state = request.getParameter("state");

		if (request.getParameter("code") == null || request.getParameter("state") == null) {
			String error[] = request.getParameterValues("error");

			// 1st case:checking if there was an error such as the user denied
			// access
			if (error != null && error.length > 0) {
				registeredGoogle = false;
			}

			return;
		} else if (request.getParameter("code") != null && request.getParameter("state") != null
				&& request.getParameter("state").equals(session.getAttribute("state"))) {

			// registeredGoogle = true;

			try {
				// check if token is null
				// if(token == null){

				// get access token
				token = googleHelper.getToken(request.getParameter("code"));

				// check if token is valid, has expired etc
				if (googleHelper.validateToken(token)) {

					userInfo = googleHelper.getUserInfoJson();

					JSONObject json = new JSONObject(userInfo);

					String email = json.getString("email");
					String firstname = json.getString("given_name");
					String lastname = json.getString("family_name");

					boolean verified = json.getBoolean("email_verified");

					// registration with Google
					if (verified && userManagementService.getUser(email) == null) {
						Date date = new Date();
						if (email.length() > 0 && firstname.length() > 0 && lastname.length() > 0) {
							editUser = new User();
							editUser.setEmail(email);
							editUser.setFirstname(firstname);
							editUser.setLastname(lastname);
							editUser.setCreationDate(date);
							editUser.setAccountType(User.GOOGLE_ACCOUNT);
							editUser.setStatus(User.STATUS_VERIFIED);

							userManagementService.saveUser(editUser);

							// message.setSeverity(FacesMessage.SEVERITY_INFO);
							// message.setSummary(show.getString("regGoogleSucc"));
							// FacesContext.getCurrentInstance().addMessage("regGoogleForm:infoReg",
							// message);

							session.removeAttribute("state");
							user = editUser;
							externalContext.redirect(properties.getString("mainUrl"));
						} else {
							message.setSeverity(FacesMessage.SEVERITY_INFO);
							message.setSummary(show.getString("regGoogleFailEmpty"));
							FacesContext.getCurrentInstance().addMessage("regGoogleForm:infoReg", message);
							return;
						}
					}
					// login with Google
					else if (verified && userManagementService.getUser(email) != null) {
						user = userManagementService.getUser(email);
						session.removeAttribute("state");
						externalContext.redirect(properties.getString("mainUrl"));
					}
					// not verified-error
					else {
						message.setSeverity(FacesMessage.SEVERITY_INFO);
						message.setSummary(show.getString("regGoogleFail"));
						FacesContext.getCurrentInstance().addMessage("regGoogleForm:infoReg", message);

						return;
					}
				}// token is valid
				else {
					message.setSeverity(FacesMessage.SEVERITY_INFO);
					message.setSummary(show.getString("notValidToken"));
					FacesContext.getCurrentInstance().addMessage("regGoogleForm:infoReg", message);

					return;
				}
				// }//token is null
				// token exists
				/*
				 * else{ //check if token is valid
				 * if(googleHelper.validateToken(token)){
				 * 
				 * String userInfo = googleHelper.getUserInfoJson(); JSONObject
				 * json = new JSONObject(userInfo);
				 * 
				 * String email = json.getString("email"); //check if user with
				 * this email exists in DB
				 * if(userManagementService.getUser(email)!=null){
				 * message.setSeverity(FacesMessage.SEVERITY_INFO);
				 * message.setSummary(show.getString("regGoogleFail"));
				 * FacesContext
				 * .getCurrentInstance().addMessage("regGoogleForm:infoReg",
				 * message); } else{ token = null;
				 * message.setSeverity(FacesMessage.SEVERITY_INFO);
				 * message.setSummary(show.getString("regGoogleAgain"));
				 * FacesContext
				 * .getCurrentInstance().addMessage("regGoogleForm:infoReg",
				 * message); }
				 * 
				 * } else{ message.setSeverity(FacesMessage.SEVERITY_INFO);
				 * message.setSummary(show.getString("notValidToken"));
				 * FacesContext
				 * .getCurrentInstance().addMessage("regGoogleForm:infoReg",
				 * message); } }
				 */
			} catch (Exception e) {
				e.printStackTrace();
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				message.setSummary(show.getString("internalError"));
				FacesContext.getCurrentInstance().addMessage("regGoogleForm:errorReg", message);
				return;
			}
		} else {
			message.setSeverity(FacesMessage.SEVERITY_INFO);
			message.setSummary(show.getString("regAlready"));
			FacesContext.getCurrentInstance().addMessage("regGoogleForm:infoReg", message);
			return;
		}
	}

	/******************
	 * FACEBOOK Actions
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws JSONException
	 ******************/

	public void checkFacebookUser(PhaseEvent event) throws JSONException {

		FacesMessage message = new FacesMessage();
		String accessToken;
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		HttpSession session = (HttpSession) externalContext.getSession(false);

		if (request.getParameter("code") == null || request.getParameter("state") == null) {
			String error[] = request.getParameterValues("error_message");

			// 1st case:checking if there was an error such as the user denied
			// access
			if (error != null && error.length > 0) {
				registeredFacebook = false;
			}

			// 2nd case:first time user is prompted to be authorized by facebook
			else {
				session.setAttribute("state", session.getId());
				String fbUrl = "http://www.facebook.com/dialog/oauth?" + "client_id="
						+ properties.getString("facebook.clientId") + "&redirect_uri="
						+ properties.getString("appServer") + properties.getString("facebook.register") + "&scope="
						+ properties.getString("facebook.scope") + "&state=" + session.getId();
				try {
					externalContext.redirect(fbUrl);
				} catch (Exception e) {
					e.printStackTrace();
					message.setSeverity(FacesMessage.SEVERITY_ERROR);
					message.setSummary(show.getString("internalError"));
					FacesContext.getCurrentInstance().addMessage("regFacebookForm:errorReg", message);
					return;
				}
			}
		}

		else if (request.getParameter("code") != null && request.getParameter("state") != null
				&& request.getParameter("state").equals(session.getId())) {

			registeredFacebook = true;
			String code = request.getParameter("code");
			accessToken = fbHelper.getFacebookAccessToken(code);
			try {
				userInfo = fbHelper.getUserInfoJson(accessToken);
				JSONObject json = new JSONObject(userInfo);
				// registration
				String firstname = json.getString("first_name");
				String lastname = json.getString("last_name");
				String email = json.getString("email");

				// Check if user email exists already
				if (userManagementService.getUser(email) == null) {
					Date date = new Date();
					if (email.length() > 0 && firstname.length() > 0 && lastname.length() > 0) {
						editUser = new User();
						editUser.setEmail(email);
						editUser.setFirstname(firstname);
						editUser.setLastname(lastname);
						editUser.setCreationDate(date);
						editUser.setAccountType(User.FACEBOOK_ACCOUNT);
						editUser.setStatus(User.STATUS_VERIFIED);

						userManagementService.saveUser(editUser);

						message.setSeverity(FacesMessage.SEVERITY_INFO);
						message.setSummary("Successfull Registration via Facebook");
						FacesContext.getCurrentInstance().addMessage("regFacebookForm:infoReg", message);

						session.removeAttribute("state");
					}
					// Authentication failure
					else {
						message.setSeverity(FacesMessage.SEVERITY_INFO);
						message.setSummary("EMPTY");
						FacesContext.getCurrentInstance().addMessage("regFacebookForm:infoReg", message);
					}
				}
				// User already exists in the database
				else if (userManagementService.getUser(email) != null) {
					message.setSeverity(FacesMessage.SEVERITY_INFO);
					message.setSummary("EXISTS");
					FacesContext.getCurrentInstance().addMessage("regFacebookForm:infoReg", message);

				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				message.setSummary(show.getString("internalError"));
				FacesContext.getCurrentInstance().addMessage("regFacebookForm:errorReg", message);

			} catch (IOException e) {
				e.printStackTrace();
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				message.setSummary(show.getString("internalError"));
				FacesContext.getCurrentInstance().addMessage("regFacebookForm:errorReg", message);

			}

		} else {
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			message.setSummary("Either 'code' is null, 'state' is null, or the sessionId doesn't match!!");
			FacesContext.getCurrentInstance().addMessage("regFacebookForm:errorReg", message);
		}
	}

	/**************** Getters-Setters ******************/

	public UserManagementService getUserManagementService() {

		return userManagementService;
	}

	public void setUserManagementService(UserManagementService userManagementService) {

		this.userManagementService = userManagementService;
	}

	public PropertyResourceBundle getShow() {

		return show;
	}

	public void setShow(PropertyResourceBundle show) {

		this.show = show;
	}

	public CredentialsValidator getEmailValidator() {

		return validator;
	}

	public void setEmailValidator(CredentialsValidator emailValidator) {

		this.validator = emailValidator;
	}

	public String getconfRegPwd() {

		return confRegisterPassword;
	}

	public void setconfRegPwd(String confRegPwd) {

		this.confRegisterPassword = confRegPwd;
	}

	public CredentialsValidator getValidator() {

		return validator;
	}

	public void setValidator(CredentialsValidator validator) {

		this.validator = validator;
	}

	public String getRegisterPassword() {

		return registerPassword;
	}

	public void setRegisterPassword(String registerPassword) {

		this.registerPassword = registerPassword;
	}

	public User getEditUser() {

		return editUser;
	}

	public void setEditUser(User editUser) {

		this.editUser = editUser;
	}

	public String getPassEmail() {

		return passEmail;
	}

	public void setPassEmail(String passEmail) {

		this.passEmail = passEmail;
	}

	public PropertyResourceBundle getProperties() {

		return properties;
	}

	public void setProperties(PropertyResourceBundle properties) {

		this.properties = properties;
	}

	public User getUser() {

		return user;
	}

	public void setUser(User user) {

		this.user = user;
	}

	public String getPassword() {

		return password;
	}

	public void setPassword(String password) {

		this.password = password;
	}

	public String getConfNewPassword() {

		return confNewPassword;
	}

	public void setConfNewPassword(String confNewPassword) {

		this.confNewPassword = confNewPassword;
	}

	public String getNewPassword() {

		return newPassword;
	}

	public void setNewPassword(String newPassword) {

		this.newPassword = newPassword;
	}

	public GoogleAuthHelper getGoogleHelper() {

		return googleHelper;
	}

	public void setGoogleHelper(GoogleAuthHelper helper) {

		this.googleHelper = helper;
	}

	public boolean isRegisteredGoogle() {

		return registeredGoogle;
	}

	public void setRegisteredGoogle(boolean registeredGoogle) {

		this.registeredGoogle = registeredGoogle;
	}

	public String getUserInfo() {

		return userInfo;
	}

	public void setUserInfo(String userInfo) {

		this.userInfo = userInfo;
	}

	public String getConfRegisterPassword() {

		return confRegisterPassword;
	}

	public void setConfRegisterPassword(String confRegisterPassword) {

		this.confRegisterPassword = confRegisterPassword;
	}

	public GoogleTokenResponse getToken() {

		return token;
	}

	public void setToken(GoogleTokenResponse token) {

		this.token = token;
	}

	public boolean isRegisteredFacebook() {

		return registeredFacebook;
	}

	public void setRegisteredFacebook(boolean registeredFacebook) {

		this.registeredFacebook = registeredFacebook;
	}

	public String getReturnURL() {

		return returnURL;
	}

	public void setReturnURL(String returnURL) {

		this.returnURL = returnURL;
	}

	public PasswordUtil getPasswordUtil() {

		return passwordUtil;
	}

	public void setPasswordUtil(PasswordUtil passwordUtil) {

		this.passwordUtil = passwordUtil;
	}

	public String getSuccessMessage() {

		return successMessage;
	}

	public void setSuccessMessage(String successMessage) {

		this.successMessage = successMessage;
	}

	public String getResetPassword() {

		return resetPassword;
	}

	public void setResetPassword(String resetPassword) {

		this.resetPassword = resetPassword;
	}

	public String getConfResetPassword() {

		return confResetPassword;
	}

	public void setConfResetPassword(String confResetPassword) {

		this.confResetPassword = confResetPassword;
	}

	public String getResetStatus() {

		return resetStatus;
	}

	public void setResetStatus(String resetStatus) {

		this.resetStatus = resetStatus;
	}
}
