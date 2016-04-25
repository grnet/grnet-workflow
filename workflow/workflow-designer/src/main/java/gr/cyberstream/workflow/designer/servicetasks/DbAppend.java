package gr.cyberstream.workflow.designer.servicetasks;

import org.activiti.designer.integration.annotation.Help;
import org.activiti.designer.integration.annotation.Property;
import org.activiti.designer.integration.annotation.Runtime;
import org.activiti.designer.integration.servicetask.AbstractCustomServiceTask;
import org.activiti.designer.integration.servicetask.PropertyType;


@Runtime(javaDelegateClass = "gr.cyberstream.workflow.engine.customservicetasks.DbAppend")
@Help(displayHelpShort = "Accumulates data in database", 
displayHelpLong = "Accumulates the process variables in the specified datasource.")
public class DbAppend extends AbstractCustomServiceTask{

	@Property(type = PropertyType.TEXT, displayName = "datasource", required = true)
	private String datasource;
	
	@Property(type = PropertyType.TEXT, displayName = "table", required = true)
	private String table;
		
	@Property(type = PropertyType.TEXT, displayName = "columns", required = true)
	private String columns;
	
	@Property(type = PropertyType.TEXT, displayName = "values", required = true)
	private String values;
		
	@Override
	public String getName() {
		return "Database Append";
	}
	
	@Override
	public String contributeToPaletteDrawer() {
		return "WorkStream Service Tasks";
	}
	
	@Override
	public String getSmallIconPath() {
		return "icons/dbappend.png";
	}

	@Override
	public String getLargeIconPath() {
		return "icons/dbappend-large.png";
	}
	
}
