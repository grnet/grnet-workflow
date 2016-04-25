/**
 * @author nlyk
 */
package gr.cyberstream.workflow.designer.usertasks;

import org.activiti.designer.integration.annotation.Help;
import org.activiti.designer.integration.annotation.Property;
import org.activiti.designer.integration.servicetask.PropertyType;
import org.activiti.designer.integration.usertask.AbstractCustomUserTask;

public class SubmitExternalDocument extends AbstractCustomUserTask {

	@Property(type = PropertyType.TEXT, displayName = "Document Title", required = true)
	@Help(displayHelpShort = "Provide a title for the document", displayHelpLong = "Use a title for the document which is valid as filesystem filename. Do not use special characters as \".\", \";\", etc.")
	private String documentTitle;

	@Property(type = PropertyType.TEXT, displayName = "Document Variable", required = true)
	@Help(displayHelpShort = "Document variable", displayHelpLong = "The variable name of the submitted document.")
	private String documentVar;

	@Property(type = PropertyType.TEXT, displayName = "Source Variable", required = false)
	@Help(displayHelpShort = "Document source variable", displayHelpLong = "Specify the variable of the source of the external document (incoming).")
	private String sourceVar;

	@Override
	public String getName() {
		return "Submit External Document";
	}

	@Override
	public String getSmallIconPath() {
		return "icons/extdocument.png";
	}

	@Override
	public String contributeToPaletteDrawer() {
		return "WorkStream User Tasks";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/extdocument-large.png";
	}

}
