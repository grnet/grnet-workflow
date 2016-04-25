package gr.cyberstream.workflow.engine.controller.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAccount;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAuthenticationToken;
import gr.cyberstream.workflow.engine.controller.v2.DefinitionController;
import gr.cyberstream.workflow.engine.model.FBLoginResponse;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfSettings;
import gr.cyberstream.workflow.engine.service.ProcessService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class DefinitionControllerTest {

	final static Logger logger = LoggerFactory.getLogger(DefinitionControllerTest.class);

	private boolean integration = false;
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	DefinitionController processController;
	
	@Mock
	private ProcessService processService;
	
	private MockMvc mockMvc;
	
	@Before
    public void setup() {

		MockitoAnnotations.initMocks(this);
		
		DefinitionController unwrappedController;
		
		try {
		
			if (!integration) {
				unwrappedController = (DefinitionController) unwrapProxy(processController);
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
	public void getAllProcessDefinitions() throws Exception {
		
		List<WfProcess> processes = Arrays.asList(new WfProcess[]{
				new WfProcess()
		});
		
		when(processService.getAll()).thenReturn(processes);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/process").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$[*]").exists());
		
	}
	
	@Test
	public void getProcessDefinition() throws Exception {
		
		int processId = 1;
		
		WfProcess process = new WfProcess();
		
		when(processService.getProcessById(any(Integer.class))).thenReturn(process);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/process/{id}", processId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void getActiveProcessDefinitions() throws Exception {
		
		List<WfProcess> processes = Arrays.asList(new WfProcess[]{
				new WfProcess()
		});
		
		when(processService.getActiveProcessDefinitions()).thenReturn(processes);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/process/active").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$[*]").exists());
	}
	
	@Test
	public void getProcessDefinitionForm() throws Exception {
		
		int processId = 1;
		
		WfProcess process = new WfProcess();
		
		when(processService.getProcessMetadata(any(Integer.class))).thenReturn(process);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/process/{id}/form", processId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void filterProcessDefinitions() throws Exception {
		
		List<WfProcess> processes = Arrays.asList(new WfProcess[]{
				new WfProcess()
		});
		
		when(processService.getDefinitionsByOwners(any(List.class))).thenReturn(processes);
		when(processService.getAll()).thenReturn(processes);
		
		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/process/filter/owners;owner=GroupA,GroupB")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$[*]").exists());
		
	}
	
	@Test
	public void filterAllProcessDefinitions() throws Exception {
		
		List<WfProcess> processes = Arrays.asList(new WfProcess[]{
				new WfProcess()
		});
		
		when(processService.getDefinitionsByOwners(any(List.class))).thenReturn(processes);
		when(processService.getAll()).thenReturn(processes);
		
		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/process/filter/owners;owner=all")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$[*]").exists());
		
	}
	
	@Test
	public void createProcessDefinition() throws Exception {
		
		byte[] content = Files.readAllBytes(Paths.get("C:/temp/bpmn/DocumentTestProcess.bpmn"));

		when(processService.createNewProcessDefinition(any(InputStream.class),any(String.class)))
			.thenReturn(new WfProcess());

		MockMultipartFile file = new MockMultipartFile("file", "DocumentTestProcess.bpmn",
				MediaType.TEXT_XML_VALUE, content);
		
		mockMvc.perform(
				fileUpload("/api/v2/process")
						.file(file)
						.contentType(MediaType.MULTIPART_FORM_DATA))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void updateProcessDefinition() throws Exception {
		
		when(processService.update(any(WfProcess.class))).thenReturn(new WfProcess());
		
		ResultActions resultActions = mockMvc.perform(put("/api/v2/process")
				.content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void deleteProcessDefinition() throws Exception {
		
		int id = 1;
		
		doNothing().when(processService).deleteProcessDefinition(any(Integer.class));
		
		ResultActions resultActions = mockMvc.perform(delete("/api/v2/process/{id}", id)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void getProcessDiagram() throws Exception {
		
		int id = 1;
		InputStreamResource diagram = new InputStreamResource(new FileInputStream("C:/temp/diagram.jpg"));
		
		when(processService.getProcessDiagram(any(Integer.class))).thenReturn(diagram);
		
		mockMvc.perform(get("/api/v2/process/{id}/diagram", id).accept(MediaType.IMAGE_JPEG))
				.andExpect(status().isOk());
	}
	
	@Test
	public void createProcessVersion() throws Exception {
		
		int processId = 1;
		
		byte[] content = Files.readAllBytes(Paths.get("C:/temp/bpmn/DocumentTestProcess.bpmn"));

		when(processService.createNewProcessVersion(any(Integer.class), any(InputStream.class),any(String.class)))
			.thenReturn(new WfProcessVersion());

		MockMultipartFile file = new MockMultipartFile("file", "DocumentTestProcess.bpmn",
				MediaType.TEXT_XML_VALUE, content);
		
		mockMvc.perform(
				fileUpload("/api/v2/process/{id}/version", processId)
						.file(file).contentType(MediaType.MULTIPART_FORM_DATA))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void activateProcessDefinitionVersion() throws Exception {
		
		int processId = 1;
		int versionId = 1;
		
		when(processService.setActiveVersion(any(Integer.class), any(Integer.class)))
			.thenReturn(new WfProcess());
		
		ResultActions resultActions = mockMvc.perform(
				put("/api/v2/process/{processId}/version/{versionId}/active", processId, versionId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void deactivateProcessDefinitionVersion() throws Exception {
		
		int processId = 1;
		int versionId = 1;
		
		when(processService.deactivateVersion(any(Integer.class), any(Integer.class)))
			.thenReturn(new WfProcessVersion());
		
		ResultActions resultActions = mockMvc.perform(
				put("/api/v2/process/{processId}/version/{versionId}/inactive", processId, versionId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void deleteProcessDefinitionVersion() throws Exception {
		
		int processId = 1;
		int deploymentId = 1;
		
		when(processService.deleteProcessDefinitionVersion(any(Integer.class), any(String.class)))
			.thenReturn(new WfProcess());
		
		ResultActions resultActions = mockMvc.perform(
				delete("/api/v2/process/version/{processId}/{deploymentId}", processId, deploymentId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void getSettings() throws Exception {
		
		WorkflowSettings settings = new WorkflowSettings();
		
		when(processService.getSettings()).thenReturn(settings);
		
		ResultActions resultActions = mockMvc.perform(get("/api/v2/settings").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
		
	}
	
	@Test
	public void updateSettings() throws Exception {
		
		WorkflowSettings settings = new WorkflowSettings();
		
		when(processService.updateSettings(any(WfSettings.class))).thenReturn(settings);
		
		ResultActions resultActions = mockMvc.perform(put("/api/v2/settings")
				.content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		resultActions.andExpect(jsonPath("$.*").exists());
	}
	
	@Test
	public void claimFacebookToken() throws Exception {
		
		when(processService.claimPermanentAccessToken(any(FBLoginResponse.class))).thenReturn(true);
		
		mockMvc.perform(post("/api/v2/facebook")
				.content("{}").contentType(MediaType.APPLICATION_JSON))
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
