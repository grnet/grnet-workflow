package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;

/**
 * The persistent class for the process instance database table.
 * 
 * @author gtyl
 */
@Entity
@Table(name = "Instance")
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

	private String reference;
	
	@Transient
	public static String STATUS_RUNNING = "running";

	@Transient
	public static String STATUS_SUSPENDED = "suspended";
	
	@Transient
	public static String STATUS_ENDED = "ended";

	
	public void updateFrom(WfProcessInstance wfProcessInstance) {
		this.setTitle(wfProcessInstance.getTitle());
		this.setSupervisor(wfProcessInstance.getSupervisor());
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

	public String getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
}
