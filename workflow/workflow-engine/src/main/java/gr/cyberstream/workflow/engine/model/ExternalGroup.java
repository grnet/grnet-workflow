package gr.cyberstream.workflow.engine.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import gr.cyberstream.workflow.engine.model.api.WfPublicGroup;

/**
 * 
 * @author kkoutros
 *
 */
@Entity
public class ExternalGroup {

	public ExternalGroup() {

	}

	@Id
	private int id;

	private String name;

	private int orderCode;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(int orderCode) {
		this.orderCode = orderCode;
	}

	public ExternalGroup(WfPublicGroup wfExternalGroup) {
		this.id = wfExternalGroup.getGroupId();
		this.name = wfExternalGroup.getName();
		this.orderCode = wfExternalGroup.getExternalGroupOrder();
	}
}
