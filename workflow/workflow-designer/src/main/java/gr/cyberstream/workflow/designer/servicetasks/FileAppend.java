package gr.cyberstream.workflow.designer.servicetasks;

import org.activiti.designer.integration.annotation.Help;
import org.activiti.designer.integration.annotation.Property;
import org.activiti.designer.integration.annotation.Runtime;
import org.activiti.designer.integration.servicetask.AbstractCustomServiceTask;
import org.activiti.designer.integration.servicetask.PropertyType;


@Runtime(javaDelegateClass = "gr.cyberstream.workflow.engine.customservicetasks.FileAppend")
@Help(displayHelpShort = "Appends a string to a file", 
displayHelpLong = "Appends the given value to a file")
public class FileAppend extends AbstractCustomServiceTask{

	@Property(type = PropertyType.TEXT, displayName = "file path", required = true)
	private String filepath;
	
	@Property(type = PropertyType.TEXT, displayName = "value", required = true)
	private String value;
	
	@Override
	public String getName() {
		return "File Append";
	}
	
	@Override
	public String contributeToPaletteDrawer() {
		return "WorkStream Service Tasks";
	}
	
	@Override
	public String getSmallIconPath() {
		return "icons/fileappend.png";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/fileappend-large.png";
	}
	

	
}
