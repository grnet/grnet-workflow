package gr.cyberstream.workflow.engine.controller.v2.test;

import java.util.ArrayList;
import java.util.Arrays;
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

import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAccount;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAuthenticationToken;
import gr.cyberstream.workflow.engine.controller.v2.RealmController;
import gr.cyberstream.workflow.engine.model.api.WfOwner;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.service.ProcessService;
import gr.cyberstream.workflow.engine.service.RealmService;


@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class RealmControllerTest {

	final static Logger logger = LoggerFactory.getLogger(RealmControllerTest.class);
	
	private boolean integration = false;
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	RealmController realmV2Controller;
	
	@Mock
	private RealmService realmService;
	
	@Mock
	private ProcessService processService;
	
	private MockMvc mockMvc;
	
	@Before
    public void setup() {

		MockitoAnnotations.initMocks(this);
		
		RealmController unwrappedController;
		
		try {
		
			if (!integration) {
				unwrappedController = (RealmController) unwrapProxy(realmV2Controller);
				ReflectionTestUtils.setField(unwrappedController, "realmService", realmService);
				ReflectionTestUtils.setField(unwrappedController, "processService", processService);
			}
			
			mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		if (integration) {
			
			String name = "Test User";
			String email = "george.tylissanakis@cyberstream.gr";
			Set<String> roles = Sets.newSet(new String[]{"ROLE_Admin"});
			List<String> groups = Arrays.asList(new String[]{"GroupA"});
			
			KeycloakAuthenticationToken authentication = 
					new MockKeycloakAuthenticationToken(new MockKeycloakAccount(name, email, roles, groups));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
    }
	
	@Test
	public void getRealmGroups() throws Exception {
		List<WfOwner> realmGroups = new ArrayList<WfOwner>();
		
		WfOwner wfOwner = new WfOwner();
		wfOwner.setName("Διαχείριση προσωπικού");
		wfOwner.setOwnerId("hr");
		
		realmGroups.add(wfOwner);
		when(realmService.getRealmGroups()).thenReturn(realmGroups);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/group").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$[*]").exists());
		
		if (!integration) {
			
			resultActions.andExpect(jsonPath("$[0]").value("GroupA"));
			resultActions.andExpect(jsonPath("$[1]").value("GroupB"));
		}
	}
	
	@Test
	public void getUserGroups() throws Exception {
		
		List<String> userGroups = Arrays.asList(new String[]{"GroupA"});
		
		when(realmService.getUserGroups()).thenReturn(userGroups);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/user/group").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$[*]").exists());
		
		if (!integration) {
			
			resultActions.andExpect(jsonPath("$[0]").value("GroupA"));
		}
	}
	
	@Test
	public void getUser() throws Exception {
		
		String userId = "1";
		WfUser user = new WfUser();
		user.setId("1");
		
		when(realmService.getUser(any(String.class))).thenReturn(user);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/user/{id}", userId)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
		
		if (!integration) {
			
			resultActions.andExpect(jsonPath("$.id").value("1"));
		}
	}
	
	@Test
	public void getUsersByRole() throws Exception {
		
		String role = "supervisor";
		List<WfUser> users = Arrays.asList(new WfUser[]{new WfUser()});
		
		when(realmService.getUsersByRole(any(String.class))).thenReturn(users);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/user/role/{role}", role)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$[*]").exists());
		
	}
	
	@Test
	public void getUsersByGroupAndRole() throws Exception {
		
		String group = "GroupA";
		String role = "supervisor";
		List<WfUser> users = Arrays.asList(new WfUser[]{new WfUser()});
		
		when(realmService.getUsersByGroupAndRole(any(String.class), any(String.class))).thenReturn(users);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/user/group/{group}/role/{role}", group, role)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$[*]").exists());
		
	}
	
	@Test
	public void getUsers() throws Exception {
		
		List<WfUser> users = Arrays.asList(new WfUser[]{new WfUser()});
		
		when(realmService.getAllUsers()).thenReturn(users);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/user")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$[*]").exists());
		
	}
	
	@Test
	public void getSupervisorsByProcess() throws Exception {
		
		int processId = 1;
		List<WfUser> users = Arrays.asList(new WfUser[]{new WfUser()});
		
		when(processService.getSupervisorsByProcess(any(Integer.class))).thenReturn(users);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/user/process/{processId}/supervisor", processId)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		
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
