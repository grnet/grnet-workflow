package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.form.FormProperty;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;

/**
 * Models a workflow definition object enhanced with form information. Used in API communications.
 * 
 * @author nlyk
 */
public class WfProcess { // extends WorkflowDefinition {

	private int id;
	private String name;
	private String description;
	private String icon;
	private String processDefinitionId;
	private boolean isActive;
	private List<WfProcessVersion> processVersions;
	private List<FormProperty> processForm;

	public WfProcess() {
	}

	public WfProcess(WorkflowDefinition definition) {
		this.setId(definition.getId());
		this.setName(definition.getName());
		this.setDescription(definition.getDescription());
		this.setIcon(definition.getIcon());
		this.setProcessDefinitionId(definition.getKey());
		this.setActive(definition.isSelectedVersionActive());

		this.initProcessVersions(definition.getDefinitionVersions());
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
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

	public void initProcessVersions(List<DefinitionVersion> processVersions) {
		if (processVersions != null && processVersions.size() > 0) {
			this.processVersions = new ArrayList<WfProcessVersion>();
		} else {
			this.processVersions = null;
			return;
		}

		for (DefinitionVersion version : processVersions) {
			this.processVersions.add(new WfProcessVersion(version));
		}
	}

	public List<FormProperty> getProcessForm() {
		return processForm;
	}

	public void setProcessForm(List<FormProperty> processForm) {
		this.processForm = processForm;
	}
}
