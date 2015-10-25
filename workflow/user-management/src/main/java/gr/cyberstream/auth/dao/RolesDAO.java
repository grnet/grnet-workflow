package gr.cyberstream.auth.dao;

import java.util.List;

import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;

public interface RolesDAO {

	public void saveRole(Role role);
	public void removeRole(Role role);
	
	public Role getRole(String name);
	
	public List<Role> getRoles();
	public List<Role> getUserRoles(User user);
	
	public void addUserRole(User user, Role role);
	public void addUserRole(User user, String roleName);
	
	public void removeUserRole(User user, Role role);
	public void removeRoles(User user);
}
