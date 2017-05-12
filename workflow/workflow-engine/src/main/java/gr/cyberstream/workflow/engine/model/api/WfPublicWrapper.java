package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import gr.cyberstream.workflow.engine.model.ExternalWrapper;

/**
 * Api model used in api communications for ExternalGroup
 * 
 * @author kkoutros
 *
 */
public class WfPublicWrapper {

	private String formId;
	private int groupId;
	private String groupName;
	private int externalGroupOrder;
	private String titleTemplate;
	private int externalFormOrder;
	private boolean mobileEnabled;
	private boolean enabled;
	private String supervisor;
	private String defintionName;
	private String icon;
	private int workflowDefinitionId;
	private WfPublicGroup formExternalGroup;

	/**
	 * Default constructor
	 */
	public WfPublicWrapper() {

	}

	/**
	 * Constructor from ExternalGroup
	 * 
	 * @param externalGroup
	 */
	public WfPublicWrapper(ExternalWrapper externalWrapper) {
		WfPublicGroup wfExternalGroup = new WfPublicGroup(externalWrapper.getExternalGroup());

		this.groupId = wfExternalGroup.getGroupId();
		this.groupName = wfExternalGroup.getName();
		this.externalGroupOrder = wfExternalGroup.getExternalGroupOrder();

		if (externalWrapper.getExternalForm() != null) {
			WfPublicForm wfExternalForm = new WfPublicForm(externalWrapper.getExternalForm());
			this.formId = wfExternalForm.getFormId();
			this.titleTemplate = wfExternalForm.getTitleTemplate();
			this.externalFormOrder = wfExternalForm.getExternalFormOrder();
			this.mobileEnabled = wfExternalForm.isMobileEnabled();
			this.enabled = wfExternalForm.isEnabled();
			this.supervisor = wfExternalForm.getSupervisor();
			this.defintionName = wfExternalForm.getDefinitionName();
			this.icon = wfExternalForm.getIcon();
			this.workflowDefinitionId = wfExternalForm.getWorkflowDefinitionId();
			this.formExternalGroup = wfExternalForm.getFormExternalGroup();
		}
	}

	/**
	 * From a list of externalGroups
	 * 
	 * @param externalGroups
	 * @return
	 */
	public static List<WfPublicWrapper> fromExternalWrappers(List<ExternalWrapper> externalWrappers) {
		List<WfPublicWrapper> returnList = new ArrayList<>();

		for (ExternalWrapper wrapper : externalWrappers) {
			WfPublicWrapper wfExternalGroup = new WfPublicWrapper(wrapper);
			returnList.add(wfExternalGroup);
		}

		return returnList;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getExternalGroupOrder() {
		return externalGroupOrder;
	}

	public void setExternalGroupOrder(int externalGroupOrder) {
		this.externalGroupOrder = externalGroupOrder;
	}

	public String getTitleTemplate() {
		return titleTemplate;
	}

	public void setTitleTemplate(String titleTemplate) {
		this.titleTemplate = titleTemplate;
	}

	public int getExternalFormOrder() {
		return externalFormOrder;
	}

	public void setExternalFormOrder(int externalFormOrder) {
		this.externalFormOrder = externalFormOrder;
	}

	public boolean isMobileEnabled() {
		return mobileEnabled;
	}

	public void setMobileEnabled(boolean mobileEnabled) {
		this.mobileEnabled = mobileEnabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}

	public String getDefintionName() {
		return defintionName;
	}

	public void setDefintionName(String defintionName) {
		this.defintionName = defintionName;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getWorkflowDefinitionId() {
		return workflowDefinitionId;
	}

	public void setWorkflowDefinitionId(int workflowDefinitionId) {
		this.workflowDefinitionId = workflowDefinitionId;
	}

	public WfPublicGroup getFormExternalGroup() {
		return formExternalGroup;
	}

	public void setFormExternalGroup(WfPublicGroup formExternalGroup) {
		this.formExternalGroup = formExternalGroup;
	}
}
