package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;

import org.activiti.engine.form.AbstractFormType;

public class ApproveFormType extends AbstractFormType {

	public static final String TYPE_NAME = "approve";

	private static final long serialVersionUID = 6071141952076174570L;
	
	@Override
	public String getName() {
		return TYPE_NAME;
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {

		ApproveType doc;

		try {
			doc = ApproveType.fromString(propertyValue);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return doc;
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {

		if (modelValue == null) {
			return null;
		}

		return modelValue.toString();
	}
}
