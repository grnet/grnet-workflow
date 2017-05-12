package gr.cyberstream.workflow.designer.servicetasks;

import org.activiti.designer.integration.annotation.Help;
import org.activiti.designer.integration.annotation.Property;
import org.activiti.designer.integration.annotation.Runtime;
import org.activiti.designer.integration.servicetask.AbstractCustomServiceTask;
import org.activiti.designer.integration.servicetask.PropertyType;

@Runtime(javaDelegateExpression = "${documentMail}")
@Help(displayHelpShort = "Sends Mail with attached document", displayHelpLong = "Sends Mail with attached document")
public class DocumentMail extends AbstractCustomServiceTask {

	@Property(type = PropertyType.TEXT, displayName = "template id", required = true)
	private String app;

	@Property(type = PropertyType.TEXT, displayName = "from", required = true)
	private String from;

	@Property(type = PropertyType.TEXT, displayName = "to", required = true)
	private String to;

	@Property(type = PropertyType.TEXT, displayName = "subject", required = true)
	private String subject;

	@Property(type = PropertyType.TEXT, displayName = "attachment", required = false)
	private String attachment;

	@Property(type = PropertyType.TEXT, displayName = "bcc", required = false)
	private String bcc;

	@Property(type = PropertyType.TEXT, displayName = "cc", required = false)
	private String cc;

	@Override
	public String getName() {
		return "Template Mail";
	}

	@Override
	public String contributeToPaletteDrawer() {
		return "WorkStream Service Tasks";
	}

	@Override
	public String getSmallIconPath() {
		return "icons/mail.png";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/mail-large.png";
	}
}
