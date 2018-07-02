package gr.cyberstream.workflow.engine.customtypes;

import org.activiti.engine.form.AbstractFormType;

public class TextareaFormType extends AbstractFormType {

	private static final long serialVersionUID = 357812211413563465L;

	public static final String TYPE_NAME = "textarea";

	@Override
	public String getName() {
		return TYPE_NAME;
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {
		return propertyValue;
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {
		return (String) modelValue;
	}
}
