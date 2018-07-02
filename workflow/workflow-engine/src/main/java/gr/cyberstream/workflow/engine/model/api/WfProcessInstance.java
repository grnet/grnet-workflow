package gr.cyberstream.workflow.engine.model.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gr.cyberstream.workflow.engine.model.WorkflowInstance;

public class WfProcessInstance {

	private String id;
	private String title;
	private String folderId;
	private String definitionVersionId;
	private String supervisor;
	private List<WfFormProperty> processForm;
	private int version;
	private Date startDate;
	private String status;
	private Date endDate;
	private String reference;
	private String captchaHash;
	private String captchaAnswer;
	private String definitionIcon;
	private String definitionName;

	@JsonIgnore
	private String client;

	public WfProcessInstance() {
	}

	public WfProcessInstance(String id, String title, String folderId, String definitionVersionId, String supervisor,
			List<WfFormProperty> processForm, String status, int version, Date startDate, String reference) {

		this.id = id;
		this.title = title;
		this.folderId = folderId;
		this.definitionVersionId = definitionVersionId;
		this.supervisor = supervisor;
		this.processForm = processForm;
		this.status = status;
		this.version = version;
		this.startDate = startDate;
		this.reference = reference;
	}

	public WfProcessInstance(WorkflowInstance instance) {
		this.setId(instance.getId());
		this.setTitle(instance.getTitle());
		this.setFolderId(instance.getFolderId());
		this.setSupervisor(instance.getSupervisor());
		this.setDefinitionVersionId(String.valueOf(instance.getDefinitionVersion().getId()));
		this.setVersion(instance.getDefinitionVersion().getVersion());
		this.setStartDate(instance.getStartDate());
		this.setStatus(instance.getStatus());
		this.setEndDate(instance.getEndDate());
		this.setReference(instance.getReference());
		this.setDefinitionIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
		this.definitionName = instance.getDefinitionVersion().getWorkflowDefinition().getName();
	}

	public static List<WfProcessInstance> fromWorkflowInstances(List<WorkflowInstance> workflowInstances) {
		List<WfProcessInstance> instances = new ArrayList<WfProcessInstance>();
		for (WorkflowInstance instance : workflowInstances) {
			instances.add(new WfProcessInstance(instance));
		}
		return instances;
	}

	@JsonIgnore
	public Map<String, String> getVariableValues() {

		if (processForm == null) {
			return null;
		}

		Map<String, String> variableValues = new HashMap<String, String>();

		for (WfFormProperty property : processForm) {

			variableValues.put(property.getId(), property.getValue());
		}

		return variableValues;
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

	public String getDefinitionVersionId() {
		return definitionVersionId;
	}

	public void setDefinitionVersionId(String definitionVersionId) {
		this.definitionVersionId = definitionVersionId;
	}

	public String getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<WfFormProperty> getProcessForm() {
		return processForm;
	}

	public void setProcessForm(List<WfFormProperty> processForm) {
		this.processForm = processForm;
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

	public String getCaptchaHash() {
		return captchaHash;
	}

	public void setCaptchaHash(String captchaHash) {
		this.captchaHash = captchaHash;
	}

	public String getCaptchaAnswer() {
		return captchaAnswer;
	}

	public void setCaptchaAnswer(String captchaAnswer) {
		this.captchaAnswer = captchaAnswer;
	}

	public String getDefinitionIcon() {
		return definitionIcon;
	}

	public void setDefinitionIcon(String definitionIcon) {
		this.definitionIcon = definitionIcon;
	}

	public String getDefinitionName() {
		return definitionName;
	}

	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}
}