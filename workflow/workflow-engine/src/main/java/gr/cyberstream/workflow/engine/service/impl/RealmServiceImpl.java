package gr.cyberstream.workflow.engine.service.impl;

import gr.cyberstream.workflow.engine.model.Owner;
import gr.cyberstream.workflow.engine.model.Role;
import gr.cyberstream.workflow.engine.model.api.WfOwner;
import gr.cyberstream.workflow.engine.model.api.WfRole;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.RealmService;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author gtyl
 *
 */
@Service
public class RealmServiceImpl implements RealmService {

	@Inject
	private Processes processRepository;
	
	@Inject
	private Environment environment;

	private static final Logger logger = LoggerFactory.getLogger(RealmServiceImpl.class);

	private static String keycloakServer;
	private static String keycloakRealm;
	private static String keycloakUser;
	private static String keycloakPassword;
	private static RealmResource keycloak;
	
	@PostConstruct
	public void initializeKeycloak() {
		keycloakServer = environment.getProperty("keycloak.server");
		keycloakRealm = environment.getProperty("keycloak.realm");
		keycloakUser = environment.getProperty("keycloak.user");
		keycloakPassword = environment.getProperty("keycloak.password");

		keycloak = Keycloak.getInstance(keycloakServer, keycloakRealm, keycloakUser, keycloakPassword, "admin-cli").realm(keycloakRealm);
	}
	
	@Override
	public WfUser getUser(String userId) {
		WfUser wfUser = new WfUser();
		
		UserResource userResource = keycloak.users().get(userId);
		
		UserRepresentation userRepresentation = userResource.toRepresentation();
		
		List<String> userRoles = new ArrayList<>();
		List<String> userGroups = new ArrayList<>();
		
		List<RoleRepresentation> roles = userResource.roles().getAll().getRealmMappings();
		List<GroupRepresentation> groups = userResource.groups();

		if (roles != null && roles.size() > 0) {
			for (RoleRepresentation userRole : roles) {
				userRoles.add(userRole.getName());
			}
		}

		if (groups != null && groups.size() > 0) {
			for (GroupRepresentation userGroup : groups) {
				userGroups.add(userGroup.getName());
			}
		}

		wfUser.setEmail(userRepresentation.getEmail());
		wfUser.setFirstName(userRepresentation.getFirstName());
		wfUser.setLastName(userRepresentation.getLastName());
		wfUser.setId(userRepresentation.getId());
		wfUser.setUsername(userRepresentation.getUsername());
		wfUser.setUserRoles(userRoles);
		wfUser.setGroups(userGroups);
		
		return wfUser;
	}
	
	@Override
	public List<WfUser> getUsersByRole(String role) {
		RestTemplate restTemplate = new RestTemplate();
		List<WfUser> users = new ArrayList<WfUser>();

		try {
			String token = getToken();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + token);

			HttpEntity<String> entity = new HttpEntity<String>(headers);

			URI uri = KeycloakUriBuilder.fromUri(keycloakServer + "/admin/realms/{realm}/users").build(keycloakRealm);

			ResponseEntity<UserRepresentation[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, UserRepresentation[].class);

			for (UserRepresentation user : response.getBody()) {
				List<String> roles = getUserRoles(restTemplate, headers, user.getId());
				
				if (roles.contains(role)) {
					WfUser wfUser = new WfUser();
					wfUser.setId(user.getId());
					wfUser.setUsername(user.getUsername());
					wfUser.setFirstName(user.getFirstName());
					wfUser.setLastName(user.getLastName());
					wfUser.setEmail(user.getEmail());
					wfUser.setGroups(user.getGroups());
					wfUser.setUserRoles(roles);
					
					users.add(wfUser);
				}
			}

		} catch (HttpClientErrorException e) {

			logger.error("GET '/admin/realms/{realm}/users' failed: " + e.getMessage());
		}
		return users;
	}

	@Override
	public List<WfUser> getAllUsers() {
		List<WfUser> returnList = new ArrayList<WfUser>();
		// UserRepresentation doesn't return groups/roles a workaround is to
		// request for every user for its groups/roles
		List<UserRepresentation> users = keycloak.users().search("", 0, -1);
		
		for(UserRepresentation user : users) {
			WfUser wfUser = getUser(user.getId());
			
			returnList.add(wfUser);
		}
		return returnList;
	}

	@Override
	public List<WfUser> getUsersByGroupAndRole(String groupName, String role) {
		
		List<WfUser> returnList = new ArrayList<>();
		List<WfUser> groupUsers = getUsersByGroup(groupName);
		
		for(WfUser wfUser: groupUsers) {
			if(wfUser.getUserRoles().contains(role)) {
				returnList.add(wfUser);
			}
		}
		
		return returnList;
	}
	/*
	@Override
	public List<WfUser> getUsersByGroupAndRole(String groupName, String role) {
		RestTemplate restTemplate = new RestTemplate();
		List<WfUser> users = new ArrayList<WfUser>();

		try {
			String token = getToken();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + token);

			HttpEntity<String> entity = new HttpEntity<String>(headers);

			URI groupURI = KeycloakUriBuilder.fromUri(keycloakServer + "/admin/realms/{realm}/group-by-path/{path}").build(keycloakRealm, groupName);

			ResponseEntity<GroupRepresentation> groupResponse = restTemplate.exchange(groupURI, HttpMethod.GET, entity, GroupRepresentation.class);

			GroupRepresentation group = groupResponse.getBody();

			// for keycloak version 1.8.1.Final
			URI uri = KeycloakUriBuilder
					.fromUri(keycloakServer + "/admin/realms/{realm}/groups/{id}/members?first=0&max=10&")
					.build(keycloakRealm, group.getId());

			// for keycloak version 1.8.1.Final and later
			// URI uri = KeycloakUriBuilder.fromUri(keycloakServer +
			// "/admin/realms/{realm}/groups/{id}/members").build(keycloakRealm,
			// group.getId());

			ResponseEntity<UserRepresentation[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, UserRepresentation[].class);

			for (UserRepresentation user : response.getBody()) {

				List<String> roles = getUserRoles(restTemplate, headers, user.getId());

				if (roles.contains(role))
					users.add(new WfUser(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
							user.getEmail(), user.getGroups(), roles));

			}
		} catch (HttpClientErrorException e) {
			logger.error("GET '/admin/realms/{realm}/groups/{id}/members' failed: " + e.getMessage());
		}

		return users;
	}
	*/

	@Override
	public List<WfUser> getUsersByGroup(String groupName) {
		List<WfUser> returnList = new ArrayList<>();
		
		GroupRepresentation group = keycloak.getGroupByPath(groupName);
		List<UserRepresentation> users = keycloak.groups().group(group.getId()).members(0, 999);

		for (UserRepresentation user : users) {
			returnList.add(getUser(user.getId()));
		}

		return returnList;
	}
	
	/*
	@Override
	public List<WfUser> getUsersByGroup(String groupName) {
		RestTemplate restTemplate = new RestTemplate();
		List<WfUser> users = new ArrayList<WfUser>();

		try {

			String token = getToken();

			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + token);

			HttpEntity<String> entity = new HttpEntity<String>(headers);

			URI groupURI = KeycloakUriBuilder.fromUri(keycloakServer + "/admin/realms/{realm}/group-by-path/{path}")
					.build(keycloakRealm, groupName);

			ResponseEntity<GroupRepresentation> groupResponse = restTemplate.exchange(groupURI, HttpMethod.GET, entity,
					GroupRepresentation.class);

			GroupRepresentation group = groupResponse.getBody();

			URI uri = KeycloakUriBuilder.fromUri(keycloakServer + "/admin/realms/{realm}/groups/{id}/members")
					.build(keycloakRealm, group.getId());

			ResponseEntity<UserRepresentation[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
					UserRepresentation[].class);

			for (UserRepresentation user : response.getBody()) {
				users.add(new WfUser(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
						user.getEmail(), user.getGroups(), user.getRealmRoles()));
			}

		} catch (HttpClientErrorException e) {
			logger.error("GET '/admin/realms/{realm}/groups/{id}/members' failed: " + e.getMessage());
		}

		return users;
	}
	*/

	@Override
	public List<WfOwner> getRealmGroups() {
		
		return WfOwner.fromOwners(processRepository.getOwners());
	}

	@Override
	public boolean groupContainsUser(String groupName){
		KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();

		GroupRepresentation group = keycloak.getGroupByPath(groupName);
		List<UserRepresentation> users = keycloak.groups().group(group.getId()).members(0, 999);

		for(UserRepresentation user : users){
			if(user.getEmail().equals(token.getEmail()))
				return true;
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getUserGroups() {
		KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
		List<String> userGroups = new ArrayList<>();

		List<GroupRepresentation> groups = keycloak.groups().groups();
		for(GroupRepresentation group : groups){
			List<UserRepresentation> users = keycloak.groups().group(group.getId()).members(0,999);
			for(UserRepresentation user : users){
				if(user.getEmail().equals(token.getEmail()))
					userGroups.add(group.getName());
			}
		}

		return userGroups;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<WfOwner> getUserOwnership() {
		KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
		List<String> groups = (List<String>) token.getOtherClaims().get("groups");
		List<WfOwner> returnList = new ArrayList<>();
		
		if(groups != null && groups.size() > 0) {
			for(String group : groups) {
				WfOwner wfOwner;
				
				try {
					wfOwner = new WfOwner(processRepository.getOwnerById(group));
					
				} catch (Exception e) {
					wfOwner = new WfOwner();
					wfOwner.setOwnerId(group);
					wfOwner.setName(group);
				}
				returnList.add(wfOwner);
			}
		}
		return returnList;
	}

	@Override
	@Transactional
	public void deleteOwner(String ownerId) {
		List<GroupRepresentation> groups = keycloak.groups().groups();
		HashMap<String, GroupRepresentation> groupsMapped = new HashMap<>();

		// create map contains group name and the group represantation of it, in
		// order to search for groups more easily
		for (GroupRepresentation group : groups) {
			groupsMapped.put(group.getName(), group);
		}

		GroupRepresentation group = groupsMapped.get(ownerId);

		// remove group from keycloak if exists
		if (group != null)
			keycloak.groups().group(group.getId()).remove();

		// delete group from db
		processRepository.deleteOwnerByOwnerId(ownerId);
	}

	@Override
	@Transactional
	public Owner saveOwner(WfOwner wfOwner) throws InvalidRequestException {
		Owner owner = new Owner();
		List<GroupRepresentation> groups = keycloak.groups().groups();
		HashMap<String, GroupRepresentation> groupsMapped = new HashMap<>();

		// create map contains group name and the group represantation of it, in
		// order to search for groups more easy
		for (GroupRepresentation group : groups) {
			groupsMapped.put(group.getName(), group);
		}
		
		// check if exists in keycloak in order to re-create it
		GroupRepresentation groupFromMap = groupsMapped.get(wfOwner.getOwnerId());

		try {
			// check if already exists in order to update it
			owner = processRepository.getOwnerById(wfOwner.getOwnerId());
			owner.setOwnerName(wfOwner.getName());

			// if owner/group to be updated not exists in keycloak will be
			// created again
			if (groupFromMap == null) {
				GroupRepresentation group = new GroupRepresentation();
				group.setName(wfOwner.getOwnerId());
				keycloak.groups().add(group);
			}

		} catch (Exception e) {
			owner.setOwnerId(wfOwner.getOwnerId());
			owner.setOwnerName(wfOwner.getName());

			if (groupFromMap == null) {
				GroupRepresentation group = new GroupRepresentation();
				group.setName(wfOwner.getOwnerId());
				keycloak.groups().add(group);
			}
		}

		processRepository.saveOwner(owner);
		return owner;
	}

	@Override
	public List<WfRole> getRoles() {

		return WfRole.fromRoles(processRepository.getRoles());
	}

	@Override
	@Transactional
	public Role saveRole(WfRole wfRole) throws InvalidRequestException {
		Role role = new Role();
		List<RoleRepresentation> roles = keycloak.roles().list();
		HashMap<String, RoleRepresentation> rolesMapped = new HashMap<>();

		for (RoleRepresentation roleRepresentation : roles) {
			rolesMapped.put(roleRepresentation.getName(), roleRepresentation);
		}

		RoleRepresentation roleFromMap = rolesMapped.get(wfRole.getRoleId());

		try {
			role = processRepository.getRoleByRoleId(wfRole.getRoleId());
			role.setRoleDescription(wfRole.getDescription());
			
			// means that the role is deleted from keycloak and therefore will be
			// created
			if (roleFromMap == null) {
				RoleRepresentation roleRepresentation = new RoleRepresentation();
				roleRepresentation.setName(wfRole.getRoleId());
				roleRepresentation.setDescription(wfRole.getDescription());
				keycloak.roles().create(roleRepresentation);
			}

		} catch (Exception e) {
			role.setRoleId(wfRole.getRoleId());
			role.setRoleDescription(wfRole.getDescription());
			
			// check if role already exists in keycloak
			if (roleFromMap == null) {
				RoleRepresentation roleRepresentation = new RoleRepresentation();
				roleRepresentation.setName(wfRole.getRoleId());
				roleRepresentation.setDescription(wfRole.getDescription());
				keycloak.roles().create(roleRepresentation);
			} 
		}
		processRepository.saveRole(role);
		return role;
	}

	@Override
	@Transactional
	public void deleteRole(String roleId) {
		List<RoleRepresentation> roles = keycloak.roles().list();
		HashMap<String, RoleRepresentation> rolesMapped = new HashMap<>();

		// create map contains role name and the role represantation, in
		// order to search for roles more easily
		for (RoleRepresentation role : roles) {
			rolesMapped.put(role.getName(), role);
		}

		RoleRepresentation roleFromMap = rolesMapped.get(roleId);

		// remove group from keycloak if exists
		if (roleFromMap != null)
			keycloak.roles().deleteRole(roleFromMap.getName());

		// delete group from db
		processRepository.deleteRoleByRoleId(roleId);
	}
	
	/**
	 * 
	 * @param restTemplate
	 * @param headers
	 * @param userID
	 * @return
	 */
	private List<String> getUserRoles(RestTemplate restTemplate, HttpHeaders headers, String userID) {
		List<String> roles = new ArrayList<String>();

		try {

			HttpEntity<String> entity = new HttpEntity<String>(headers);
			
			URI uri = KeycloakUriBuilder.fromUri(keycloakServer + "/admin/realms/{realm}/users/{id}/role-mappings/realm/composite").build(keycloakRealm, userID);
			
			ResponseEntity<RoleRepresentation[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, RoleRepresentation[].class);

			for (RoleRepresentation role : response.getBody()) {
				roles.add(role.getName());
			}

		} catch (HttpClientErrorException e) {
			logger.error("GET '/admin/realms/{realm}/users/{id}/role-mappings/realm/composite' failed: " + e.getMessage());
		}

		return roles;
	}

	/**
	 * 
	 * @return
	 */
	private String getToken() {
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> data = new LinkedMultiValueMap<String, String>();

		data.add("username", keycloakUser);
		data.add("password", keycloakPassword);
		data.add(OAuth2Constants.GRANT_TYPE, "password");
		data.add(OAuth2Constants.CLIENT_ID, "admin-cli");

		URI tokenURI = KeycloakUriBuilder.fromUri(keycloakServer).path(ServiceUrlConstants.TOKEN_PATH).build(keycloakRealm);

		@SuppressWarnings("unchecked")
		Map<String, String> result = restTemplate.postForObject(tokenURI, data, Map.class);

		return result.get("access_token");
	}

	@Override
	public List<WfRole> synchronizeRoles() {

		List<WfRole> returnRoles = new ArrayList<>();
		List<RoleRepresentation> keycloakRoles = keycloak.roles().list();

		if (keycloakRoles != null && keycloakRoles.size() > 0) {

			for (RoleRepresentation role : keycloakRoles) {
				if (!processRepository.isRoleExist(role.getName())) {

					WfRole newRole = new WfRole();
					newRole.setRoleId(role.getName());

					if (role.getDescription() != null)
						newRole.setDescription(role.getDescription());
					else
						newRole.setDescription(role.getName());

					returnRoles.add(newRole);
				}
			}

		}

		return returnRoles;
	}

	@Override
	public List<WfOwner> synchronizeOwners() {

		List<WfOwner> returnOwners = new ArrayList<>();
		List<GroupRepresentation> keycloakOwners = keycloak.groups().groups();

		if (keycloakOwners != null && keycloakOwners.size() > 0) {

			for (GroupRepresentation group : keycloakOwners) {
				if (!processRepository.isOWnerExist(group.getName())) {

					WfOwner newOwner = new WfOwner();
					newOwner.setOwnerId(group.getName());
					newOwner.setName(group.getName());

					returnOwners.add(newOwner);
				}
			}

		}

		return returnOwners;
	}

	@Override
	@Transactional
	public void importOwners(List<WfOwner> owners) {
		
		if(owners != null && owners.size() > 0) {
			for(WfOwner wfOwner : owners) {
				
				Owner owner = new Owner();
				owner.setOwnerId(wfOwner.getOwnerId());
				
				processRepository.saveOwner(owner);
			}
		}
		
	}

	@Override
	@Transactional
	public void importRoles(List<WfRole> roles) {
		
		if(roles != null && roles.size() > 0) {
			for(WfRole wfrole : roles) {
				
				Role role = new Role();
				role.setRoleId(wfrole.getRoleId());
				
				processRepository.saveRole(role);
			}
		}
	}
}