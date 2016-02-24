/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;

import org.activiti.engine.form.AbstractFormType;

public class MessageFormType extends AbstractFormType {

	private static final long serialVersionUID = -8955839243713097969L;
	
	public static final String TYPE_NAME = "message";

	@Override
	public String getName() {
		return TYPE_NAME;
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {

		MessageType message;

		try {
			message = MessageType.fromString(propertyValue);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return message;
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {

		if (modelValue == null) {
			return null;
		}

		return modelValue.toString();
	}

}
