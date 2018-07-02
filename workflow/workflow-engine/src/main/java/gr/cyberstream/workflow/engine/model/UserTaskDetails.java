package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;

/**
 * The persistence class for the UserTaskDetails table
 * 
 * @author vpap
 *
 */
@Entity
@Table(name = "UserTaskDetails")
public class UserTaskDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String USER_TASK = "USER_TASK";
	public static final String START_EVENT_TASK = "START_EVENT";

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

	// TODO: REFACTOR RENAME TO assignBySuperVisor
	private boolean assign;

	@OneToMany(mappedBy = "userTaskDetail", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserTaskFormElement> userTaskFormElements;

	private String type;

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

	public List<UserTaskFormElement> getUserTaskFormElements() {
		return userTaskFormElements;
	}

	public void setUserTaskFormElements(List<UserTaskFormElement> userTaskFormElements) {
		this.userTaskFormElements = userTaskFormElements;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void updateFrom(WfTaskDetails wfTaskDetails) {
		this.description = wfTaskDetails.getDescription();
		this.assign = wfTaskDetails.isAssign();
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof UserTaskDetails) {
			UserTaskDetails that = (UserTaskDetails) other;
			result = (this.getId() == that.getId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		builder.append(taskId);
		builder.append(description);
		builder.append(name);
		builder.append(definitionVersion);
		builder.append(assign);
		builder.append(userTaskFormElements);
		builder.append(type);
		return builder.toHashCode();
	}

}
