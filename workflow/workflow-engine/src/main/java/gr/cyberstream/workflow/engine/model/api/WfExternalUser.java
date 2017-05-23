package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import gr.cyberstream.workflow.engine.model.ExternalUser;

public class WfExternalUser {

	private String deviceId;
	private String simPhoneNumber;
	private String phoneNo;
	private String email;
	private String name;
	private String address;
	private String client;

	public WfExternalUser() {

	}

	public WfExternalUser(ExternalUser mobileUser) {
		this.deviceId = mobileUser.getDeviceId();
		this.simPhoneNumber = mobileUser.getSimPhoneNumber();
		this.phoneNo = mobileUser.getPhoneNumber();
		this.email = mobileUser.getEmail();
		this.name = mobileUser.getName();
		this.address = mobileUser.getAddress();
		this.client = mobileUser.getClient();
	}

	public static List<WfExternalUser> fromMobileUsers(List<ExternalUser> mobileUsers) {
		List<WfExternalUser> returnList = new ArrayList<WfExternalUser>();

		for (ExternalUser user : mobileUsers) {
			returnList.add(new WfExternalUser(user));
		}

		return returnList;
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

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
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

}
