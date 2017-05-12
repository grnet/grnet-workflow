/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	private Date deploymentdate;
	private String deploymentId;
	private String processDefinitionId;
	private int workflowDefinitionId;

	public WfProcessVersion() {
	}

	public WfProcessVersion(DefinitionVersion version) {
		this.setId(version.getId());
		this.setVersion(version.getVersion());
		this.setStatus(version.getStatus());
		this.setDeploymentdate(version.getDeploymentdate());
		this.setDeploymentId(version.getDeploymentId());
		this.setProcessDefinitionId(version.getProcessDefinitionId());
		this.setWorkflowDefinitionId(version.getWorkflowDefinition().getId());
	}

	public static List<WfProcessVersion> fromDefinitionVersions(List<DefinitionVersion> versions) {
		List<WfProcessVersion> returnVersions = new ArrayList<WfProcessVersion>();
		for (DefinitionVersion version : versions) {
			returnVersions.add(new WfProcessVersion(version));
		}
		return returnVersions;
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

	public int getWorkflowDefinitionId() {
		return workflowDefinitionId;
	}

	public void setWorkflowDefinitionId(int workflowDefinitionId) {
		this.workflowDefinitionId = workflowDefinitionId;
	}

}
