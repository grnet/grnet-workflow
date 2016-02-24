package gr.cyberstream.workflow.engine.model.api;

import java.util.List;

public class WfUser {

	private String id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private List<String> groups;
	private List<String> userRoles;
	private Long pendingTasks;
	public WfUser() {
	}

	public WfUser(String id, String username, String firstName, String lastName, String email, List<String> groups,
			List<String> userRoles) {

		this.id = id;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.groups = groups;
		this.userRoles = userRoles;

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public List<String> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<String> userRoles) {
		this.userRoles = userRoles;
	}

	public Long getPendingTasks() {
		return pendingTasks;
	}

	public void setPendingTasks(Long pendingTasks) {
		this.pendingTasks = pendingTasks;
	}
	
	@Override 
	 public boolean equals(Object other) {
	        boolean result = false;
	        if (other instanceof WfUser) {
	        	WfUser that = (WfUser) other;
	            result = (this.getId().equals(that.getId()));
	        }
	        return result;
	    }
}
