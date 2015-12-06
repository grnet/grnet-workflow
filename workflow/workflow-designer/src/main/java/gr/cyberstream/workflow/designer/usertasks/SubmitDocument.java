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

	@Property(type = PropertyType.TEXT, displayName = "Document Id", required = true)
	@Help(displayHelpShort = "Unique document id", displayHelpLong = "Provide a unique id for the document within the context of the process.")
	private String documentId;

	@Property(type = PropertyType.TEXT, displayName = "Reference number", required = false)
	@Help(displayHelpShort = "Document reference number", displayHelpLong = "Official document reference number provided when registering the document.")
	private String refNo;

	@Property(type = PropertyType.MULTILINE_TEXT, displayName = "Instructions", required = false)
	@Help(displayHelpShort = "Give instructions", displayHelpLong = "In this field you can give instructions and information about the editing of the document")
	private String instructions;

	@Property(type = PropertyType.TEXT, displayName = "Document Template", required = false)
	@Help(displayHelpShort = "The path of a document to be used as template", displayHelpLong = "Give the path to a document to be used as template. It's better to ensure that this document is read only.")
	private String templateDocument;

	@Property(type = PropertyType.TEXT, displayName = "Conversation", required = false)
	@Help(displayHelpShort = "Set a conversation thread", displayHelpLong = "Set the conversation thread name (id). The entries to this conversation will be retained during the filetime of the process.")
	private String conversation;

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
		return "GRNET-Workflow User Tasks";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/document-large.png";
	}

}
