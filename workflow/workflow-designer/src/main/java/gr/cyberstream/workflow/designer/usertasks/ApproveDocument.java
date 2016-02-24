/**
 * @author nlyk
 */
package gr.cyberstream.workflow.designer.usertasks;

import org.activiti.designer.integration.annotation.Help;
import org.activiti.designer.integration.annotation.Property;
import org.activiti.designer.integration.servicetask.PropertyType;
import org.activiti.designer.integration.usertask.AbstractCustomUserTask;

public class ApproveDocument extends AbstractCustomUserTask {
	
	@Property(type = PropertyType.TEXT, displayName = "Document Variable", required = true)
	@Help(displayHelpShort = "Document variable", displayHelpLong = "The variable name of the document to be approved.")
	private String documentVar;

	@Property(type = PropertyType.TEXT, displayName = "Approval Variable", required = true)
	@Help(displayHelpShort = "Approval variable name", displayHelpLong = "Give a unique variable name for the approval of the document.")
	private String approvalVar;

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
		return "WorkStream User Tasks";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/approve-large.png";
	}

}
