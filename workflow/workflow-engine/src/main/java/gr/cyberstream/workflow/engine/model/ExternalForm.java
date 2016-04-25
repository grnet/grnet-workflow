package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import gr.cyberstream.workflow.engine.model.api.WfPublicForm;

@Entity
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

	public void updateFrom(WfPublicForm wfxForm, WorkflowDefinition workflow) {
		this.id = wfxForm.getFormId();
		this.supervisor = wfxForm.getSupervisor();
		this.titleTemplate = wfxForm.getTitleTemplate();
		this.enabled = wfxForm.isEnabled();

		this.mobileEnabled = wfxForm.isMobileEnabled();
		this.orderCode = wfxForm.getExternalFormOrder();
		this.workflowDefinition = workflow;

		//check if external form has group
		if (wfxForm.getFormExternalGroup() != null && wfxForm.getFormExternalGroup().getGroupId() != 0) {
			this.externalGroup = new ExternalGroup(wfxForm.getFormExternalGroup());
		}
	}

}
