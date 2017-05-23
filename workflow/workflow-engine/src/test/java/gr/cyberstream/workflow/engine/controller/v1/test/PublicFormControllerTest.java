package gr.cyberstream.workflow.engine.controller.v1.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAccount;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAuthenticationToken;
import gr.cyberstream.workflow.engine.controller.v2.PublicFormController;
import gr.cyberstream.workflow.engine.model.ExternalWrapper;
import gr.cyberstream.workflow.engine.model.Registry;
import gr.cyberstream.workflow.engine.model.api.WfProcessMetadata;
import gr.cyberstream.workflow.engine.model.api.WfPublicForm;
import gr.cyberstream.workflow.engine.model.api.WfPublicGroup;
import gr.cyberstream.workflow.engine.model.api.WfPublicService;
import gr.cyberstream.workflow.engine.service.ProcessService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class PublicFormControllerTest {

	final static Logger logger = LoggerFactory.getLogger(DefinitionControllerTest.class);

	private boolean integration = false;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	PublicFormController externalFormController;

	@Mock
	private ProcessService processService;

	private MockMvc mockMvc;

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);

		PublicFormController unwrappedController;

		try {

			if (!integration) {
				unwrappedController = (PublicFormController) unwrapProxy(externalFormController);
				ReflectionTestUtils.setField(unwrappedController, "processService", processService);
			}

			mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

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
	public void getPublicServices() throws Exception {

		List<WfPublicService> publicServices = Arrays.asList(new WfPublicService[] { new WfPublicService() });

		when(processService.getExternalServices()).thenReturn(publicServices);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/public/service").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void getPublicForm() throws Exception {

		String id = "form";
		String client = "mobile";
		WfProcessMetadata processMetadata = new WfProcessMetadata();

		when(processService.getPublicProcessMetadata(any(String.class), any(String.class))).thenReturn(processMetadata);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/public/form/{id}?client=" + client, id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void getProcessPublicForms() throws Exception {

		int id = 1;

		List<WfPublicForm> publicForms = Arrays.asList(new WfPublicForm[] { new WfPublicForm() });

		when(processService.getExternalFromsByDefinitionId(any(Integer.class))).thenReturn(publicForms);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/form/process/{id}", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void createPublicForm() throws Exception {

		WfPublicForm form = new WfPublicForm();

		when(processService.createPublicForm(any(WfPublicForm.class))).thenReturn(form);

		ResultActions resultActions = mockMvc
				.perform(post("/api/v2/form").content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void updatePublicForm() throws Exception {

		WfPublicForm form = new WfPublicForm();

		when(processService.updateExternalForm(any(WfPublicForm.class))).thenReturn(form);

		ResultActions resultActions = mockMvc
				.perform(put("/api/v2/form").content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void deletePublicForm() throws Exception {

		String id = "form";

		doNothing().when(processService).deleteExternalForm(any(String.class));

		mockMvc.perform(delete("/api/v2/form/{id}", id).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void getExternalForms() throws Exception {

		List<WfPublicForm> externalForms = Arrays.asList(new WfPublicForm[] { new WfPublicForm() });

		when(processService.getExternalFromsByDefinitionId(any(Integer.class))).thenReturn(externalForms);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/form/process/{id}", 1).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void modifyExternalFormStatus() throws Exception {

		String id = "form";
		String action = "suspend";

		WfPublicForm form = new WfPublicForm();

		when(processService.modifyExternalFormStatus(any(String.class), any(Boolean.class))).thenReturn(form);

		ResultActions resultActions = mockMvc.perform(put("/api/v2/form/{id}/{action}", id, action))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$.*").exists());
	}

	/**
	 * <code>GET: /api/v2/form/group/wrapped</code>
	 * 
	 * Returns all available external forms ordered by group
	 * 
	 * @return
	 * @throws Exception
	 */
	@Test
	public void getWrappedGroupsForms() throws Exception {

		List<ExternalWrapper> externalForms = Arrays.asList(new ExternalWrapper[] { new ExternalWrapper() });

		when(processService.getExternalWrapper()).thenReturn(externalForms);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/form/group/wrapped").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void getPublicGroups() throws Exception {

		List<WfPublicGroup> groups = Arrays.asList(new WfPublicGroup[] { new WfPublicGroup() });

		when(processService.getExternalGroups()).thenReturn(groups);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/form/group").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void createPublicGroup() throws Exception {

		doNothing().when(processService).createExternalGroup(any(WfPublicGroup.class));

		mockMvc.perform(post("/api/v2/form/group").content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void getRegistries() throws Exception {

		List<Registry> registries = Arrays.asList(new Registry[] { new Registry() });

		when(processService.getRegistries()).thenReturn(registries);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/registry").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void createRegistry() throws Exception {

		doNothing().when(processService).createRegistry(any(Registry.class));

		mockMvc.perform(post("/api/v2/registry").content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void updateRegistry() throws Exception {

		doNothing().when(processService).updateRegistry(any(Registry.class));

		mockMvc.perform(put("/api/v2/registry").content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void deleteRegistry() throws Exception {

		String id = "reg";

		doNothing().when(processService).deleteRegistry(any(String.class));

		mockMvc.perform(delete("/api/v2/registry/{id}", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
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
