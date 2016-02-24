package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;

/**
 * The persistence class for the usertasksdetails table
 * @author vpap
 *
 */

@Entity
public class UserTaskDetails implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "task_id")
	private String taskId;
	
	private String description;	
	private String name;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "definitionversion_id")
	private DefinitionVersion definitionVersion;
	
	//TODO: REFACTOR RENAME TO assignBySuperVisor
	private boolean assign;
	
	
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DefinitionVersion getDefinitionVersion() {
		return definitionVersion;
	}

	public void setDefinitionVersion(DefinitionVersion definitionVersion) {
		this.definitionVersion = definitionVersion;
	}


	public boolean isAssign() {
		return assign;
	}

	public void setAssign(boolean assign) {
		this.assign = assign;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	public void updateFrom(WfTaskDetails wfTaskDetails){
		this.setDescription(wfTaskDetails.getDescription());
		this.setAssign(wfTaskDetails.isAssign());
	}
	
}
