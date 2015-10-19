package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the DefinitionVersion database table.
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

	@JsonIgnore	// is responsible for avoiding JSON convert infinite loop
	@ManyToOne
	@JoinColumn(name = "workflow_definition_id")
	private WorkflowDefinition workflowDefinition;

	public DefinitionVersion() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDeploymentId() {
		return this.deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public String getStatus() {
		return this.status;
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

}