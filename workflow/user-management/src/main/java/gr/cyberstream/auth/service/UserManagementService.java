package gr.cyberstream.auth.service;

import java.util.List;

import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;

public interface UserManagementService {

	public boolean createUser(User user, String password);
	public void approveUser(User user);
	
	public boolean resetPassword(User user);
	
	public boolean comparePassword(User user, String password);
	
	public void saveUser(User user);
	public void updateUser(User user);
	public User getUser(String email);
	public User getUserByKey(String key);
	
	public void removeUser(User user);
	
	public List<User> searchUsers(String keyword, String... statusArray);
	public List<User> getUsers(String... status);
	
	public List<Role> getRoles();
	public Role getRole(String name);
	public List<User> getRoleMembers(Role role);
	
	public List<Role> getUserRoles(User user);
	
	public void saveRole(Role role);
	public void removeRole(Role role);
	
	public void addUserRole(User user, String roleName);
	public void removeUserRole(User user, Role role);
	
	public void sendMail(String from, String to, String subject, String content);
}
