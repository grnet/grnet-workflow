package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import gr.cyberstream.workflow.engine.model.api.WfSettings;

/**
 * The persistent class for the WorkflowSettings database table.
 * 
 */
@Entity
public class WorkflowSettings implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "auto_assignment")
	private boolean autoAssignment;

	@Column(name = "duedate_alert_period")
	private int duedateAlertPeriod;

	@Column(name = "assignment_notification")
	private boolean assignmentNotification;

	/**
	 * Default constructor
	 */
	public WorkflowSettings() {

	}

	/**
	 * A copy constructor using a {@link WfSettings} object
	 * 
	 * @param wfSettings
	 *            The {@link WfSettings} to copy
	 */
	public WorkflowSettings(WfSettings wfSettings) {
		this.id = wfSettings.getId();
		this.duedateAlertPeriod = wfSettings.getDuedateAlertPeriod();
		this.autoAssignment = wfSettings.isAutoAssignment();
		this.assignmentNotification = wfSettings.isAssignmentNotification();
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