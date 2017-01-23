package gr.cyberstream.workflow.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the DefinitionVersion database table.
 * 
 * @author nlyk
 */
@Entity
public class DefinitionVersion implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "deployment_id")
	private String deploymentId;

	private Integer version;

	private Date deploymentdate;

	@Column(name = "process_definition_id")
	private String processDefinitionId;

	private String status;

	private String justification;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "workflow_definition_id")
	private WorkflowDefinition workflowDefinition;

	@OneToMany(mappedBy = "definitionVersion", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserTaskDetails> tasks;

	/**
	 * Default constructor
	 */
	public DefinitionVersion() {

	}

	/**
	 * A copy constructor using a {@link WfProcessVersion} object
	 * 
	 * @param wfProcessVersion
	 *            {@link WfProcessVersion} to be converted
	 */
	public void updateFrom(WfProcessVersion wfProcessVersion) {
		this.deploymentId = wfProcessVersion.getDeploymentId();
		this.deploymentdate = wfProcessVersion.getDeploymentdate();
		this.processDefinitionId = wfProcessVersion.getProcessDefinitionId();
		this.status = wfProcessVersion.getStatus();
		this.justification = wfProcessVersion.getJustification();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Date getDeploymentdate() {
		return deploymentdate;
	}

	public void setDeploymentdate(Date deploymentdate) {
		this.deploymentdate = deploymentdate;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public WorkflowDefinition getWorkflowDefinition() {
		return workflowDefinition;
	}

	public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
		this.workflowDefinition = workflowDefinition;
	}

	public String getJustification() { return justification; }

	public void setJustification(String justification) { this.justification = justification; }

	public List<UserTaskDetails> getTasks() {
		return tasks;
	}

	public void setTasks(List<UserTaskDetails> tasks) {
		this.tasks = tasks;
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof DefinitionVersion) {
			DefinitionVersion that = (DefinitionVersion) other;
			result = (this.getId() == that.getId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		builder.append(deploymentId);
		builder.append(version);
		builder.append(deploymentdate);
		builder.append(processDefinitionId);
		builder.append(status);
		builder.append(workflowDefinition);
		builder.append(justification);
		builder.append(tasks);
		return builder.toHashCode();
	}

}