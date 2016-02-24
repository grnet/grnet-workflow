/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.task.Task;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.UserTaskDetails;

/**
 * Models a task object. Used in API communications.
 * 
 * @author nlyk
 */
public class WfTaskDetails {

	private int id;
	private String name;
	private String description;
	private int definitionVersionId;
	private String taskId;
	private boolean assign;	


	public WfTaskDetails() {
	}

	public WfTaskDetails(UserTaskDetails task) {
		this.setId(task.getId());
		this.setName(task.getName());
		this.setDescription(task.getDescription());
		this.setDefinitionVersionId(task.getDefinitionVersion().getId());
		this.setTaskId(task.getTaskId());
		this.assign = task.isAssign();
	}
	
	
	public static List<WfTaskDetails> fromUserTaskDetails(List<UserTaskDetails> taskDetails){
		List<WfTaskDetails> wfTaskDetails = new ArrayList<WfTaskDetails>();
		for(UserTaskDetails td : taskDetails){
			wfTaskDetails.add(new WfTaskDetails(td));
		}
		return wfTaskDetails;		
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

	
	
	public int getDefinitionVersionId() {
		return definitionVersionId;
	}

	public void setDefinitionVersionId(int definitionVersionId) {
		this.definitionVersionId = definitionVersionId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public boolean isAssign() {
		return assign;
	}

	public void setAssign(boolean assign) {
		this.assign = assign;
	}

}
