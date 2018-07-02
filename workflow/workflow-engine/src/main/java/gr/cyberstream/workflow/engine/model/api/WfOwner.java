package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import gr.cyberstream.workflow.engine.model.Owner;

/**
 * 
 * @author kkoutros
 */
public class WfOwner {

	private String ownerId;
	private String name;

	public WfOwner() {

	}

	public WfOwner(Owner owner) {
		this.ownerId = owner.getOwnerId();
		this.name = owner.getOwnerName();
	}

	public static List<WfOwner> fromOwners(List<Owner> owners) {
		List<WfOwner> returnList = new ArrayList<>();

		for (Owner owner : owners) {
			WfOwner wfOwner = new WfOwner(owner);
			returnList.add(wfOwner);
		}

		return returnList;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
