package gr.cyberstream.workflow.engine.persistence.test;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gr.cyberstream.workflow.engine.model.dashboard.DashboardDatePartResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardSimpleResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardTaskResult;
import gr.cyberstream.workflow.engine.persistence.Dashboard;

@ContextConfiguration(classes = PersistenceConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DashboardTest {

	final static Logger logger = LoggerFactory.getLogger(ProcessesTest.class);

	@Autowired
	private Dashboard dashboard;
		
	@Test
	public void shouldGetDefinitionRunningInstances() {

		String supervisor = null;
		
		try {
		
			List<DashboardSimpleResult> definitionRunningInstances = dashboard.getDefinitionRunningInstances(supervisor);
		
			assertTrue(definitionRunningInstances != null);
			assertTrue(definitionRunningInstances.size() > 0);
			
			for (DashboardSimpleResult result : definitionRunningInstances) {
				
				logger.info(result.toString());
			}
			
		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetDefinitionRunningInstancesForSupervisor() {

		String supervisor = "vassilis.lo@vrilissia.gr";
		
		try {
		
			List<DashboardSimpleResult> definitionRunningInstances = dashboard.getDefinitionRunningInstances(supervisor);
		
			assertTrue(definitionRunningInstances != null);
			assertTrue(definitionRunningInstances.size() > 0);
			
			for (DashboardSimpleResult result : definitionRunningInstances) {
				
				logger.info(result.toString());
			}
			
		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetOwnerRunningInstances() {

		try {
		
			List<DashboardSimpleResult> ownerRunningInstances = dashboard.getOwnerRunningInstances();
		
			assertTrue(ownerRunningInstances != null);
			assertTrue(ownerRunningInstances.size() > 0);
			
			for (DashboardSimpleResult result : ownerRunningInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedInstancesFromDateByYear() {

		String supervisor = null;
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getCompletedInstances(supervisor, cal.getTime(), null, "year");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedInstancesFromDateByMonthForSupervisor() {

		String supervisor = "giannis.zaf@vrilissia.gr";
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getCompletedInstances(supervisor, cal.getTime(), null, "month");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedInstancesFromDateByWeek() {

		String supervisor = null;
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getCompletedInstances(supervisor, cal.getTime(), null, "week");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetDefinitionCompletedInstancesFromDateByWeek() {

		String supervisor = null;
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getDefinitionCompletedInstances(supervisor, cal.getTime(), null, "week");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetDefinitionCompletedInstancesForSupervisor() {

		String supervisor = "giannis.zaf@vrilissia.gr";
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getDefinitionCompletedInstances(supervisor, cal.getTime(), null, "month");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetSpecificDefinitionCompletedInstancesFromDateByWeek() {

		String supervisor = null;
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getDefinitionCompletedInstances(supervisor, 
					cal.getTime(), null, "month", "Φθορές και θέματα πρασίνου", "Φθορές σε πάρκα και παιδικές χαρές");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetSpecificDefinitionCompletedInstancesForSupervisor() {

		String supervisor = "giannis.zaf@vrilissia.gr";
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getDefinitionCompletedInstances(supervisor, 
					cal.getTime(), null, "month", "Φθορές και θέματα πρασίνου", "Φθορές σε πάρκα και παιδικές χαρές");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetOwnerCompletedInstancesFromDateByMonth() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getOwnerCompletedInstances(cal.getTime(), null, "month");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetSpecificOwnerCompletedInstancesFromDateByMonth() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getOwnerCompletedInstances(cal.getTime(), null, "month",
					"Αστικό Περιβάλλον και Πρασίνου Πόλης");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetClientStartedInstancesFromDateByMonth() {

		String supervisor = null;
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getClientStartedInstances(supervisor, cal.getTime(), null, "month");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetClientStartedInstancesForSupervisor() {

		String supervisor = "giannis.zaf@vrilissia.gr";
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getClientStartedInstances(supervisor, cal.getTime(), null, "month");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetSpecificClientStartedInstancesFromDateByMonth() {

		String supervisor = null;
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboard.getClientStartedInstances(supervisor, cal.getTime(), null, "month",
					"MOBILE");
		
			assertTrue(completedInstances != null);
			assertTrue(completedInstances.size() > 0);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetOverdueTasks() {

		String taskTableName = "ACT_RU_TASK";
		
		String supervisor = null;
		
		try {
		
			List<DashboardTaskResult> tasks = dashboard.getOverdueTasks(supervisor, taskTableName);
		
			assertTrue(tasks != null);
			assertTrue(tasks.size() > 0);
			
			for (DashboardTaskResult result : tasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetOverdueTasksForSupervisor() {

		String taskTableName = "ACT_RU_TASK";
		
		String supervisor = "eleftheria.s@vrilissia.gr";
		
		try {
		
			List<DashboardTaskResult> tasks = dashboard.getOverdueTasks(supervisor, taskTableName);
		
			assertTrue(tasks != null);
			assertTrue(tasks.size() > 0);
			
			for (DashboardTaskResult result : tasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetUnassignedTasks() {

		String taskTableName = "ACT_RU_TASK";
		
		String supervisor = null;
		
		try {
		
			List<DashboardTaskResult> tasks = dashboard.getUnassignedTasks(supervisor, taskTableName);
		
			assertTrue(tasks != null);
			assertTrue(tasks.size() > 0);
			
			for (DashboardTaskResult result : tasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetUnassignedTasksForSupervisor() {

		String taskTableName = "ACT_RU_TASK";
		
		String supervisor = "kalitsis@vrilissia.gr";
		
		try {
		
			List<DashboardTaskResult> tasks = dashboard.getUnassignedTasks(supervisor, taskTableName);
		
			assertTrue(tasks != null);
			assertTrue(tasks.size() > 0);
			
			for (DashboardTaskResult result : tasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedInstancesMeanTimeByMonth() {

		String supervisor = null;
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> definitionTimes = dashboard.getCompletedInstancesMeanTime(supervisor, cal.getTime(), null, "month");
		
			assertTrue(definitionTimes != null);
			assertTrue(definitionTimes.size() > 0);
			
			for (DashboardDatePartResult result : definitionTimes) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedInstancesMeanTimeByMonthForSupervisor() {

		String supervisor = "giannis.zaf@vrilissia.gr";
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0);
		
		try {
		
			List<DashboardDatePartResult> definitionTimes = dashboard.getCompletedInstancesMeanTime(supervisor, cal.getTime(), null, "month");
		
			assertTrue(definitionTimes != null);
			assertTrue(definitionTimes.size() > 0);
			
			for (DashboardDatePartResult result : definitionTimes) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetActiveUserTasks() {
		
		String supervisor = null;
		
		String taskTableName = "ACT_RU_TASK";
		String mode = "top";
		int count = 10;
		
		try {
		
			List<DashboardSimpleResult> usersWithActiveTasks = dashboard.getActiveUserTasks(supervisor, taskTableName, mode, count);
		
			assertTrue(usersWithActiveTasks != null);
			assertTrue(usersWithActiveTasks.size() > 0);
			
			for (DashboardSimpleResult result : usersWithActiveTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetActiveUserTasksForSupervisor() {
		
		String supervisor = "kalitsis@vrilissia.gr";
		
		String taskTableName = "ACT_RU_TASK";
		String mode = "top";
		int count = 10;
		
		try {
		
			List<DashboardSimpleResult> usersWithActiveTasks = dashboard.getActiveUserTasks(supervisor, taskTableName, mode, count);
		
			assertTrue(usersWithActiveTasks != null);
			assertTrue(usersWithActiveTasks.size() > 0);
			
			for (DashboardSimpleResult result : usersWithActiveTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedUserTasks() {
		
		String supervisor = null;
		
		String taskTableName = "ACT_HI_TASKINST";
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0);
		
		
		String mode = "top";
		int count = 10;
		
		try {
		
			List<DashboardSimpleResult> usersWithCompletedTasks = dashboard.getCompletedUserTasks(supervisor, taskTableName, cal.getTime(), null,
					mode, count);
		
			assertTrue(usersWithCompletedTasks != null);
			assertTrue(usersWithCompletedTasks.size() > 0);
			
			for (DashboardSimpleResult result : usersWithCompletedTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedUserTasksForSupervisor() {
		
		String supervisor = "giannis.zaf@vrilissia.gr";
		
		String taskTableName = "ACT_HI_TASKINST";
		
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 5, 1, 0, 0);
		
		
		String mode = "top";
		int count = 10;
		
		try {
		
			List<DashboardSimpleResult> usersWithCompletedTasks = dashboard.getCompletedUserTasks(supervisor, taskTableName, cal.getTime(), null,
					mode, count);
		
			assertTrue(usersWithCompletedTasks != null);
			assertTrue(usersWithCompletedTasks.size() > 0);
			
			for (DashboardSimpleResult result : usersWithCompletedTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
}
