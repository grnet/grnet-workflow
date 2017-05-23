package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;

import gr.cyberstream.workflow.engine.model.api.WfExternalUser;

@Entity
@Table(name = "ExternalUser")
public class ExternalUser implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String deviceId;
	private String simPhoneNumber;
	private String phoneNumber;
	private String email;
	private String name;
	private String address;
	private String client;

	public ExternalUser() {

	}

	public ExternalUser(WfExternalUser wfExternalUser) {
		this.deviceId = wfExternalUser.getDeviceId();
		this.simPhoneNumber = wfExternalUser.getSimPhoneNumber();
		this.phoneNumber = wfExternalUser.getPhoneNo();
		this.email = wfExternalUser.getEmail();
		this.name = wfExternalUser.getName();
		this.address = wfExternalUser.getAddress();
		this.client = wfExternalUser.getClient();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getSimPhoneNumber() {
		return simPhoneNumber;
	}

	public void setSimPhoneNumber(String simPhoneNumber) {
		this.simPhoneNumber = simPhoneNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof ExternalUser) {
			ExternalUser that = (ExternalUser) other;
			result = this.getDeviceId().equals(that.getDeviceId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		builder.append(deviceId);
		builder.append(simPhoneNumber);
		builder.append(phoneNumber);
		builder.append(email);
		builder.append(name);
		builder.append(address);
		builder.append(client);
		return builder.toHashCode();
	}

}
