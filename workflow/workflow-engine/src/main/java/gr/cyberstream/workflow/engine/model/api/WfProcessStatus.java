package gr.cyberstream.workflow.engine.model.api;

import java.util.List;

public class WfProcessStatus {

	private String status;
	private String pendingTaskDescr;
	private List<WfTask> tasks;

	/**
	 * Default constructor
	 */
	public WfProcessStatus() {

	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPendingTaskDescr() {
		return pendingTaskDescr;
	}

	public void setPendingTaskDescr(String pendingTaskDescr) {
		this.pendingTaskDescr = pendingTaskDescr;
	}

	public List<WfTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<WfTask> tasks) {
		this.tasks = tasks;
	}

}
