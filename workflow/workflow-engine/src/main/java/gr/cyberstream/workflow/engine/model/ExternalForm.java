package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;

import gr.cyberstream.workflow.engine.model.api.WfPublicForm;

/**
 * The persistent class for the ExternalForm database table.
 * 
 */
@Entity
@Table(name = "ExternalForm")
public class ExternalForm implements Serializable {

	private static final long serialVersionUID = 518327039761229729L;

	@Id
	private String id;

	private String supervisor;

	@Column(name = "title_template")
	private String titleTemplate;

	@ManyToOne
	@JoinColumn(name = "workflow_definition_id")
	private WorkflowDefinition workflowDefinition;

	private boolean enabled;

	@ManyToOne
	@JoinColumn(name = "externalGroup")
	private ExternalGroup externalGroup;

	private int orderCode;

	private boolean mobileEnabled;

	/**
	 * Default constructor
	 */
	public ExternalForm() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}

	public WorkflowDefinition getWorkflowDefinition() {
		return workflowDefinition;
	}

	public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
		this.workflowDefinition = workflowDefinition;
	}

	public String getTitleTemplate() {
		return titleTemplate;
	}

	public void setTitleTemplate(String titleTemplate) {
		this.titleTemplate = titleTemplate;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ExternalGroup getExternalGroup() {
		return externalGroup;
	}

	public void setExternalGroup(ExternalGroup externalGroup) {
		this.externalGroup = externalGroup;
	}

	public int getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(int orderCode) {
		this.orderCode = orderCode;
	}

	public boolean isMobileEnabled() {
		return mobileEnabled;
	}

	public void setMobileEnabled(boolean mobileEnabled) {
		this.mobileEnabled = mobileEnabled;
	}

	/**
	 * Copy constructor
	 * 
	 * @param wfPublicform
	 * @param workflowDefinition
	 */
	public void updateFrom(WfPublicForm wfPublicform, WorkflowDefinition workflowDefinition) {
		this.id = wfPublicform.getFormId();
		this.supervisor = wfPublicform.getSupervisor();
		this.titleTemplate = wfPublicform.getTitleTemplate();
		this.enabled = wfPublicform.isEnabled();
		this.mobileEnabled = wfPublicform.isMobileEnabled();
		this.orderCode = wfPublicform.getExternalFormOrder();
		this.workflowDefinition = workflowDefinition;

		// check if external form has group
		// otherwise will throw exception since there is no group with id 0
		if (wfPublicform.getFormExternalGroup() != null && wfPublicform.getFormExternalGroup().getGroupId() != 0) {
			this.externalGroup = new ExternalGroup(wfPublicform.getFormExternalGroup());
		}
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof ExternalForm) {
			ExternalForm that = (ExternalForm) other;
			result = (this.getId() == that.getId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		builder.append(supervisor);
		builder.append(titleTemplate);
		builder.append(workflowDefinition);
		builder.append(enabled);
		builder.append(externalGroup);
		builder.append(orderCode);
		builder.append(mobileEnabled);
		return builder.toHashCode();
	}

}
