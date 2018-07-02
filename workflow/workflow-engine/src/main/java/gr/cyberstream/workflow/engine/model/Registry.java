package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;

import gr.cyberstream.workflow.engine.model.api.WfRegistry;

/**
 * The persistent class for the Registry database table.
 * 
 */
@Entity
@Table(name = "Registry")
public class Registry implements Serializable {

	private static final long serialVersionUID = 1847022657860380926L;

	@Id
	private String id;

	private String template;

	private int next;

	/**
	 * Default constructor
	 */
	public Registry() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public int getNext() {
		return next;
	}

	public void setNext(int next) {
		this.next = next;
	}

	/**
	 * Copy constructor
	 * 
	 * @param wfRegistry
	 */
	public Registry(WfRegistry wfRegistry) {
		this.id = wfRegistry.getId();
		this.template = wfRegistry.getTemplate();
		this.next = wfRegistry.getNext();
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Registry) {
			Registry that = (Registry) other;
			result = (this.getId() == that.getId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		builder.append(template);
		builder.append(next);
		return builder.toHashCode();
	}

}