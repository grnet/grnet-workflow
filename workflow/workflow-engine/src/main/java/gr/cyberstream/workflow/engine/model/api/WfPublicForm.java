package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import gr.cyberstream.workflow.engine.model.ExternalForm;

public class WfPublicForm {

	private String formId;
	private String supervisor;
	private String titleTemplate;
	private int workflowDefinitionId;
	private boolean enabled;
	private boolean mobileEnabled;
	private String groupName;
	private int externalFormOrder;
	private WfPublicGroup formExternalGroup;
	private String icon;
	private String definitionName;

	public WfPublicForm() {
	}

	public WfPublicForm(ExternalForm externalForm) {
		this.formId = externalForm.getId();
		this.supervisor = externalForm.getSupervisor();
		this.titleTemplate = externalForm.getTitleTemplate();
		this.workflowDefinitionId = externalForm.getWorkflowDefinition().getId();
		this.enabled = externalForm.isEnabled();
		this.mobileEnabled = externalForm.isMobileEnabled();
		this.externalFormOrder = externalForm.getOrderCode();
		this.icon = externalForm.getWorkflowDefinition().getIcon();
		this.definitionName = externalForm.getWorkflowDefinition().getName();

		if (externalForm.getExternalGroup() != null && externalForm.getExternalGroup().getId() != 0) {
			this.groupName = externalForm.getExternalGroup().getName();
			this.formExternalGroup = new WfPublicGroup(externalForm.getExternalGroup());

		}
	}

	public static List<WfPublicForm> fromExternalForms(List<ExternalForm> forms) {

		List<WfPublicForm> wfForms = new ArrayList<WfPublicForm>();
		for (ExternalForm form : forms) {
			wfForms.add(new WfPublicForm(form));
		}

		return wfForms;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}

	public String getTitleTemplate() {
		return titleTemplate;
	}

	public void setTitleTemplate(String titleTemplate) {
		this.titleTemplate = titleTemplate;
	}

	public int getWorkflowDefinitionId() {
		return workflowDefinitionId;
	}

	public void setWorkflowDefinitionId(int workflowDefinitionId) {
		this.workflowDefinitionId = workflowDefinitionId;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isMobileEnabled() {
		return mobileEnabled;
	}

	public void setMobileEnabled(boolean mobileEnabled) {
		this.mobileEnabled = mobileEnabled;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getExternalFormOrder() {
		return externalFormOrder;
	}

	public void setExternalFormOrder(int externalFormOrder) {
		this.externalFormOrder = externalFormOrder;
	}

	public WfPublicGroup getFormExternalGroup() {
		return formExternalGroup;
	}

	public void setFormExternalGroup(WfPublicGroup formExternalGroup) {
		this.formExternalGroup = formExternalGroup;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getDefinitionName() {
		return definitionName;
	}

	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}

}
