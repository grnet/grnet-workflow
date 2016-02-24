package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;

import org.activiti.engine.form.AbstractFormType;

public class PositionFormType extends AbstractFormType {

	private static final long serialVersionUID = 6019280536900072546L;

	public static final String TYPE_NAME = "position";
	
	@Override
	public String getName() {
		return TYPE_NAME;
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {

		PositionType position;

		try {
			position = PositionType.fromString(propertyValue);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return position;
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {
		
		if (modelValue == null) {
			return null;
		}
		
		return modelValue.toString();
	}

}
