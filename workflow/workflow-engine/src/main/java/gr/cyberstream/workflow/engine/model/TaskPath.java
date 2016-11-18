package gr.cyberstream.workflow.engine.model;

/**
 * A wrap class that contains task details
 * 
 * @author kkoutros
 *
 */
public class TaskPath {

	private UserTaskDetails taskDetails;
	private WorkflowInstance instance;
	private DefinitionVersion definitionVersion;
	private WorkflowDefinition definition;

	public TaskPath() {

	}

	public TaskPath(UserTaskDetails taskDetails, WorkflowInstance instance, DefinitionVersion definitionVersion, WorkflowDefinition definition) {
		this.taskDetails = taskDetails;
		this.instance = instance;
		this.definitionVersion = definitionVersion;
		this.definition = definition;
	}

	public UserTaskDetails getTaskDetails() {
		return taskDetails;
	}

	public void setTaskDetails(UserTaskDetails taskDetails) {
		this.taskDetails = taskDetails;
	}

	public WorkflowInstance getInstance() {
		return instance;
	}

	public void setInstance(WorkflowInstance instance) {
		this.instance = instance;
	}

	public DefinitionVersion getDefinitionVersion() {
		return definitionVersion;
	}

	public void setDefinitionVersion(DefinitionVersion definitionVersion) {
		this.definitionVersion = definitionVersion;
	}

	public WorkflowDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(WorkflowDefinition definition) {
		this.definition = definition;
	}
}
