/**
 * @author nlyk
 */
package gr.cyberstream.workflow.designer.usertasks;

import org.activiti.designer.integration.annotation.Help;
import org.activiti.designer.integration.annotation.Property;
import org.activiti.designer.integration.servicetask.PropertyType;
import org.activiti.designer.integration.usertask.AbstractCustomUserTask;

public class SubmitDocument extends AbstractCustomUserTask {

	@Property(type = PropertyType.TEXT, displayName = "Document Title", required = true)
	@Help(displayHelpShort = "Provide a title for the document", displayHelpLong = "Use a title for the document which is valid as filesystem filename. Do not use special characters as \".\", \";\", etc.")
	private String documentTitle;

	@Property(type = PropertyType.TEXT, displayName = "Document Variable", required = true)
	@Help(displayHelpShort = "Document variable", displayHelpLong = "The variable name of the submitted document.")
	private String documentVar;

	@Property(type = PropertyType.TEXT, displayName = "Document Template", required = false)
	@Help(displayHelpShort = "The path of a document to be used as template", displayHelpLong = "Give the path to a document to be used as template. It's better to ensure that this document is read only.")
	private String templateDocument;

	@Override
	public String getName() {
		return "Submit Document";
	}

	@Override
	public String getSmallIconPath() {
		return "icons/document.png";
	}

	@Override
	public String contributeToPaletteDrawer() {
		return "WorkStream User Tasks";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/document-large.png";
	}

}
