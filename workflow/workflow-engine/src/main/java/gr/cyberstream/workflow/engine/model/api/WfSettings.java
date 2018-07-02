package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gr.cyberstream.workflow.engine.model.WorkflowSettings;

public class WfSettings {

	private int id;
	private boolean autoAssignment;
	private int duedateAlertPeriod;
	private boolean assignmentNotification;
	// facebook
	private List<String> pages; 
	// twitter
	private List<String> accounts;

	public WfSettings() {
		
	}

	public WfSettings(WorkflowSettings settings) {
		this.id = settings.getId();
		this.autoAssignment = settings.isAutoAssignment();
		this.duedateAlertPeriod = settings.getDuedateAlertPeriod();
		this.assignmentNotification = settings.isAssignmentNotification();
		retrieveFacebookPages(settings); // sets the pages variable
		retrieveTwitterPages(settings);  // sets the twitter accounts
	}

	
	// -- Social Media Connectivity Helpers
	
	// --Facebook
	private void retrieveFacebookPages(WorkflowSettings settings){
		Map<String,String> map;
		if(settings.fetchFacebookTokensAsMap()!=null){
			map = settings.fetchFacebookTokensAsMap();
			this.pages = new ArrayList<String>(map.keySet());
		}		
	}
	
	// --Twitter
	private void retrieveTwitterPages(WorkflowSettings settings){
		Map<String,String> map;
		if(settings.fetchTwitterTokensAsMap()!=null){
			map = settings.fetchTwitterTokensAsMap();
			this.accounts = new ArrayList<String>(map.keySet());
		}		
	}
	
	
	
	// -- GETTERS / SETTERS
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isAutoAssignment() {
		return autoAssignment;
	}

	public void setAutoAssignment(boolean autoAssignment) {
		this.autoAssignment = autoAssignment;
	}

	public int getDuedateAlertPeriod() {
		return duedateAlertPeriod;
	}

	public void setDuedateAlertPeriod(int duedateAlertPeriod) {
		this.duedateAlertPeriod = duedateAlertPeriod;
	}

	public boolean isAssignmentNotification() {
		return assignmentNotification;
	}

	public void setAssignmentNotification(boolean assignmentNotification) {
		this.assignmentNotification = assignmentNotification;
	}

	public List<String> getPages() {
		return pages;
	}

	public void setPages(List<String> pages) {
		this.pages = pages;
	}

	public List<String> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<String> accounts) {
		this.accounts = accounts;
	}
	
}
