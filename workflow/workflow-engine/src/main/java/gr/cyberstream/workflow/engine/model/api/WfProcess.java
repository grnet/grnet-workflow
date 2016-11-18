/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;

/**
 * Models a workflow definition object enhanced with form information. Used in
 * API communications.
 * 
 * @author nlyk
 */
public class WfProcess {

	private int id;
	private String name;
	private String description;
	private String icon;
	private String processDefinitionId;
	private boolean active;
	private List<WfProcessVersion> processVersions;
	private List<WfFormProperty> processForm;
	private String owner;
	private boolean assignBySupervisor;
	private String activeDeploymentId;
	private boolean startForm;

	/**
	 * Default constructor
	 */
	public WfProcess() {
	}

	/**
	 * Copy constructor used to convert a {@link WorkflowDefinition} into a
	 * {@link WfProcess}
	 * 
	 * @param workflowDefinition
	 *            Object to be converted
	 */
	public WfProcess(WorkflowDefinition workflowDefinition) {
		this.id = workflowDefinition.getId();
		this.name = workflowDefinition.getName();
		this.description = workflowDefinition.getDescription();
		this.icon = workflowDefinition.getIcon();
		this.processDefinitionId = workflowDefinition.getKey();
		this.active = workflowDefinition.isSelectedVersionActive();
		setDefinitionVersions(workflowDefinition.getDefinitionVersions()); // set
																			// definition
																			// versions
		this.owner = workflowDefinition.getOwner();
		this.assignBySupervisor = workflowDefinition.isAssignBySupervisor();
		this.activeDeploymentId = workflowDefinition.getActiveDeploymentId();
		this.startForm = workflowDefinition.hasStartForm();
	}

	/**
	 * A copy constructor from a list of workflow definitions
	 * 
	 * @param processes
	 *            A list to be converted
	 * 
	 * @return A list of {@link WfProcess}
	 */
	public static List<WfProcess> fromWorkflowDefinitions(List<WorkflowDefinition> workflowDefinitions) {
		List<WfProcess> returnList = new ArrayList<WfProcess>();

		for (WorkflowDefinition definition : workflowDefinitions) {
			returnList.add(new WfProcess(definition));
		}

		return returnList;
	}

	/**
	 * A helper function used to covert a list of {@link DefinitionVersion} to
	 * {@link WfProcessVersion}, so it can be set into the {@link WfProcess}
	 * 
	 * @param definitionVersions
	 *            List to be converted
	 */
	private void setDefinitionVersions(List<DefinitionVersion> definitionVersions) {
		
		this.processVersions = new ArrayList<>();

		if (definitionVersions == null || definitionVersions.size() == 0)
			return;

		for (DefinitionVersion definitionVersion : definitionVersions) {
			this.processVersions.add(new WfProcessVersion(definitionVersion));
		}
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<WfProcessVersion> getProcessVersions() {
		return processVersions;
	}

	public void setProcessVersions(List<WfProcessVersion> processVersions) {
		this.processVersions = processVersions;
	}

	public List<WfFormProperty> getProcessForm() {
		return processForm;
	}

	public void setProcessForm(List<WfFormProperty> processForm) {
		this.processForm = processForm;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public boolean isAssignBySupervisor() {
		return assignBySupervisor;
	}

	public void setAssignBySupervisor(boolean assignBySupervisor) {
		this.assignBySupervisor = assignBySupervisor;
	}

	public String getActiveDeploymentId() {
		return activeDeploymentId;
	}

	public void setActiveDeploymentId(String activeDeploymentId) {
		this.activeDeploymentId = activeDeploymentId;
	}

	public boolean isStartForm() {
		return startForm;
	}

	public void setStartForm(boolean startForm) {
		this.startForm = startForm;
	}

}
