package gr.cyberstream.workflow.engine.service.impl;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ManagementService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gr.cyberstream.workflow.engine.model.dashboard.DashboardDatePartResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardSimpleResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardTaskResult;
import gr.cyberstream.workflow.engine.persistence.Dashboard;
import gr.cyberstream.workflow.engine.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	private Dashboard dashboard;
	
	@Autowired
	private ManagementService activitiManagementService;
		
	@Override
	public List<DashboardSimpleResult> getDefinitionRunningInstances(boolean supervisor) {
		
		String user = null;
		
		if (supervisor) {
			KeycloakAuthenticationToken authentication = 
					(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
			user = token.getEmail();
		}
		
		return dashboard.getDefinitionRunningInstances(user);
	}
	
	@Override
	public List<DashboardSimpleResult> getOwnerRunningInstances() {
		
		return dashboard.getOwnerRunningInstances();
	}
	
	@Override
	public List<DashboardDatePartResult> getCompletedInstances(boolean supervisor, Date from, Date to, String dateGroup) {
	
		String user = null;
		
		if (supervisor) {
			KeycloakAuthenticationToken authentication = 
					(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
			user = token.getEmail();
		}
		
		return dashboard.getCompletedInstances(user, from, to, dateGroup);
	}
	
	@Override
	public List<DashboardDatePartResult> getDefinitionCompletedInstances(boolean supervisor, Date from, Date to,
			String dateGroup, String... definitions) {

		String user = null;
		
		if (supervisor) {
			KeycloakAuthenticationToken authentication = 
					(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
			user = token.getEmail();
		}
		
		return dashboard.getDefinitionCompletedInstances(user, from, to, dateGroup, definitions);
	}
	
	@Override
	public List<DashboardDatePartResult> getOwnerCompletedInstances(Date from, Date to,
			String dateGroup, String... owners) {
		
		return dashboard.getOwnerCompletedInstances(from, to, dateGroup, owners);
	}

	@Override
	public List<DashboardDatePartResult> getClientStartedInstances(boolean supervisor, Date from, Date to, String dateGroup, String... clients) {
		
		String user = null;
		
		if (supervisor) {
			KeycloakAuthenticationToken authentication = 
					(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
			user = token.getEmail();
		}
		
		return dashboard.getClientStartedInstances(user, from, to, dateGroup, clients);
	}
	
	@Override
	public List<DashboardTaskResult> getOverdueTasks(boolean supervisor) {
		
		String user = null;
		
		if (supervisor) {
			KeycloakAuthenticationToken authentication = 
					(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
			user = token.getEmail();
		}
		
		String taskTableName = activitiManagementService.getTableName(Task.class);
		
		return dashboard.getOverdueTasks(user, taskTableName);
	}
	
	@Override
	public List<DashboardTaskResult> getUnassignedTasks(boolean supervisor) {
		
		String user = null;
		
		if (supervisor) {
			KeycloakAuthenticationToken authentication = 
					(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
			user = token.getEmail();
		}
		
		String taskTableName = activitiManagementService.getTableName(Task.class);
		
		return dashboard.getUnassignedTasks(user, taskTableName);
	}
	
	@Override
	public List<DashboardDatePartResult> getCompletedInstancesMeanTime(boolean supervisor, Date from, Date to, String dateGroup) {
		
		String user = null;
		
		if (supervisor) {
			KeycloakAuthenticationToken authentication = 
					(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
			user = token.getEmail();
		}
		
		return dashboard.getCompletedInstancesMeanTime(user, from, to, dateGroup);
	}
	
	@Override
	public List<DashboardSimpleResult> getActiveUserTasks(boolean supervisor, String mode, int count) {
		
		String user = null;
		
		if (supervisor) {
			KeycloakAuthenticationToken authentication = 
					(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
			user = token.getEmail();
		}
		
		String taskTableName = activitiManagementService.getTableName(Task.class);
		
		return dashboard.getActiveUserTasks(user, taskTableName, mode, count);
	}
	
	@Override
	public List<DashboardSimpleResult> getCompletedUserTasks(boolean supervisor, Date from, Date to, String mode, int count) {
		
		String user = null;
		
		if (supervisor) {
			KeycloakAuthenticationToken authentication = 
					(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();
			user = token.getEmail();
		}
		
		String taskTableName = activitiManagementService.getTableName(HistoricTaskInstance.class);
		
		return dashboard.getCompletedUserTasks(user, taskTableName, from, to, mode, count);
	}
}
