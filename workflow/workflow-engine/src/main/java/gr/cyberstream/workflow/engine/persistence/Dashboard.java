package gr.cyberstream.workflow.engine.persistence;

import java.util.Date;
import java.util.List;

import gr.cyberstream.workflow.engine.model.dashboard.DashboardDatePartResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardSimpleResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardTaskResult;

public interface Dashboard {

	/**
	 * Returns a list of result objects containing 
	 * the workflow definition name and the number of instances running
	 * for the specified supervisor
	 * 
	 * @return
	 */
	public List<DashboardSimpleResult> getDefinitionRunningInstances(String supervisor);
	
	/**
	 * Returns a list of result objects containing 
	 * the owner name and the number of instances running owned by that group
	 * 
	 * @return
	 */
	public List<DashboardSimpleResult> getOwnerRunningInstances();
	
	/**
	 * Returns a list of result objects containing 
	 * the number of instances completed in each week/month/year 
	 * between the from and to dates for the specified supervisor
	 * 
	 * @return
	 */
	public List<DashboardDatePartResult> getCompletedInstances(String supervisor, Date from, Date to, String dateGroup);
	
	/**
	 * Returns a list of result objects containing 
	 * the number of instances completed in each week/month/year 
	 * between the from and to dates
	 * grouped by the workflow definition
	 * for the specified supervisor
	 * 
	 * @return
	 */
	public List<DashboardDatePartResult> getDefinitionCompletedInstances(String supervisor, Date from, Date to, String dateGroup, String... definitions);
	
	/**
	 * Returns a list of result objects containing 
	 * the number of instances completed in each week/month/year 
	 * between the from and to dates
	 * grouped by the owner
	 * for the specified supervisor
	 * 
	 * @return
	 */
	public List<DashboardDatePartResult> getOwnerCompletedInstances(Date from, Date to, String dateGroup, String... owners);
	
	/**
	 * Returns a list of result objects containing 
	 * the number of instances started in each week/month/year 
	 * between the from and to dates
	 * grouped by the client
	 * for the specified supervisor
	 * 
	 * @return
	 */
	public List<DashboardDatePartResult> getClientStartedInstances(String supervisor, Date from, Date to, String dateGroup, String... clients);
	
	/**
	 * Returns a list of result objects containing 
	 * the tasks not completed on time 
	 * 
	 * @return
	 */
	public List<DashboardTaskResult> getOverdueTasks(String supervisor, String taskTableName);
	
	/**
	 * Returns a list of result objects containing 
	 * the tasks that are not assigned 
	 * 
	 * @return
	 */
	public List<DashboardTaskResult> getUnassignedTasks(String supervisor, String taskTableName);
		
	/**
	 * Returns a list of result objects containing 
	 * the mean time to complete instances in each week/month/year 
	 * between the from and to dates
	 * for the specified supervisor
	 * @return
	 */
	public List<DashboardDatePartResult> getCompletedInstancesMeanTime(String supervisor, Date from, Date to, String dateGroup);
	
	/**
	 * Returns a list of result objects containing 
	 * the users with the highest or lowest count of active tasks
	 * 
	 * @return
	 */
	public List<DashboardSimpleResult> getActiveUserTasks(String supervisor, String taskTableName, String mode, int count);
	
	/**
	 * Returns a list of result objects containing 
	 * the users with the highest or lowest count of completed tasks
	 * in the period specified
	 * 
	 * @return
	 */
	public List<DashboardSimpleResult> getCompletedUserTasks(String supervisor, String taskTableName, Date from, Date to,
			String mode, int count);
}
