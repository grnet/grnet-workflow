package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.List;

import gr.cyberstream.workflow.engine.model.ExternalForm;

public class WfExternalForm {

	private String id;
	private String supervisor;
	private String titleTemplate;
	private int workflowDefinitionId;
	private boolean enabled;
	
	public WfExternalForm(){}
	
	public WfExternalForm(ExternalForm externalForm){
		this.id = externalForm.getId();
		this.supervisor = externalForm.getSupervisor();
		this.titleTemplate = externalForm.getTitleTemplate();
		this.workflowDefinitionId = externalForm.getWorkflowDefinition().getId();
		this.enabled = externalForm.isEnabled();
	}
	
	public static List<WfExternalForm> fromExternalForms(List<ExternalForm> forms){
		
		List<WfExternalForm> wfForms = new ArrayList<WfExternalForm>();		
		for(ExternalForm form : forms){
			wfForms.add(new WfExternalForm(form));			
		}
		
		return wfForms;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSupervisor() {
		return supervisor;
	}
	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}
	public String getTitleTemplate() {
		return titleTemplate;
	}
	public void setTitleTemplate(String titleTemplate) {
		this.titleTemplate = titleTemplate;
	}
	public int getWorkflowDefinitionId() {
		return workflowDefinitionId;
	}
	public void setWorkflowDefinitionId(int workflowDefinitionId) {
		this.workflowDefinitionId = workflowDefinitionId;
	}

	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
		
}
