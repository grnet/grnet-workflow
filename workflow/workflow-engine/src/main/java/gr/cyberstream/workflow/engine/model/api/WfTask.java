/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	private List<WfFormProperty> taskForm;
	private int processId;
	private Date dueDate;
	private Date startDate;
	private WfProcessInstance processInstance;
	private String assignee;
	private String icon;
	private String definitionName;
	private WfTaskDetails taskDetails;
	private boolean completed;
	private Date endDate;
	private boolean startForm;

	public WfTask() {
		this.completed = false;
	}

	public WfTask(Task task) {
		this.setId(task.getId());
		this.setName(task.getName());
		this.setDescription(task.getDescription());
		this.setProcessDefinitionId(task.getProcessDefinitionId());
		this.setDueDate(task.getDueDate());
		this.setStartDate(task.getCreateTime());
		this.setAssignee(task.getAssignee());
		this.completed = false;
	}

	public WfTask(HistoricTaskInstance historicTask) {
		this.setId(historicTask.getId());
		this.setName(historicTask.getName());
		this.setDescription(historicTask.getDescription());
		this.setProcessDefinitionId(historicTask.getProcessDefinitionId());
		this.setDueDate(historicTask.getDueDate());
		this.setStartDate(historicTask.getCreateTime());
		this.setAssignee(historicTask.getAssignee());
		this.setEndDate(historicTask.getEndTime());
		this.completed = true;
	}

	public void initFromDefinitionVersion(DefinitionVersion version) {
		this.setProcessId(version.getWorkflowDefinition().getId());
		this.setDeploymentId(version.getDeploymentId());
	}

	@JsonIgnore
	public Map<String, String> getVariableValues() {

		if (taskForm == null) {
			return null;
		}

		Map<String, String> variableValues = new HashMap<String, String>();

		for (WfFormProperty property : taskForm) {

			if (property.isWritable())
				variableValues.put(property.getId(), property.getValue());
		}

		return variableValues;
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

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != getClass())
			return false;
		WfTask task = (WfTask) object;
		if (this.id.equals(task.getId()))
			return true;
		return false;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public List<WfFormProperty> getTaskForm() {
		return taskForm;
	}

	public void setTaskForm(List<WfFormProperty> taskForm) {
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

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public WfProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(WfProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
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

	public WfTaskDetails getTaskDetails() {
		return taskDetails;
	}

	public void setTaskDetails(WfTaskDetails taskDetails) {
		this.taskDetails = taskDetails;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isStartForm() {
		return startForm;
	}

	public void setStartForm(boolean startForm) {
		this.startForm = startForm;
	}
}