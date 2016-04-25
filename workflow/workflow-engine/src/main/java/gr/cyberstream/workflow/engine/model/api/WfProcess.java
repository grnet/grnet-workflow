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
public class WfProcess { // extends WorkflowDefinition {

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
	private String registryId;
	private boolean startForm;

	public WfProcess() {
	}

	public WfProcess(WorkflowDefinition definition) {
		this.setId(definition.getId());
		this.setName(definition.getName());
		this.setDescription(definition.getDescription());
		this.setIcon(definition.getIcon());
		this.setActive(definition.isSelectedVersionActive());
		this.setProcessDefinitionId(definition.getKey());
		this.setActive(definition.isSelectedVersionActive());
		this.setOwner(definition.getOwner());
		this.setAssignBySupervisor(definition.isAssignBySupervisor());
		this.setActiveDeploymentId(definition.getActiveDeploymentId());

		this.registryId = (definition.getRegistry() != null) ? definition.getRegistry().getId() : null;
		this.fromDefinitionVersions(definition.getDefinitionVersions());
		this.startForm = definition.hasStartForm();
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

	public List<WfProcessVersion> getProcessVersions() {
		return processVersions;
	}

	public void setProcessVersions(List<WfProcessVersion> processVersions) {
		this.processVersions = processVersions;
	}

	private void fromDefinitionVersions(List<DefinitionVersion> processVersions) {

		if (processVersions == null)
			return;

		this.processVersions = new ArrayList<WfProcessVersion>();

		for (DefinitionVersion version : processVersions) {
			this.processVersions.add(new WfProcessVersion(version));
		}
	}

	public static List<WfProcess> fromWorkflowDefinitions(List<WorkflowDefinition> processes) {
		List<WfProcess> returnProcesses = new ArrayList<WfProcess>();
		for (WorkflowDefinition definition : processes) {
			returnProcesses.add(new WfProcess(definition));
		}
		return returnProcesses;
	}

	public List<WfFormProperty> getProcessForm() {
		return processForm;
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

	public void setProcessForm(List<WfFormProperty> processForm) {
		this.processForm = processForm;
	}

	public String getActiveDeploymentId() {
		return activeDeploymentId;
	}

	public void setActiveDeploymentId(String activeDeploymentId) {
		this.activeDeploymentId = activeDeploymentId;
	}

	public String getRegistryId() {
		return registryId;
	}

	public void setRegistryId(String registryId) {
		this.registryId = registryId;
	}

	public boolean hasStartForm() {
		return startForm;
	}

	public void setStartForm(boolean startForm) {
		this.startForm = startForm;
	}

}
