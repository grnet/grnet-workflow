package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;

import org.activiti.engine.form.AbstractFormType;

public class DocumentListFormType extends AbstractFormType {

	public static final String TYPE_NAME = "documentList";

	private static final long serialVersionUID = 310729831915399853L;

	@Override
	public String getName() {
		return TYPE_NAME;
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {

		DocumentListType docList;

		try {
			docList = DocumentListType.fromString(propertyValue);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return docList;
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {

		if (modelValue == null) {
			return null;
		}

		return modelValue.toString();
	}
}
