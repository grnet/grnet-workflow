package gr.cyberstream.workflow.engine.model.api;

import gr.cyberstream.workflow.engine.model.ExternalForm;

/**
 * Models an external service object. Used in API communications.
 * 
 * @author kkoutros
 *
 */
public class WfPublicService {

	private String definitionName;
	private String definitionDescription;
	private String externalFormId;
	private String icon;

	public WfPublicService() {

	}

	public WfPublicService(ExternalForm externalForm) {
		this.definitionName = externalForm.getWorkflowDefinition().getName();
		this.definitionDescription = externalForm.getWorkflowDefinition().getDescription();
		this.externalFormId = externalForm.getId();
		this.icon = externalForm.getWorkflowDefinition().getIcon();
	}

	public String getDefinitionName() {
		return definitionName;
	}

	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}

	public String getDefinitionDescription() {
		return definitionDescription;
	}

	public void setDefinitionDescription(String definitionDescription) {
		this.definitionDescription = definitionDescription;
	}

	public String getExternalFormId() {
		return externalFormId;
	}

	public void setExternalFormId(String externalFormId) {
		this.externalFormId = externalFormId;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}
