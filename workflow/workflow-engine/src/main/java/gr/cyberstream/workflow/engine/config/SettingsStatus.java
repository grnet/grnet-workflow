package gr.cyberstream.workflow.engine.config;

import gr.cyberstream.workflow.engine.model.WorkflowSettings;

public class SettingsStatus {

	private WorkflowSettings workflowSettings;

	public SettingsStatus() {
	}

	public WorkflowSettings getWorkflowSettings() {
		return workflowSettings;
	}

	public void setWorkflowSettings(WorkflowSettings workflowSettings) {
		this.workflowSettings = workflowSettings;
	}
}
