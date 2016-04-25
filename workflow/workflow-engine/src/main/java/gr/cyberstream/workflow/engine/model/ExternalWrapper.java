package gr.cyberstream.workflow.engine.model;

/**
 * 
 * @author kkoutros
 *
 */
public class ExternalWrapper {

	private ExternalGroup externalGroup;
	private ExternalForm externalForm;

	public ExternalWrapper() {
	}
	
	public ExternalWrapper(ExternalForm externalForm, ExternalGroup externalGroup) {
		this.externalGroup = externalGroup;
		this.externalForm = externalForm;
	}

	public ExternalWrapper(ExternalGroup externalGroup) {
		this.externalGroup = externalGroup;
	}

	public ExternalWrapper(ExternalForm externalForm) {
		this.externalForm = externalForm;
	}

	public ExternalGroup getExternalGroup() {
		return externalGroup;
	}

	public void setExternalGroup(ExternalGroup externalGroup) {
		this.externalGroup = externalGroup;
	}

	public ExternalForm getExternalForm() {
		return externalForm;
	}

	public void setExternalForm(ExternalForm externalForm) {
		this.externalForm = externalForm;
	}

}
