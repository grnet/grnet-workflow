/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;

import org.activiti.engine.form.AbstractFormType;

public class DocumentFormType extends AbstractFormType {

	public static final String TYPE_NAME = "document";

	private static final long serialVersionUID = -7478012268318441014L;

	@Override
	public String getName() {
		return TYPE_NAME;
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {

		DocumentType doc;

		try {
			doc = DocumentType.fromString(propertyValue);
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
