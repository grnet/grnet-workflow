package gr.cyberstream.workflow.engine.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;

import gr.cyberstream.workflow.engine.model.api.WfPublicGroup;

/**
 * The persistent class for the ExternalGroup database table.
 * 
 * @author kkoutros
 */
@Entity
@Table(name = "ExternalGroup")
public class ExternalGroup {

	@Id
	private int id;

	private String name;

	private int orderCode;

	/**
	 * Default constructor
	 */
	public ExternalGroup() {

	}

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

	/**
	 * Copy constructor
	 * 
	 * @param wfExternalGroup
	 */
	public ExternalGroup(WfPublicGroup wfExternalGroup) {
		this.id = wfExternalGroup.getGroupId();
		this.name = wfExternalGroup.getName();
		this.orderCode = wfExternalGroup.getExternalGroupOrder();
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof ExternalGroup) {
			ExternalGroup that = (ExternalGroup) other;
			result = (this.getId() == that.getId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		builder.append(name);
		builder.append(orderCode);
		return builder.toHashCode();
	}
}
