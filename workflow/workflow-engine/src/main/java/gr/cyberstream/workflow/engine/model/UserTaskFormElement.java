package gr.cyberstream.workflow.engine.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistence class for the usertaskformelements table
 * 
 * @author kkoutros
 *
 */
@Entity
public class UserTaskFormElement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String description;
	private String format;

	private String elementId;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "taskDetail")
	private UserTaskDetails userTaskDetail;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public UserTaskDetails getUserTaskDetail() {
		return userTaskDetail;
	}

	public void setUserTaskDetail(UserTaskDetails userTaskDetail) {
		this.userTaskDetail = userTaskDetail;
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

}
