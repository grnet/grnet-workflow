package gr.cyberstream.auth.dao;

import java.util.List;

import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;

public interface UsersDAO {

	public boolean comparePassword(User user, String password);
	
	public void saveUser(User user);
	public void updateUser(User user);
	public void removeUser(User user);
	
	public User getUser(String email);
	public User getUserByKey(String key);
	
	public List<User> searchUsers(String keyword, String... statusArray);
	
	public List<User> getUsers(String... statusArray);
		
	public List<User> getRoleMembers(Role role);
}
