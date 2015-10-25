package gr.cyberstream.auth.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseEvent;

import org.richfaces.component.UIExtendedDataTable;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;

import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;
import gr.cyberstream.auth.service.UserManagementService;
import gr.cyberstream.auth.util.CredentialsValidator;
import gr.cyberstream.auth.util.PasswordUtil;

@SessionScoped
@ManagedBean(name = "userManagementBackend")
public class UserManagementBackend {

	@ManagedProperty(value = "#{userManagementService}")
	private UserManagementService userManagementService;
	
	@ManagedProperty(value = "#{passwordUtil}")
	private PasswordUtil passwordUtil;

	private PropertyResourceBundle show;
	private PropertyResourceBundle properties;
	
	private CredentialsValidator validator;
	
	private User user;
	private Role role;
	
	private List<User> allUsersList;
	private Collection<Object> allUsersSelection;
	private List<User> bannedUsersList;
	private Collection<Object> bannedUsersSelection;
	private List<User> disabledUsersList;
	private Collection<Object> disabledUsersSelection;
	
	private List<Role> rolesList;
	private Collection<Object> rolesSelection;
	
	private List<User> roleMembersList;
	private Collection<Object> membersSelection;
	
	private List<Role> selectedRoles;
	
	private boolean messagesExist;
	private boolean edit;
	
	private String keyword;
		
	public UserManagementBackend() {

		properties = (PropertyResourceBundle) ResourceBundle.getBundle("auth");
		show = (PropertyResourceBundle) ResourceBundle.getBundle("gr.cyberstream.auth.resources.show", FacesContext
				.getCurrentInstance().getViewRoot().getLocale());
		
		validator = new CredentialsValidator();
		
		user = new User();

		allUsersSelection = new HashSet<Object>();
		bannedUsersSelection = new HashSet<Object>();
		disabledUsersSelection = new HashSet<Object>();
		
		rolesSelection = new HashSet<Object>();
		membersSelection = new HashSet<Object>();
	}
	
	public void roleSelectionListener(AjaxBehaviorEvent event) {

		UIExtendedDataTable rolesTable = (UIExtendedDataTable) event.getComponent();
		Object originalKey = rolesTable.getRowKey();
		rolesTable.setRowKey(rolesSelection.iterator().next());

		if (rolesTable.isRowAvailable()) {
			role = (Role) rolesTable.getRowData();
			roleMembersList = userManagementService.getRoleMembers(role);
		}
		
		rolesTable.setRowKey(originalKey);
	}
	
	public void searchUsers() {
		
		allUsersList = userManagementService.searchUsers(keyword, User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
	}

	public void clearSearchUsers() {
		
		keyword = null;
		allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
	}
	
	public void addUser() {
		
		edit = false;
		user = new User();
		
		rolesList = userManagementService.getRoles();
		selectedRoles = new ArrayList<Role>();
	}
	
	public void editUser() {
		
		edit = true;
		
		if (allUsersSelection.size() > 0) {
			
			String email = allUsersList.get((Integer) allUsersSelection.iterator().next()).getEmail();
			user = userManagementService.getUser(email);
			
		} else {
			
			user = new User();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(show.getString("error_select_user")));
		}
		
		rolesList = userManagementService.getRoles();
		selectedRoles = userManagementService.getUserRoles(user);
	}
	
	public void saveUser() {
		
		boolean valid = true;
		
		if (user.getFirstname().length() == 0) {

			valid = false;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(show.getString("error_firstname_empty")));
		}

		if (user.getLastname().length() == 0) {

			valid = false;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(show.getString("error_lastname_empty")));
		}

		if (user.getEmail().length() == 0) {

			valid = false;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(show.getString("error_email_empty")));

		} else if (!validator.mailValidate(user.getEmail())) {

			valid = false;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(show.getString("error_email_invalid")));

		}
		
		if (!valid) {
			messagesExist = true;
			return;
		}
		
		if (edit) {
			
			userManagementService.updateUser(user);
			
			for (Role role : rolesList) {
				
				if (selectedRoles.contains(role)) {
					
					userManagementService.addUserRole(user, role.getName());
					
				} else {
					
					userManagementService.removeUserRole(user, role);
				}
			}
			
		} else {
		
			String password = generatePassword(8);
			
			user.setAccountType(User.SIMPLE_ACCOUNT);
			user.setPassword(passwordUtil.encode(password));
			user.setCreationDate(new Date());
			
			user.setStatus(User.STATUS_VERIFIED);
	
			if (!userManagementService.createUser(user, password)) {
				
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(show.getString("error_email_exists")));
			} else {
				
				for (Role role : selectedRoles) {
					
					userManagementService.addUserRole(user, role.getName());
				}
			}
		}
		
		allUsersList = userManagementService.getUsers();
	}
	
	public void approveUsers() {

		for (Object selection : allUsersSelection) {

			int index = (Integer) selection;
			User user = allUsersList.get(index);
			
			if (user.getStatus().equals(User.STATUS_MODERATED))
				userManagementService.approveUser(user);
		}

		allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
		allUsersSelection.clear();
	}
	
	public void resendVerify() {

		for (Object selection : allUsersSelection) {

			int index = (Integer) selection;
			User user = allUsersList.get(index);
			
			if (user.getStatus().equals(User.STATUS_PENDING))
				userManagementService.approveUser(user);
		}

		allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
		allUsersSelection.clear();
	}
	
	public void banUsers() {

		for (Object selection : allUsersSelection) {

			int index = (Integer) selection;
			User user = allUsersList.get(index);
			
			user.setStatus(User.STATUS_BANNED);
			userManagementService.updateUser(user);
		}
		
		allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
		allUsersSelection.clear();

		bannedUsersList = userManagementService.getUsers(User.STATUS_BANNED);
		bannedUsersSelection.clear();
	}
	
	public void unbanUsers() {

		for (Object selection : bannedUsersSelection) {

			int index = (Integer) selection;
			User user = bannedUsersList.get(index);
			
			userManagementService.approveUser(user);
		}

		allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
		allUsersSelection.clear();
		
		bannedUsersList = userManagementService.getUsers(User.STATUS_BANNED);
		bannedUsersSelection.clear();
	}
	
	public void disableUsers() {

		for (Object selection : allUsersSelection) {

			int index = (Integer) selection;
			User user = allUsersList.get(index);
			
			user.setStatus(User.STATUS_DISABLED);
			userManagementService.updateUser(user);
		}

		allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
		allUsersSelection.clear();
		
		disabledUsersList = userManagementService.getUsers(User.STATUS_DISABLED);
		disabledUsersSelection.clear();
	}
	
	public void disableBannedUsers() {

		for (Object selection : bannedUsersSelection) {

			int index = (Integer) selection;
			User user = bannedUsersList.get(index);
			
			user.setStatus(User.STATUS_DISABLED);
			userManagementService.updateUser(user);
		}

		bannedUsersList = userManagementService.getUsers(User.STATUS_BANNED);
		bannedUsersSelection.clear();
		
		disabledUsersList = userManagementService.getUsers(User.STATUS_DISABLED);
		disabledUsersSelection.clear();
	}
	
	public void enableUsers() {

		for (Object selection : disabledUsersSelection) {

			int index = (Integer) selection;
			User user = disabledUsersList.get(index);
			
			userManagementService.approveUser(user);
		}

		allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
		allUsersSelection.clear();
		
		disabledUsersList = userManagementService.getUsers(User.STATUS_DISABLED);
		disabledUsersSelection.clear();
	}
	
	public void removeUsers() {

		for (Object selection : allUsersSelection) {

			int index = (Integer) selection;

			userManagementService.removeUser(allUsersList.get(index));
		}

		allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
		allUsersSelection.clear();
	}
	
	public void removeBannedUsers() {

		for (Object selection : bannedUsersSelection) {

			int index = (Integer) selection;
			User user = bannedUsersList.get(index);
			
			userManagementService.removeUser(user);
		}

		bannedUsersList = userManagementService.getUsers(User.STATUS_BANNED);
		bannedUsersSelection.clear();
	}
	
	public void removeDisabledUsers() {

		for (Object selection : disabledUsersSelection) {

			int index = (Integer) selection;
			User user = disabledUsersList.get(index);
			
			userManagementService.removeUser(user);
		}

		disabledUsersList = userManagementService.getUsers(User.STATUS_DISABLED);
		disabledUsersSelection.clear();
	}
	
	public void addRole() {
		
		role = new Role();
	}
	
	public void saveRole() {
		
		boolean valid = true;
		
		if (role.getName().length() == 0) {

			valid = false;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(show.getString("error_name_empty")));
		}

		if (role.getDescription().length() == 0) {

			valid = false;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(show.getString("error_desc_empty")));
		}

		if (!valid) {
			messagesExist = true;
			return;
		}
		
		userManagementService.saveRole(role);
		
		rolesList = userManagementService.getRoles();
	}
	
	public void removeRoles() {
		
		for (Object selection : rolesSelection) {

			int index = (Integer) selection;

			userManagementService.removeRole(rolesList.get(index));
		}

		rolesList = userManagementService.getRoles();
		rolesSelection.clear();
	}
	
	private String generatePassword(int length) {
		
		RandomValueStringGenerator randomPasswordGenerator = new RandomValueStringGenerator(length);
		return randomPasswordGenerator.generate();
	}
	
	// ---- Navigation ---- //
	
	public void initUsers(PhaseEvent phaseEvent) {
		
		if (allUsersList == null)
			allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
		
		if (bannedUsersList == null)
			bannedUsersList = userManagementService.getUsers(User.STATUS_BANNED);
		
		if (disabledUsersList == null)
			disabledUsersList = userManagementService.getUsers(User.STATUS_DISABLED);
	}
	
	public String users() {

		allUsersList = userManagementService.getUsers(User.STATUS_PENDING, User.STATUS_MODERATED, User.STATUS_VERIFIED);
		bannedUsersList = userManagementService.getUsers(User.STATUS_BANNED);
		disabledUsersList = userManagementService.getUsers(User.STATUS_DISABLED);

		return "/admin/users";
	}
	
	public void initRoles(PhaseEvent phaseEvent) {
		
		if (rolesList == null)
			rolesList = userManagementService.getRoles();
	}

	public String roles() {

		rolesList = userManagementService.getRoles();

		return "/admin/roles";
	}
	
	// ---- Getters Setters ---- //

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

	public List<User> getAllUsersList() {
	
		return allUsersList;
	}

	public void setAllUsersList(List<User> allUsersList) {
	
		this.allUsersList = allUsersList;
	}

	public Collection<Object> getAllUsersSelection() {
	
		return allUsersSelection;
	}

	public void setAllUsersSelection(Collection<Object> allUsersSelection) {
	
		this.allUsersSelection = allUsersSelection;
	}

	public boolean isMessagesExist() {
	
		return messagesExist;
	}

	public void setMessagesExist(boolean messagesExist) {
	
		this.messagesExist = messagesExist;
	}

	public PasswordUtil getPasswordUtil() {
	
		return passwordUtil;
	}

	public void setPasswordUtil(PasswordUtil passwordUtil) {
	
		this.passwordUtil = passwordUtil;
	}

	public List<Role> getRolesList() {
	
		return rolesList;
	}

	public void setRolesList(List<Role> rolesList) {
	
		this.rolesList = rolesList;
	}

	public Collection<Object> getRolesSelection() {
	
		return rolesSelection;
	}

	public void setRolesSelection(Collection<Object> rolesSelection) {
	
		this.rolesSelection = rolesSelection;
	}

	public boolean isEdit() {
	
		return edit;
	}

	public void setEdit(boolean edit) {
	
		this.edit = edit;
	}

	public List<User> getBannedUsersList() {
	
		return bannedUsersList;
	}

	public void setBannedUsersList(List<User> bannedUsersList) {
	
		this.bannedUsersList = bannedUsersList;
	}

	public Collection<Object> getBannedUsersSelection() {
	
		return bannedUsersSelection;
	}

	public void setBannedUsersSelection(Collection<Object> bannedUsersSelection) {
	
		this.bannedUsersSelection = bannedUsersSelection;
	}

	public List<User> getDisabledUsersList() {
	
		return disabledUsersList;
	}

	public void setDisabledUsersList(List<User> disabledUsersList) {
	
		this.disabledUsersList = disabledUsersList;
	}

	public Collection<Object> getDisabledUsersSelection() {
	
		return disabledUsersSelection;
	}

	public void setDisabledUsersSelection(Collection<Object> disabledUsersSelection) {
	
		this.disabledUsersSelection = disabledUsersSelection;
	}

	public Collection<Object> getMembersSelection() {
	
		return membersSelection;
	}

	public void setMembersSelection(Collection<Object> membersSelection) {
	
		this.membersSelection = membersSelection;
	}

	public Role getRole() {
	
		return role;
	}

	public void setRole(Role role) {
	
		this.role = role;
	}

	public List<User> getRoleMembersList() {
	
		return roleMembersList;
	}

	public void setRoleMembersList(List<User> roleMembersList) {
	
		this.roleMembersList = roleMembersList;
	}

	public List<Role> getSelectedRoles() {
	
		return selectedRoles;
	}

	public void setSelectedRoles(List<Role> selectedRoles) {
	
		this.selectedRoles = selectedRoles;
	}

	public String getKeyword() {
	
		return keyword;
	}

	public void setKeyword(String keyword) {
	
		this.keyword = keyword;
	}
}
