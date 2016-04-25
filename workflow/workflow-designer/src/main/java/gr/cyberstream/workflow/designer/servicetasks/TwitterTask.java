/**
 * @author nlyk
 */
package gr.cyberstream.workflow.designer.servicetasks;

import org.activiti.designer.integration.annotation.Help;
import org.activiti.designer.integration.annotation.Property;
import org.activiti.designer.integration.annotation.Runtime;
import org.activiti.designer.integration.servicetask.AbstractCustomServiceTask;
import org.activiti.designer.integration.servicetask.PropertyType;

@Runtime(javaDelegateClass = "gr.cyberstream.workflow.engine.customservicetasks.TwitterService")
public class TwitterTask extends AbstractCustomServiceTask {

	@Property(type = PropertyType.TEXT, displayName = "Twitter Status", required = true)
	@Help(displayHelpShort = "Provide Twitter message", displayHelpLong = "Add the text message you want (up to 140 characters)")
	private String status;

	@Override
	public String getName() {
		return "Twitter";
	}

	@Override
	public String getSmallIconPath() {
		return "icons/twitter.png";
	}

	@Override
	public String contributeToPaletteDrawer() {
		return "WorkStream Service Tasks";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/twitter.png";
	}
		
}
