package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.HashCodeBuilder;

import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;

/**
 * The persistent class for the WorkflowInstance database table.
 * 
 * @author gtyl
 */
@Entity
public class WorkflowInstance implements Serializable {

	private static final long serialVersionUID = 7477832256955969149L;

	@Id
	private String id;

	private String title;

	@Column(name = "folder_id")
	private String folderId;

	@ManyToOne
	@JoinColumn(name = "definition_version_id")
	private DefinitionVersion definitionVersion;

	private String status;

	private String supervisor;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Transient
	public static String STATUS_RUNNING = "running";

	@Transient
	public static String STATUS_SUSPENDED = "suspended";

	@Transient
	public static String STATUS_ENDED = "ended";

	@Transient
	public static String STATUS_DELETED = "deleted";

	/**
	 * Default constructor
	 */
	public WorkflowInstance() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public DefinitionVersion getDefinitionVersion() {
		return definitionVersion;
	}

	public void setDefinitionVersion(DefinitionVersion definitionVersion) {
		this.definitionVersion = definitionVersion;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void updateFrom(WfProcessInstance wfProcessInstance) {
		this.title = wfProcessInstance.getTitle();
		this.supervisor = wfProcessInstance.getSupervisor();
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof WorkflowInstance) {
			WorkflowInstance that = (WorkflowInstance) other;
			result = (this.getId() == that.getId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		builder.append(title);
		builder.append(folderId);
		builder.append(definitionVersion);
		builder.append(status);
		builder.append(supervisor);
		builder.append(startDate);
		builder.append(endDate);
		return builder.toHashCode();
	}
}
