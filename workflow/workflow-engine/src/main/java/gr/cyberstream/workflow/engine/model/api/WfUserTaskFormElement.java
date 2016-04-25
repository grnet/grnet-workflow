package gr.cyberstream.workflow.engine.model.api;

import gr.cyberstream.workflow.engine.model.UserTaskFormElement;

public class WfUserTaskFormElement {

	private String description;
	private String format;

	public WfUserTaskFormElement() {

	}

	public WfUserTaskFormElement(UserTaskFormElement userTaskFormElement) {
		setDescription(userTaskFormElement.getDescription());
		setFormat(userTaskFormElement.getFormat());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
