package gr.cyberstream.workflow.engine.model.api;

import gr.cyberstream.workflow.engine.model.WorkflowSettings;

public class WfSettings {

	private int id;
	private boolean autoAssignment;
	private int duedateAlertPeriod;
	private boolean assignmentNotification;

	public WfSettings() {

	}

	public WfSettings(WorkflowSettings settings) {
		this.id = settings.getId();
		this.autoAssignment = settings.isAutoAssignment();
		this.duedateAlertPeriod = settings.getDuedateAlertPeriod();
		this.assignmentNotification = settings.isAssignmentNotification();
	}

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
}
