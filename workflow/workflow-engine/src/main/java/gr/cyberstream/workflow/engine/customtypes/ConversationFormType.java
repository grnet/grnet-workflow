/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.form.AbstractFormType;

public class ConversationFormType extends AbstractFormType {

	public static final String TYPE_NAME = "conversation";

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return TYPE_NAME;
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {

		List<MessageType> conversation;

		try {
			conversation = MessageType.listFromString(propertyValue);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return conversation;
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {

		if (modelValue == null) {
			return null;
		}

		return modelValue.toString();
	}

}
