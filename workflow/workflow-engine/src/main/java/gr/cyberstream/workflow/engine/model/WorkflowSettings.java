package gr.cyberstream.workflow.engine.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gr.cyberstream.workflow.engine.model.api.WfSettings;

/**
 * The persistent class for the WorkflowSettings database table.
 * 
 */
@Entity
@Table(name = "WorkflowSettings")
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

	@Column(name = "facebook_tokens", columnDefinition = "BLOB DEFAULT NULL")
	private byte[] facebookTokens;

	@Column(name = "twitter_tokens", columnDefinition = "BLOB DEFAULT NULL")
	private byte[] twitterTokens;

	/**
	 * Default constructor
	 */
	public WorkflowSettings() {

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

	public byte[] getFacebookTokens() {
		return facebookTokens;
	}

	public void setFacebookTokens(byte[] facebookTokens) {
		this.facebookTokens = facebookTokens;
	}

	public byte[] getTwitterTokens() {
		return twitterTokens;
	}

	public void setTwitterTokens(byte[] twitterTokens) {
		this.twitterTokens = twitterTokens;
	}

	public WorkflowSettings(WfSettings wfSettings, byte[] facebookTokens, byte[] twitterTokens) {
		this.id = wfSettings.getId();
		this.duedateAlertPeriod = wfSettings.getDuedateAlertPeriod();
		this.autoAssignment = wfSettings.isAutoAssignment();
		this.assignmentNotification = wfSettings.isAssignmentNotification();
		this.facebookTokens = facebookTokens;
		this.twitterTokens = twitterTokens;
	}

	/**
	 * Convert Facebook Tokens to map
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> fetchFacebookTokensAsMap() {
		if (this.facebookTokens == null)
			return null;

		ByteArrayInputStream baInputStream = new ByteArrayInputStream(this.facebookTokens);
		ObjectInputStream objInputStream = null;
		try {
			objInputStream = new ObjectInputStream(baInputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, String> map = new HashMap<String, String>();

		try {
			map = (Map<String, String>) objInputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return map;
	}

	/**
	 * Convert Facebook tokens from map to byte array and set it to
	 * facebookTokens property
	 * 
	 * @param map
	 */
	public void assignFacebookTokensFromMap(Map<String, String> map) {
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objOutputStream = null;

		try {
			objOutputStream = new ObjectOutputStream(baOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			objOutputStream.writeObject(map);
			objOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.setFacebookTokens(baOutputStream.toByteArray());
	}

	/**
	 * Convert Twitter Tokens to map
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> fetchTwitterTokensAsMap() {
		if (this.twitterTokens == null)
			return null;

		ByteArrayInputStream baInputStream = new ByteArrayInputStream(this.twitterTokens);
		ObjectInputStream objInputStream = null;
		try {
			objInputStream = new ObjectInputStream(baInputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, String> map = new HashMap<String, String>();

		try {
			map = (Map<String, String>) objInputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return map;
	}

	/**
	 * Convert Twitter tokens from map to byte array and set it to twitterTokens
	 * property
	 * 
	 * @param map
	 */
	public void assignTwitterTokensFromMap(Map<String, String> map) {
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objOutputStream = null;

		try {
			objOutputStream = new ObjectOutputStream(baOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			objOutputStream.writeObject(map);
			objOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.setTwitterTokens(baOutputStream.toByteArray());
	}

}