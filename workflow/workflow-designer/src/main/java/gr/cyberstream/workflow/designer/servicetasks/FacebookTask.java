package gr.cyberstream.workflow.designer.servicetasks;

import org.activiti.designer.integration.annotation.Help;
import org.activiti.designer.integration.annotation.Property;
import org.activiti.designer.integration.annotation.Runtime;
import org.activiti.designer.integration.servicetask.AbstractCustomServiceTask;
import org.activiti.designer.integration.servicetask.PropertyType;

@Runtime(javaDelegateExpression= "${facebookService}")
public class FacebookTask extends AbstractCustomServiceTask{

	@Property(type = PropertyType.TEXT, displayName = "Facebook Status", required = true)
	@Help(displayHelpShort = "Provide Facebook status text", displayHelpLong = "Add the text you want")
	private String status;
	
	@Property(type = PropertyType.TEXT, displayName = "Facebook Page", required = true)
	@Help(displayHelpShort = "Facebook page name", displayHelpLong = "Add the name of the facebook managed "
			+ "page where you want to post")
	private String page;
	
	@Override
	public String getName() {
		return "Facebook";
	}

	@Override
	public String getSmallIconPath() {
		return "icons/fb.png";
	}

	@Override
	public String contributeToPaletteDrawer() {
		return "WorkStream Service Tasks";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/fb-large.png";
	}

}
