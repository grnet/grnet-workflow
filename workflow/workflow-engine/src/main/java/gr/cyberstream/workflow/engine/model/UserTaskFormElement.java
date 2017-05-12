package gr.cyberstream.workflow.engine.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistence class for the UserTaskFormElement table
 * 
 * @author kkoutros
 *
 */
@Entity
@Table(name = "UserTaskFormElement")
public class UserTaskFormElement {

	// static variables identifying client
	public static final String ALL_DEVICES = "ALL";
	public static final String MOBILE = "MOBILE";
	public static final String BROWSER = "BROWSER";

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

	private String device;

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

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof UserTaskFormElement) {
			UserTaskFormElement that = (UserTaskFormElement) other;
			result = (this.getId() == that.getId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		builder.append(description);
		builder.append(format);
		builder.append(elementId);
		builder.append(userTaskDetail);
		builder.append(device);
		return builder.toHashCode();
	}

}
