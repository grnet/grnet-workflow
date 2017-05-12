package gr.cyberstream.workflow.engine.controller.v2.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAccount;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAuthenticationToken;
import gr.cyberstream.workflow.engine.controller.v2.DashboardController;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardDatePartResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardSimpleResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardTaskResult;
import gr.cyberstream.workflow.engine.service.DashboardService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class DashboardControllerTest {

	final static Logger logger = LoggerFactory.getLogger(DashboardControllerTest.class);

	private boolean integration = false;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	DashboardController dashboardController;

	@Mock
	private DashboardService dashboardService;

	private MockMvc mockMvc;

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);

		DashboardController unwrappedController;

		try {

			if (!integration) {
				unwrappedController = (DashboardController) unwrapProxy(dashboardController);
				ReflectionTestUtils.setField(unwrappedController, "dashboardService", dashboardService);
			}

			CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
			characterEncodingFilter.setEncoding("UTF-8");
			characterEncodingFilter.setForceEncoding(true);
						
			mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
					.addFilter(characterEncodingFilter, "/*").build();

		} catch (Exception e) {

			e.printStackTrace();
		}

		if (integration) {

			String name = "Test User";
			String email = "george.tylissanakis@cyberstream.gr";
			Set<String> roles = Sets.newSet(new String[] { "ROLE_Admin" });
			List<String> groups = Arrays.asList(new String[] { "GroupA" });

			KeycloakAuthenticationToken authentication = new MockKeycloakAuthenticationToken(
					new MockKeycloakAccount(name, email, roles, groups));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
	}

	@Test
	public void getDefinitionRunningInstances() throws Exception {
		
		List<DashboardSimpleResult> results = Arrays.asList(new DashboardSimpleResult[] {
				new DashboardSimpleResult("Βλάβη δικτύου ύδρευσης", 10),
				new DashboardSimpleResult("Βλάβη οδοφωτισμού", 15)
		});

		when(dashboardService.getDefinitionRunningInstances(any(Boolean.class))).thenReturn(results);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/stat/definition/running").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getDefinitionRunningInstancesForSupervisor() throws Exception {
		
		boolean supervisor = true;
		
		List<DashboardSimpleResult> results = Arrays.asList(new DashboardSimpleResult[] {
				new DashboardSimpleResult("Βλάβη δικτύου ύδρευσης", 10),
				new DashboardSimpleResult("Βλάβη οδοφωτισμού", 15)
		});

		when(dashboardService.getDefinitionRunningInstances(eq(supervisor))).thenReturn(results);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/stat/definition/running?supervisor={supervisor}", supervisor)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getOwnerRunningInstances() throws Exception {
		
		List<DashboardSimpleResult> results = Arrays.asList(new DashboardSimpleResult[] {
				new DashboardSimpleResult("Βλάβη δικτύου ύδρευσης", 10),
				new DashboardSimpleResult("Βλάβη οδοφωτισμού", 15)
		});

		when(dashboardService.getOwnerRunningInstances()).thenReturn(results);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/stat/owner/running").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void getCompletedInstances() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("4/2016", -1, 4, 2016, 10, 0.0),
				new DashboardDatePartResult("5/2016", -1, 5, 2016, 15, 0.0)
		});

		when(dashboardService.getCompletedInstances(any(Boolean.class), any(Date.class), any(Date.class), any(String.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedInstances(any(Boolean.class), isNull(), any(Date.class), any(String.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedInstances(any(Boolean.class), any(Date.class), isNull(), any(String.class)))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/{from}/{to}/{dateGroup}", from, to, dateGroup)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getCompletedInstancesForSupervisor() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		
		boolean supervisor = true;

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("4/2016", -1, 4, 2016, 10, 0.0),
				new DashboardDatePartResult("5/2016", -1, 5, 2016, 15, 0.0)
		});

		when(dashboardService.getCompletedInstances(eq(supervisor), any(Date.class), any(Date.class), any(String.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedInstances(eq(supervisor), isNull(), any(Date.class), any(String.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedInstances(eq(supervisor), any(Date.class), isNull(), any(String.class)))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/{from}/{to}/{dateGroup}?supervisor={supervisor}", from, to, dateGroup, supervisor)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getDefinitionCompletedInstances() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "definition";

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("Φθορές και θέματα πρασίνου", -1, 4, 2016, 10, 0.0),
				new DashboardDatePartResult("Φθορές σε πάρκα και παιδικές χαρές", -1, 5, 2016, 15, 0.0)
		});

		when(dashboardService.getDefinitionCompletedInstances(any(Boolean.class), any(Date.class), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getDefinitionCompletedInstances(any(Boolean.class), isNull(), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getDefinitionCompletedInstances(any(Boolean.class), any(Date.class), isNull(), any(String.class), isNull()))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/{groupBy}/{from}/{to}/{dateGroup}", groupBy, from, to, dateGroup)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getDefinitionCompletedInstancesForSupervisor() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "definition";

		boolean supervisor = true;
		
		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("Φθορές και θέματα πρασίνου", -1, 4, 2016, 10, 0.0),
				new DashboardDatePartResult("Φθορές σε πάρκα και παιδικές χαρές", -1, 5, 2016, 15, 0.0)
		});

		when(dashboardService.getDefinitionCompletedInstances(eq(supervisor), any(Date.class), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getDefinitionCompletedInstances(eq(supervisor), isNull(), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getDefinitionCompletedInstances(eq(supervisor), any(Date.class), isNull(), any(String.class), isNull()))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/{groupBy}/{from}/{to}/{dateGroup}?supervisor={supervisor}", groupBy, from, to, dateGroup, supervisor)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getSpecificDefinitionCompletedInstances() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "definition;definition=Φθορές και θέματα πρασίνου";

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("Φθορές και θέματα πρασίνου", -1, 4, 2016, 10, 0.0)
		});

		when(dashboardService.getDefinitionCompletedInstances(any(Boolean.class), any(Date.class), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getDefinitionCompletedInstances(any(Boolean.class), isNull(), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getDefinitionCompletedInstances(any(Boolean.class), any(Date.class), isNull(), any(String.class), any(String[].class)))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/{groupBy}/{from}/{to}/{dateGroup}", groupBy, from, to, dateGroup)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getSpecificDefinitionCompletedInstancesForSupervisor() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "definition;definition=Φθορές και θέματα πρασίνου";

		boolean supervisor = true;
		
		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("Φθορές και θέματα πρασίνου", -1, 4, 2016, 10, 0.0)
		});

		when(dashboardService.getDefinitionCompletedInstances(eq(supervisor), any(Date.class), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getDefinitionCompletedInstances(eq(supervisor), isNull(), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getDefinitionCompletedInstances(eq(supervisor), any(Date.class), isNull(), any(String.class), any(String[].class)))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/{groupBy}/{from}/{to}/{dateGroup}?supervisor={supervisor}", groupBy, from, to, dateGroup, supervisor)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getOwnerCompletedInstances() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "owner";

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("Αστικό Περιβάλλον και Πρασίνου Πόλης", -1, 4, 2016, 10, 0.0)
		});

		when(dashboardService.getOwnerCompletedInstances(any(Date.class), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getOwnerCompletedInstances(isNull(), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getOwnerCompletedInstances(any(Date.class), isNull(), any(String.class), isNull()))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/{groupBy}/{from}/{to}/{dateGroup}", groupBy, from, to, dateGroup)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getSpecificOwnerCompletedInstances() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "owner;owner=Αστικό Περιβάλλον και Πρασίνου Πόλης";

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("Αστικό Περιβάλλον και Πρασίνου Πόλης", -1, 4, 2016, 10, 0.0)
		});

		when(dashboardService.getOwnerCompletedInstances(any(Date.class), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getOwnerCompletedInstances(isNull(), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getOwnerCompletedInstances(any(Date.class), isNull(), any(String.class), any(String[].class)))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/{groupBy}/{from}/{to}/{dateGroup}", groupBy, from, to, dateGroup)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getClientStartedInstances() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "client";

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("MOBILE", -1, 4, 2016, 10, 0.0)
		});

		when(dashboardService.getClientStartedInstances(any(Boolean.class), any(Date.class), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getClientStartedInstances(any(Boolean.class), isNull(), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getClientStartedInstances(any(Boolean.class), any(Date.class), isNull(), any(String.class), isNull()))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/started/{groupBy}/{from}/{to}/{dateGroup}", groupBy, from, to, dateGroup)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getClientStartedInstancesForSupervisor() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "client";

		boolean supervisor = true;
		
		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("MOBILE", -1, 4, 2016, 10, 0.0)
		});

		when(dashboardService.getClientStartedInstances(eq(supervisor), any(Date.class), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getClientStartedInstances(eq(supervisor), isNull(), any(Date.class), any(String.class), isNull()))
			.thenReturn(results);
		when(dashboardService.getClientStartedInstances(eq(supervisor), any(Date.class), isNull(), any(String.class), isNull()))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/started/{groupBy}/{from}/{to}/{dateGroup}?supervisor={supervisor}", groupBy, from, to, dateGroup, supervisor)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getSpecificClientStartedInstances() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "client;client=MOBILE";

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("MOBILE", -1, 4, 2016, 10, 0.0)
		});

		when(dashboardService.getClientStartedInstances(any(Boolean.class), any(Date.class), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getClientStartedInstances(any(Boolean.class), isNull(), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getClientStartedInstances(any(Boolean.class), any(Date.class), isNull(), any(String.class), any(String[].class)))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/started/{groupBy}/{from}/{to}/{dateGroup}", groupBy, from, to, dateGroup)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getSpecificClientStartedInstancesForSupervisor() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		String groupBy = "client;client=MOBILE";

		boolean supervisor = true;
		
		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("MOBILE", -1, 4, 2016, 10, 0.0)
		});

		when(dashboardService.getClientStartedInstances(eq(supervisor), any(Date.class), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getClientStartedInstances(eq(supervisor), isNull(), any(Date.class), any(String.class), any(String[].class)))
			.thenReturn(results);
		when(dashboardService.getClientStartedInstances(eq(supervisor), any(Date.class), isNull(), any(String.class), any(String[].class)))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/started/{groupBy}/{from}/{to}/{dateGroup}?supervisor={supervisor}", groupBy, from, to, dateGroup, supervisor)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getOverdueTasks() throws Exception {
		
		List<DashboardTaskResult> results = Arrays.asList(new DashboardTaskResult[] {
				new DashboardTaskResult()
		});

		when(dashboardService.getOverdueTasks(any(Boolean.class))).thenReturn(results);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/stat/tasks/overdue").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getOverdueTasksForSupervisor() throws Exception {
		
		boolean supervisor = true;
		
		List<DashboardTaskResult> results = Arrays.asList(new DashboardTaskResult[] {
				new DashboardTaskResult()
		});

		when(dashboardService.getOverdueTasks(eq(supervisor))).thenReturn(results);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/stat/tasks/overdue?supervisor={supervisor}", supervisor)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getUnassignedTasks() throws Exception {
		
		List<DashboardTaskResult> results = Arrays.asList(new DashboardTaskResult[] {
				new DashboardTaskResult()
		});

		when(dashboardService.getUnassignedTasks(any(Boolean.class))).thenReturn(results);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/stat/tasks/unassigned").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getUnassignedTasksForSupervisor() throws Exception {
		
		boolean supervisor = true;
		
		List<DashboardTaskResult> results = Arrays.asList(new DashboardTaskResult[] {
				new DashboardTaskResult()
		});

		when(dashboardService.getUnassignedTasks(eq(supervisor))).thenReturn(results);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/stat/tasks/unassigned?supervisor={supervisor}", supervisor)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getCompletedInstancesMeanTime() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("Φθορές και θέματα πρασίνου", -1, 4, 2016, 10, 12.5),
				new DashboardDatePartResult("Φθορές σε πάρκα και παιδικές χαρές", -1, 5, 2016, 15, 5.1)
		});

		when(dashboardService.getCompletedInstancesMeanTime(any(Boolean.class), any(Date.class), any(Date.class), any(String.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedInstancesMeanTime(any(Boolean.class), isNull(), any(Date.class), any(String.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedInstancesMeanTime(any(Boolean.class), any(Date.class), isNull(), any(String.class)))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/times/{from}/{to}/{dateGroup}", from, to, dateGroup)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getCompletedInstancesMeanTimeForSupervisor() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String dateGroup = "month";
		
		boolean supervisor = true;

		List<DashboardDatePartResult> results = Arrays.asList(new DashboardDatePartResult[] {
				new DashboardDatePartResult("Φθορές και θέματα πρασίνου", -1, 4, 2016, 10, 12.5),
				new DashboardDatePartResult("Φθορές σε πάρκα και παιδικές χαρές", -1, 5, 2016, 15, 5.1)
		});

		when(dashboardService.getCompletedInstancesMeanTime(eq(supervisor), any(Date.class), any(Date.class), any(String.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedInstancesMeanTime(eq(supervisor), isNull(), any(Date.class), any(String.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedInstancesMeanTime(eq(supervisor), any(Date.class), isNull(), any(String.class)))
			.thenReturn(results);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/completed/times/{from}/{to}/{dateGroup}?supervisor={supervisor}", from, to, dateGroup, supervisor)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getUserActiveTasks() throws Exception {

		String mode = "top";
		int count = 10;
		
		List<DashboardSimpleResult> results = Arrays.asList(new DashboardSimpleResult[] {
				new DashboardSimpleResult("giannis.zaf@vrilissia.gr", 43),
				new DashboardSimpleResult("lena.oik@vrilissia.gr", 20)
		});

		when(dashboardService.getActiveUserTasks(any(Boolean.class), any(String.class), any(Integer.class)))
			.thenReturn(results);
		
		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/users/active/{mode}/{count}", mode, count)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getUserActiveTasksForSupervisor() throws Exception {

		String mode = "top";
		int count = 10;
		
		boolean supervisor = true;
		
		List<DashboardSimpleResult> results = Arrays.asList(new DashboardSimpleResult[] {
				new DashboardSimpleResult("giannis.zaf@vrilissia.gr", 43),
				new DashboardSimpleResult("lena.oik@vrilissia.gr", 20)
		});

		when(dashboardService.getActiveUserTasks(eq(supervisor), any(String.class), any(Integer.class)))
			.thenReturn(results);
		
		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/users/active/{mode}/{count}?supervisor={supervisor}", mode, count, supervisor)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getUserCompletedTasks() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String mode = "top";
		int count = 10;
		
		List<DashboardSimpleResult> results = Arrays.asList(new DashboardSimpleResult[] {
				new DashboardSimpleResult("giannis.zaf@vrilissia.gr", 43),
				new DashboardSimpleResult("lena.oik@vrilissia.gr", 20)
		});

		when(dashboardService.getCompletedUserTasks(any(Boolean.class), any(Date.class), any(Date.class), any(String.class), any(Integer.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedUserTasks(any(Boolean.class), any(Date.class), isNull(), any(String.class), any(Integer.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedUserTasks(any(Boolean.class), isNull(), any(Date.class), any(String.class), any(Integer.class)))
			.thenReturn(results);
		
		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/users/completed/{from}/{to}/{mode}/{count}", from, to, mode, count)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getUserCompletedTasksForSupervisor() throws Exception {

		String from = "2016-04-01";
		String to = "-";
		String mode = "top";
		int count = 10;
		
		boolean supervisor = true;
		
		List<DashboardSimpleResult> results = Arrays.asList(new DashboardSimpleResult[] {
				new DashboardSimpleResult("giannis.zaf@vrilissia.gr", 43),
				new DashboardSimpleResult("lena.oik@vrilissia.gr", 20)
		});

		when(dashboardService.getCompletedUserTasks(eq(supervisor), any(Date.class), any(Date.class), any(String.class), any(Integer.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedUserTasks(eq(supervisor), any(Date.class), isNull(), any(String.class), any(Integer.class)))
			.thenReturn(results);
		when(dashboardService.getCompletedUserTasks(eq(supervisor), isNull(), any(Date.class), any(String.class), any(Integer.class)))
			.thenReturn(results);
		
		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/stat/users/completed/{from}/{to}/{mode}/{count}?supervisor={supervisor}", from, to, mode, count, supervisor)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	public static final Object unwrapProxy(Object bean) throws Exception {

		/*
		 * If the given object is a proxy, set the return value as the object
		 * being proxied, otherwise return the given object.
		 */
		if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
			Advised advised = (Advised) bean;
			bean = advised.getTargetSource().getTarget();
		}
		return bean;
	}
}
