package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import gr.cyberstream.workflow.engine.model.ExternalGroup;

/**
 * Api model used in api communications for ExternalGroup
 * 
 * @author kkoutros
 *
 */
public class WfPublicGroup {

	private int groupId;
	private String name;
	private int externalGroupOrder;

	/**
	 * Default constructor
	 */
	public WfPublicGroup() {

	}

	/**
	 * Constructor from ExternalGroup
	 * 
	 * @param externalGroup
	 */
	public WfPublicGroup(ExternalGroup externalGroup) {
		if (externalGroup != null && externalGroup.getId() != 0) {
			this.groupId = externalGroup.getId();
			this.name = externalGroup.getName();
			this.externalGroupOrder = externalGroup.getOrderCode();
		}
	}

	/**
	 * From a list of externalGroups
	 * 
	 * @param externalGroups
	 * @return
	 */
	public static List<WfPublicGroup> fromExternalGroups(List<ExternalGroup> externalGroups) {
		List<WfPublicGroup> returnList = new ArrayList<>();

		for (ExternalGroup externalGroup : externalGroups) {
			WfPublicGroup wfExternalGroup = new WfPublicGroup(externalGroup);
			returnList.add(wfExternalGroup);
		}

		return returnList;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getExternalGroupOrder() {
		return externalGroupOrder;
	}

	public void setExternalGroupOrdere(int externalGroupOrder) {
		this.externalGroupOrder = externalGroupOrder;
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof WfPublicGroup) {
			WfPublicGroup that = (WfPublicGroup) other;
			result = (this.getGroupId() == that.getGroupId());
		}
		return result;
	}

}
