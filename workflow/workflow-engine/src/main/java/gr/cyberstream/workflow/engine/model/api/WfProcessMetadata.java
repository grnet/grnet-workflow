package gr.cyberstream.workflow.engine.model.api;

import java.util.List;

public class WfProcessMetadata {

	private String name;
	private String icon;
	private String description;
	
	private String captchaImage;
	private String captchaHash;
	
	private List<WfFormProperty> processForm;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCaptchaImage() {
		return captchaImage;
	}

	public void setCaptchaImage(String captchaImage) {
		this.captchaImage = captchaImage;
	}

	public String getCaptchaHash() {
		return captchaHash;
	}

	public void setCaptchaHash(String captchaHash) {
		this.captchaHash = captchaHash;
	}

	public List<WfFormProperty> getProcessForm() {
		return processForm;
	}

	public void setProcessForm(List<WfFormProperty> processForm) {
		this.processForm = processForm;
	}
}
