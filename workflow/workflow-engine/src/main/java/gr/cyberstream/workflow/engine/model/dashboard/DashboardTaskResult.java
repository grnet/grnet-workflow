package gr.cyberstream.workflow.engine.model.dashboard;

import java.util.Date;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.MappedSuperclass;
import javax.persistence.SqlResultSetMapping;

@SqlResultSetMapping(
        name = "TaskResultMapping",
        classes = @ConstructorResult(
                targetClass = DashboardTaskResult.class,
                columns = {
                	@ColumnResult(name = "instanceName"),
                    @ColumnResult(name = "taskId"),
                    @ColumnResult(name = "taskName"),
                    @ColumnResult(name = "assignee"),
                    @ColumnResult(name = "dueDate"),
                    }))

@MappedSuperclass
public class DashboardTaskResult {

	private String instanceName;
	private String taskId;
	private String taskName;
	private String assignee;
	private Date dueDate;
	
	public DashboardTaskResult() {
	}

	public DashboardTaskResult(String instanceName, String taskId, String taskName, String assignee, Date dueDate) {

		this.instanceName = instanceName;
		this.taskId = taskId;
		this.taskName = taskName;
		this.assignee = assignee;
		this.dueDate = dueDate;
	}

	public String getInstanceName() {
	
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
	
		this.instanceName = instanceName;
	}

	public String getTaskId() {
	
		return taskId;
	}

	public void setTaskId(String taskId) {
	
		this.taskId = taskId;
	}

	public String getTaskName() {
	
		return taskName;
	}

	public void setTaskName(String taskName) {
	
		this.taskName = taskName;
	}
	
	public String getAssignee() {
		
		return assignee;
	}

	public void setAssignee(String assignee) {
	
		this.assignee = assignee;
	}

	public Date getDueDate() {
	
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
	
		this.dueDate = dueDate;
	}
	
	@Override
	public String toString() {
		
		return instanceName + " - " + taskName + "(" + taskId + ") - " + assignee + " - " + dueDate;
	}
}
