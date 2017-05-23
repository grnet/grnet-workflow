package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import gr.cyberstream.workflow.engine.model.Role;

public class WfRole {

	private String roleId;
	private String description;

	public WfRole() {

	}

	public WfRole(Role role) {
		this.roleId = role.getRoleId();
		this.description = role.getRoleDescription();
	}

	public static List<WfRole> fromRoles(List<Role> roles) {
		List<WfRole> returnList = new ArrayList<>();

		for (Role role : roles) {
			WfRole wfRole = new WfRole(role);
			returnList.add(wfRole);
		}
		return returnList;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
