package gr.cyberstream.workflow.engine.service;

import java.util.Date;
import java.util.List;

import gr.cyberstream.workflow.engine.model.dashboard.DashboardDatePartResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardSimpleResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardTaskResult;

public interface DashboardService {

	/**
	 * Returns a list of result objects containing 
	 * the workflow definition and the number of instances running
	 * 
	 * @return
	 */
	public List<DashboardSimpleResult> getDefinitionRunningInstances(boolean supervisor);
	
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
	 * between the from and to dates
	 * 
	 * @return
	 */
	public List<DashboardDatePartResult> getCompletedInstances(boolean supervisor, Date from, Date to, String dateGroup);
	
	/**
	 * Returns a list of result objects containing 
	 * the number of instances completed in each week/month/year 
	 * between the from and to dates
	 * grouped by the workflow definition
	 * 
	 * @return
	 */
	public List<DashboardDatePartResult> getDefinitionCompletedInstances(boolean supervisor, Date from, Date to, String dateGroup, String... definitions);
	
	/**
	 * Returns a list of result objects containing 
	 * the number of instances completed in each week/month/year 
	 * between the from and to dates
	 * grouped by the owner
	 * 
	 * @return
	 */
	public List<DashboardDatePartResult> getOwnerCompletedInstances(Date from, Date to, String dateGroup, String... owners);

	/**
	 * Returns a list of result objects containing 
	 * the number of instances started in each week/month/year 
	 * between the from and to dates
	 * grouped by the client
	 * 
	 * @return
	 */
	public List<DashboardDatePartResult> getClientStartedInstances(boolean supervisor, Date from, Date to, String dateGroup, String... clients);
	
	/**
	 * Returns a list of result objects containing 
	 * the tasks not completed on time 
	 * 
	 * @return
	 */
	public List<DashboardTaskResult> getOverdueTasks(boolean supervisor);
	
	/**
	 * Returns a list of result objects containing 
	 * the tasks that are not assigned 
	 * 
	 * @return
	 */
	public List<DashboardTaskResult> getUnassignedTasks(boolean supervisor);
	
	/**
	 * Returns a list of result objects containing 
	 * the mean time to complete instances in each week/month/year 
	 * between the from and to dates
	 * 
	 * @return
	 */
	public List<DashboardDatePartResult> getCompletedInstancesMeanTime(boolean supervisor, Date from, Date to, String dateGroup);
	
	/**
	 * Returns a list of result objects containing 
	 * the users with the highest or lowest count of active tasks
	 * 
	 * @return
	 */
	public List<DashboardSimpleResult> getActiveUserTasks(boolean supervisor, String mode, int count);
	
	/**
	 * Returns a list of result objects containing 
	 * the users with the highest or lowest count of completed tasks
	 * in the period specified
	 * 
	 * @return
	 */
	public List<DashboardSimpleResult> getCompletedUserTasks(boolean supervisor, Date from, Date to, String mode, int count);
}
