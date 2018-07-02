package gr.cyberstream.workflow.engine.service.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAccount;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAuthenticationToken;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardDatePartResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardSimpleResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardTaskResult;
import gr.cyberstream.workflow.engine.service.DashboardService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class DashboardServiceTest {

	@Autowired
	private DashboardService dashboardService;

	private static final Logger logger = LoggerFactory.getLogger(DashboardServiceTest.class);

	@Before
	public void setup() {

		String name = "Test User";
		String email = "giannis.zaf@vrilissia.gr";
		Set<String> roles = Sets.newSet(new String[] { "ROLE_Admin" });
		List<String> groups = Arrays.asList(new String[] { "Καθαριότητα Πόλης" });
		
		KeycloakAuthenticationToken authentication = new MockKeycloakAuthenticationToken(
				new MockKeycloakAccount(name, email, roles, groups));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
	@Test
	public void shouldGetRunningInstances() {

		try {
			
			List<DashboardSimpleResult> definitionRunningInstances =  dashboardService.getDefinitionRunningInstances(false);
			
			assertTrue(definitionRunningInstances != null);
			
			for (DashboardSimpleResult result : definitionRunningInstances) {
				
				logger.info(result.toString());
			}

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetRunningInstancesForSupervisor() {

		try {
			
			List<DashboardSimpleResult> definitionRunningInstances =  dashboardService.getDefinitionRunningInstances(true);
			
			assertTrue(definitionRunningInstances != null);
			
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
			
			List<DashboardSimpleResult> ownerRunningInstances = dashboardService.getOwnerRunningInstances();
		
			assertTrue(ownerRunningInstances != null);
			
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

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getCompletedInstances(false, cal.getTime(), null, "year");
		
			assertTrue(completedInstances != null);
			
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

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getCompletedInstances(true, cal.getTime(), null, "month");
		
			assertTrue(completedInstances != null);
			
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

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getCompletedInstances(false, cal.getTime(), null, "week");
		
			assertTrue(completedInstances != null);
			
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

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getDefinitionCompletedInstances(
					false, cal.getTime(), null, "week");
		
			assertTrue(completedInstances != null);
			
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

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getDefinitionCompletedInstances(false, 
					cal.getTime(), null, "week", "Φθορές και θέματα πρασίνου", "Φθορές σε πάρκα και παιδικές χαρές");
		
			assertTrue(completedInstances != null);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetDefinitionCompletedInstancesFromDateByWeekForSupervisor() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getDefinitionCompletedInstances(true, cal.getTime(), null, "week");
		
			assertTrue(completedInstances != null);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetSpecificDefinitionCompletedInstancesFromDateByWeekForSupervisor() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getDefinitionCompletedInstances(true, 
					cal.getTime(), null, "week", "Φθορές και θέματα πρασίνου", "Φθορές σε πάρκα και παιδικές χαρές");
		
			assertTrue(completedInstances != null);
			
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
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getOwnerCompletedInstances(cal.getTime(), null, "month");
		
			assertTrue(completedInstances != null);
			
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
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getOwnerCompletedInstances(cal.getTime(), null, "month",
					"Αστικό Περιβάλλον και Πρασίνου Πόλης");
		
			assertTrue(completedInstances != null);
			
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

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getClientStartedInstances(false, cal.getTime(), null, "month");
		
			assertTrue(completedInstances != null);
			
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

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getClientStartedInstances(true, cal.getTime(), null, "month");
		
			assertTrue(completedInstances != null);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetSpecificClientStartedInstancesFromDateByWeek() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getClientStartedInstances(false, cal.getTime(), null, "week",
					"MOBILE");
		
			assertTrue(completedInstances != null);
			
			for (DashboardDatePartResult result : completedInstances) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetSpecificClientStartedInstancesForSupervisor() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> completedInstances = dashboardService.getClientStartedInstances(true, cal.getTime(), null, "week",
					"MOBILE");
		
			assertTrue(completedInstances != null);
			
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

		try {
		
			List<DashboardTaskResult> overdueTasks = dashboardService.getOverdueTasks(false);
		
			assertTrue(overdueTasks != null);
			
			for (DashboardTaskResult result : overdueTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetOverdueTasksForSupervisor() {

		try {
		
			List<DashboardTaskResult> overdueTasks = dashboardService.getOverdueTasks(true);
		
			assertTrue(overdueTasks != null);
			
			for (DashboardTaskResult result : overdueTasks) {
				
				logger.info(result.toString());
			}

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetUnassignedTasks() {

		try {
		
			List<DashboardTaskResult> unassignedTasks = dashboardService.getUnassignedTasks(false);
		
			assertTrue(unassignedTasks != null);
			
			for (DashboardTaskResult result : unassignedTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetUnassignedTasksForSupervisor() {

		try {
		
			List<DashboardTaskResult> unassignedTasks = dashboardService.getUnassignedTasks(true);
		
			assertTrue(unassignedTasks != null);
			
			for (DashboardTaskResult result : unassignedTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedInstancesMeanTimeByMonth() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> instancesTime = dashboardService.getCompletedInstancesMeanTime(false, cal.getTime(), null, "week");
		
			assertTrue(instancesTime != null);
			
			for (DashboardDatePartResult result : instancesTime) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedInstancesMeanTimeForSupervisor() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		try {
		
			List<DashboardDatePartResult> instancesTime = dashboardService.getCompletedInstancesMeanTime(true, cal.getTime(), null, "week");
		
			assertTrue(instancesTime != null);
			
			for (DashboardDatePartResult result : instancesTime) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetActiveUserTasks() {

		String mode = "top";
		int count = 10;
		
		try {
		
			List<DashboardSimpleResult> activeUserTasks = dashboardService.getActiveUserTasks(false, mode, count);
		
			assertTrue(activeUserTasks != null);
			
			for (DashboardSimpleResult result : activeUserTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetActiveUserTasksForSupervisor() {

		String mode = "top";
		int count = 10;
		
		try {
		
			List<DashboardSimpleResult> activeUserTasks = dashboardService.getActiveUserTasks(true, mode, count);
		
			assertTrue(activeUserTasks != null);
			
			for (DashboardSimpleResult result : activeUserTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedUserTasks() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		String mode = "bottom";
		int count = 10;
		
		try {
		
			List<DashboardSimpleResult> completedUserTasks = dashboardService.getCompletedUserTasks(false, cal.getTime(), null, mode, count);
		
			assertTrue(completedUserTasks != null);
			
			for (DashboardSimpleResult result : completedUserTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetCompletedUserTasksForSupervisor() {

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 3, 1);
		
		String mode = "bottom";
		int count = 10;
		
		try {
		
			List<DashboardSimpleResult> completedUserTasks = dashboardService.getCompletedUserTasks(true, cal.getTime(), null, mode, count);
		
			assertTrue(completedUserTasks != null);
			
			for (DashboardSimpleResult result : completedUserTasks) {
				
				logger.info(result.toString());
			}
			

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
}
