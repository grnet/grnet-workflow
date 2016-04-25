package gr.cyberstream.workflow.engine.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import gr.cyberstream.workflow.engine.model.api.WfUser;

/**
 * 
 * @author gtyl
 *
 */

@Service
public class RealmService {

	final static Logger logger = LoggerFactory.getLogger(RealmService.class);
	
	private final String keycloakServer;
	private final String keycloakRealm;
	private final String keycloakUser;
	private final String keycloakPassword;
	
	public RealmService() {
		
		PropertyResourceBundle properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");
		
		keycloakServer = properties.getString("keycloak.server");
		keycloakRealm = properties.getString("keycloak.realm");
		keycloakUser = properties.getString("keycloak.user");
		keycloakPassword = properties.getString("keycloak.password");
	}
	
	public WfUser getUser(String id) {
		
		RestTemplate restTemplate = new RestTemplate();
		
		WfUser user = new WfUser();
		
		try {

			String token = getToken();
			
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + token);
			
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			
			URI uri = KeycloakUriBuilder.fromUri(keycloakServer + "/admin/realms/{realm}/users/{id}").build(keycloakRealm, id);
			
			ResponseEntity<UserRepresentation> response = restTemplate.exchange(uri, HttpMethod.GET, entity, UserRepresentation.class);
			
			List<String> roles = getUserRoles(restTemplate, headers, id);
			
			if (response != null && response.getBody() != null)
				user = new WfUser(response.getBody().getId(), response.getBody().getUsername(),
						response.getBody().getFirstName(), response.getBody().getLastName(),
						response.getBody().getEmail(), response.getBody().getGroups(),
						roles);
			
		} catch (HttpClientErrorException e) {

			logger.error("GET '/admin/realms/{realm}/users/{id}' failed: " + e.getMessage());
		}
		
		return user;
	}
	
	public List<WfUser> getUsersByRole(String role) {
		
		RestTemplate restTemplate = new RestTemplate();
		
		List<WfUser> users = new ArrayList<WfUser>();
		
		try {

			String token = getToken();
			
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + token);
			
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			
			URI uri = KeycloakUriBuilder.fromUri(keycloakServer 
					+ "/admin/realms/{realm}/users").build(keycloakRealm);
			
			ResponseEntity<UserRepresentation[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, UserRepresentation[].class);
			
			for (UserRepresentation user : response.getBody()) {
				
				List<String> roles = getUserRoles(restTemplate, headers, user.getId());
				
				if (roles.contains(role))
					users.add(new WfUser(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
						user.getEmail(), user.getGroups(), roles));
				
			}
			
		} catch (HttpClientErrorException e) {

			logger.error("GET '/admin/realms/{realm}/users' failed: " + e.getMessage());
		}
		
		return users;
	}
	
	public List<WfUser> getAllUsers() {
		
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
				users.add(new WfUser(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getGroups(), user.getRealmRoles()));
			}
			
		} catch (HttpClientErrorException e) {

			logger.error("GET '/admin/realms/{realm}/users' failed: " + e.getMessage());
		}
		
		return users;
	}
	
	public List<WfUser> getUsersByGroupAndRole(String groupName, String role){
		
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
			
			//for keycloak version 1.8.1.Final
			URI uri = KeycloakUriBuilder.fromUri(keycloakServer + "/admin/realms/{realm}/groups/{id}/members?first=0&max=10&").build(keycloakRealm, group.getId());
			
			//for keycloak version 1.8.1.Final and later
			//URI uri = KeycloakUriBuilder.fromUri(keycloakServer + "/admin/realms/{realm}/groups/{id}/members").build(keycloakRealm, group.getId());
			
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
	
	
	public List<WfUser> getUsersByGroup(String groupName){
		
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
			
			URI uri = KeycloakUriBuilder.fromUri(keycloakServer + "/admin/realms/{realm}/groups/{id}/members").build(keycloakRealm, group.getId());
			
			ResponseEntity<UserRepresentation[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, UserRepresentation[].class);
			
			for (UserRepresentation user : response.getBody()) {
				users.add(new WfUser(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getGroups(), user.getRealmRoles()));
			}
			
		} catch (HttpClientErrorException e) {

			logger.error("GET '/admin/realms/{realm}/groups/{id}/members' failed: " + e.getMessage());
		}
		
		return users;
	}
	
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
	
	public List<String> getRealmGroups() {
		
		RestTemplate restTemplate = new RestTemplate();
		
		List<String> groups = new ArrayList<String>();

		try {

			String token = getToken();
			
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + token);
			
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			
			URI uri = KeycloakUriBuilder.fromUri(keycloakServer
					+ "/admin/realms/{realm}/groups").build(keycloakRealm);
			
			ResponseEntity<GroupRepresentation[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, GroupRepresentation[].class);
			
			for (GroupRepresentation group : response.getBody()) {
				
				groups.add(group.getName());
			}
			
		} catch (HttpClientErrorException e) {

			logger.error("GET '/admin/realms/{realm}/groups' failed: " + e.getMessage());
		}

		return groups;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getUserGroups() {
		
		KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();
		AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();

		return (List<String>) token.getOtherClaims().get("groups");
	}
	
	private String getToken() {
		
		RestTemplate restTemplate = new RestTemplate();
		
		MultiValueMap<String, String> data = new LinkedMultiValueMap<String, String>();
		data.add("username", keycloakUser);
		data.add("password", keycloakPassword);
		data.add(OAuth2Constants.GRANT_TYPE, "password");
		data.add(OAuth2Constants.CLIENT_ID, "admin-cli");

		URI tokenURI = KeycloakUriBuilder.fromUri(keycloakServer)
				.path(ServiceUrlConstants.TOKEN_PATH).build(keycloakRealm);
		
		@SuppressWarnings("unchecked")
		Map<String, String> result = restTemplate.postForObject(tokenURI, data, Map.class);
				
		return result.get("access_token");
	}
}
