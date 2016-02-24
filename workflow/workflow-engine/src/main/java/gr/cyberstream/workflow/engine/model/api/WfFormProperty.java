package gr.cyberstream.workflow.engine.model.api;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = FormPropertyDeserializer.class)
public class WfFormProperty {

	private String id;
	private String name;
	private String type;
	private String value;
	
	private boolean readable;
	private boolean writable;
	private boolean required;
	
	private Map<String, String> formValues;
	
	public WfFormProperty() {
	}

	public WfFormProperty(String id, String name, String type, String value, boolean readable, boolean writable, boolean required, Map<String, String> formValues) {
	
		this.id = id;
		this.name = name;
		this.type = type;
		this.value = value;
		this.readable = readable;
		this.writable = writable;
		this.required = required;
		
		this.formValues = formValues;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public boolean isReadable() {
		return readable;
	}

	public boolean isWritable() {
		return writable;
	}

	public boolean isRequired() {
		return required;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public Map<String, String> getFormValues() {
		return formValues;
	}

	public void setFormValues(Map<String, String> formValues) {
		this.formValues = formValues;
	}
}