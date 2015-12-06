/**
 * @author nlyk
 */
package gr.cyberstream.workflow.designer.usertasks;

import org.activiti.designer.integration.annotation.Help;
import org.activiti.designer.integration.annotation.Property;
import org.activiti.designer.integration.servicetask.PropertyType;
import org.activiti.designer.integration.usertask.AbstractCustomUserTask;

public class ApproveDocument extends AbstractCustomUserTask {
	
	@Property(type = PropertyType.TEXT, displayName = "Document Id", required = true)
	@Help(displayHelpShort = "Unique document id", displayHelpLong = "The document id for the document to be approved")
	private String documentId;

	@Property(type = PropertyType.MULTILINE_TEXT, displayName = "Instructions", required = false)
	@Help(displayHelpShort = "Give instructions", displayHelpLong = "In this field you can give instructions and information about the approval of the document")
	private String instructions;

	@Property(type = PropertyType.TEXT, displayName = "Conversation", required = false)
	@Help(displayHelpShort = "Set a conversation thread", displayHelpLong = "Set the conversation thread name (id). The entries to this conversation will be retained during the filetime of the process.")
	private String conversation;

	@Property(type = PropertyType.MULTILINE_TEXT, displayName = "Comments", required = false)
	@Help(displayHelpShort = "Comments on the document", displayHelpLong = "Write your comments")
	private String comments;

	@Property(type = PropertyType.TEXT, displayName = "Approval id", required = true, defaultValue = "final_approval")
	@Help(displayHelpShort = "Approval unique id", displayHelpLong = "Give a unique id for the approval of the document. The id has to be unique in the context of ths specific document approval.")
	private String approvalId;

	@Override
	public String getName() {
		return "Approve Document";
	}

	@Override
	public String getSmallIconPath() {
		return "icons/approve.png";
	}

	@Override
	public String contributeToPaletteDrawer() {
		return "GRNET-Workflow User Tasks";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/approve-large.png";
	}

}
