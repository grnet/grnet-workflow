/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.model.api;

import java.util.Date;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;

/**
 * Used to communicate Process Versions through the API functions.
 * 
 * @author nlyk
 */
public class WfProcessVersion {

	private int id;
	private int version;
	private String status;
	private String deploymentId;
	private String processDefinitionId;
	private Date deploymentDate;

	public WfProcessVersion() {
	}

	public WfProcessVersion(DefinitionVersion version) {
		this.setId(version.getId());
		this.setVersion(version.getVersion());
		this.setStatus(version.getStatus());
		this.setDeploymentId(version.getDeploymentId());
		this.setDeploymentDate(version.getDeploymentdate());
		this.setProcessDefinitionId(version.getProcessDefinitionId());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public Date getDeploymentDate() {
		return deploymentDate;
	}

	public void setDeploymentDate(Date deploymentDate) {
		this.deploymentDate = deploymentDate;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}
}
