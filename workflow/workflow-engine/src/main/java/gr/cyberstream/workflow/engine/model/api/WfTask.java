/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.task.Task;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;

/**
 * Models a task object. Used in API communications.
 * 
 * @author nlyk
 */
public class WfTask {

	private String id;
	private String name;
	private String description;
	private String processDefinitionId;
	private String deploymentId;
	private List<FormProperty> taskForm;
	private int processId;

	public WfTask() {
	}

	public WfTask(Task task) {
		this.initFromTask(task);
	}

	public void initFromTask(Task task) {
		this.setId(task.getId());
		this.setName(task.getName());
		this.setDescription(task.getDescription());
		this.setProcessDefinitionId(task.getProcessDefinitionId());
	}

	public void initFromDefinitionVersion(DefinitionVersion version) {
		this.setProcessId(version.getWorkflowDefinition().getId());
		this.setDeploymentId(version.getDeploymentId());
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	/**
	 * Utility function to convert a list of activiti tasks to list of WfTasks
	 * 
	 * @param tasks
	 * @return
	 */
	static public List<WfTask> tasksToWfTasks(List<Task> tasks) {
		List<WfTask> wfTasks = new ArrayList<WfTask>();

		for (Task task : tasks) {
			wfTasks.add(new WfTask(task));
		}

		return wfTasks;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public List<FormProperty> getTaskForm() {
		return taskForm;
	}

	public void setTaskForm(List<FormProperty> taskForm) {
		this.taskForm = taskForm;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

}
