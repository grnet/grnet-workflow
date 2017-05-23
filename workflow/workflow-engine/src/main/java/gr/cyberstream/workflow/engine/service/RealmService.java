package gr.cyberstream.workflow.engine.service;

import java.util.List;

import gr.cyberstream.workflow.engine.model.Owner;
import gr.cyberstream.workflow.engine.model.Role;
import gr.cyberstream.workflow.engine.model.api.WfOwner;
import gr.cyberstream.workflow.engine.model.api.WfRole;
import gr.cyberstream.workflow.engine.model.api.WfUser;

public interface RealmService {

	/**
	 * Returns user by its id
	 * 
	 * @param id
	 *            User's id
	 * 
	 * @return {@link WfUser}
	 */
	public WfUser getUser(String id);

	/**
	 * Returns a list of users by role
	 * 
	 * @param role
	 *            Role which users will be returned
	 * 
	 * @return List of {@link WfUser}
	 */
	public List<WfUser> getUsersByRole(String role);

	/**
	 * Returns all available users from keycloak
	 * 
	 * @return List of {@link WfUser}
	 */
	public List<WfUser> getAllUsers();

	/**
	 * Returns users by role and group
	 * 
	 * @param groupName
	 *            Group's name
	 * 
	 * @param roleName
	 *            Role's name
	 * 
	 * @return List of {@link WfUser}
	 */
	public List<WfUser> getUsersByGroupAndRole(String groupName, String roleName);

	/**
	 * Returns users by its group
	 * 
	 * @param groupName
	 *            Group's which users will be returned
	 * 
	 * @return List of {@link WfUser}
	 */
	public List<WfUser> getUsersByGroup(String groupName);

	/**
	 * Returns all available groups which are saved in db (not keycloak)
	 * 
	 * @return List of {@link WfOwner}
	 */
	public List<WfOwner> getRealmGroups();

	/**
	 * Returns logged in user's groups
	 * 
	 * @return List of {@link String}
	 */
	public List<String> getUserGroups();

	/**
	 * Returns logged in user's groups. Used by api communication
	 * 
	 * @return List of {@link WfOwner}
	 */
	public List<WfOwner> getUserOwnership();

	/**
	 * Deletes owner/group from keycloak
	 * 
	 * @param ownerId
	 *            Owner's/group's id to be deleted
	 * 
	 * @throws InvalidRequestException
	 */
	public void deleteOwner(String ownerId);

	/**
	 * Create or update a group/owner
	 * 
	 * @param wfOwner
	 *            The owner to be created or updated
	 * 
	 * @return The saved entity
	 * @throws InvalidRequestException
	 */
	public Owner saveOwner(WfOwner wfOwner) throws InvalidRequestException;

	/**
	 * Returns all available roles which are saved in db (not keycloak)
	 * 
	 * @return List of {@link WfRole}
	 */
	public List<WfRole> getRoles();

	/**
	 * Save or update role
	 * 
	 * @param wfRole
	 *            The entity to be created/updated
	 * 
	 * @return {@link Role} The saved entity
	 * @throws InvalidRequestException
	 */
	public Role saveRole(WfRole wfRole) throws InvalidRequestException;

	/**
	 * Deletes a role by its id
	 * 
	 * @param roleId
	 *            Role's id to be deleted
	 */
	public void deleteRole(String roleId);

	/**
	 * Returns a list that contains roles from keycloak, that are NOT present in
	 * the roles table.
	 * 
	 * @return
	 */
	public List<WfRole> synchronizeRoles();

	/**
	 * Returns a list that containts owners/groups from keycloak, that are NOT
	 * present in the owners table.
	 * 
	 * @return
	 */
	public List<WfOwner> synchronizeOwners();

	/**
	 * 
	 * @param owners
	 */
	public void importOwners(List<WfOwner> owners);

	/**
	 * 
	 * @param roles
	 */
	public void importRoles(List<WfRole> roles);

}
