package gr.cyberstream.workflow.engine.model.api;

import gr.cyberstream.workflow.engine.model.UserTaskFormElement;

public class WfUserTaskFormElement {

	private String description;
	private String format;
	private String device;

	public WfUserTaskFormElement() {

	}

	public WfUserTaskFormElement(UserTaskFormElement userTaskFormElement) {
		this.description = userTaskFormElement.getDescription();
		this.format = userTaskFormElement.getFormat();
		this.device = userTaskFormElement.getDevice();
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

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

}
