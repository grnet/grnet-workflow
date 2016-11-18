package gr.cyberstream.workflow.engine.service;

import java.util.List;

import gr.cyberstream.workflow.engine.model.api.WfUser;

public interface RealmService {

	/**
	 * Returns user
	 * 
	 * @param id
	 *            User's id
	 * 
	 * @return {@link WfUser}
	 */
	public WfUser getUser(String id);

	/**
	 * Return users by role
	 * 
	 * @param role
	 *            Users role
	 * 
	 * @return List of {@link WfUser}
	 */
	public List<WfUser> getUsersByRole(String role);

	/**
	 * Returns all available users
	 * 
	 * @return List of {@link WfUser}
	 */
	public List<WfUser> getAllUsers();

	/**
	 * Returns users by group and role
	 * 
	 * @param groupName
	 *            Users group
	 * 
	 * @param role
	 *            Users role
	 * 
	 * @return List of {@link WfUser}
	 */
	public List<WfUser> getUsersByGroupAndRole(String groupName, String role);

	/**
	 * Returns users by group
	 * 
	 * @param groupName
	 *            Users role
	 * 
	 * @return List of {@link WfUser}
	 */
	public List<WfUser> getUsersByGroup(String groupName);

	/**
	 * Returns all available groups
	 * 
	 * @return List of {@link String}
	 */
	public List<String> getRealmGroups();

	/**
	 * Returns groups of the logged in user
	 * 
	 * @return List of {@link String}
	 */
	public List<String> getUserGroups();

}
