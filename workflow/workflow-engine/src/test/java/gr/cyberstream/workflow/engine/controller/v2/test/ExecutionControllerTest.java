package gr.cyberstream.workflow.engine.controller.v2.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
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
import org.springframework.web.multipart.MultipartFile;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAccount;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAuthenticationToken;
import gr.cyberstream.workflow.engine.controller.v2.ExecutionController;
import gr.cyberstream.workflow.engine.model.api.WfDocument;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.service.ProcessService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ExecutionControllerTest {

	final static Logger logger = LoggerFactory.getLogger(DefinitionControllerTest.class);

	private boolean integration = false;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	ExecutionController executionV2Controller;

	@Mock
	private ProcessService processService;

	private MockMvc mockMvc;

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);

		ExecutionController unwrappedController;

		try {

			if (!integration) {
				unwrappedController = (ExecutionController) unwrapProxy(executionV2Controller);
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
	public void getExecutions() throws Exception {

		int id = 1;
		List<WfProcessInstance> executions = Arrays.asList(new WfProcessInstance[] { new WfProcessInstance() });

		when(processService.getActiveProcessInstances(any(Integer.class))).thenReturn(executions);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/execution/process/version/{id}", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void getSupervisedExecutions() throws Exception {

		List<WfProcessInstance> executions = Arrays.asList(new WfProcessInstance[] { new WfProcessInstance() });

		when(processService.getSupervisedInstances()).thenReturn(executions);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/execution/supervised").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void getUserCompletedExecutions() throws Exception {

		List<WfProcessInstance> executions = Arrays.asList(new WfProcessInstance[] { new WfProcessInstance() });

		when(processService.getUserCompletedInstances()).thenReturn(executions);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/execution/completed").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void getExecutionById() throws Exception {

		String id = "execution";
		WfProcessInstance execution = new WfProcessInstance();

		when(processService.getProcessInstanceById(any(String.class))).thenReturn(execution);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/mobile/execution/{id}", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void startProcess() throws Exception {

		int id = 1;
		WfProcessInstance processInstance = new WfProcessInstance();

		when(processService.startProcess(any(Integer.class), any(WfProcessInstance.class))).thenReturn(processInstance);

		ResultActions resultActions = mockMvc
				.perform(post("/api/v2/process/{id}/start", id).content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void startProcessWithDocuments() throws Exception {

		int processId = 1;
		String instanceData = "{}";

		byte[] content = Files.readAllBytes(Paths.get("C:/temp/Performance_Tuning_Michael_Han.pdf"));

		when(processService.startProcess(any(Integer.class), any(WfProcessInstance.class), any(MultipartFile[].class)))
				.thenReturn(new WfProcessInstance());

		MockMultipartFile data = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_VALUE,
				instanceData.getBytes());
		MockMultipartFile file = new MockMultipartFile("file", "Performance_Tuning_Michael_Han.pdf",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, content);

		mockMvc.perform(fileUpload("/api/v2/process/{id}/start", processId).file(data).file(file)
				.contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isOk())
				.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void startPublicProcess() throws Exception {

		String id = "form";
		String instanceData = "{\"captchaAnswer\": \"12345\"}";
		WfProcessInstance processInstance = new WfProcessInstance();

		when(processService.startPublicProcess(any(String.class), any(WfProcessInstance.class)))
				.thenReturn(processInstance);

		ResultActions resultActions = mockMvc.perform(post("/api/v2/public/form/{id}/start", id).content(instanceData)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void startPublicProcessWithDocuments() throws Exception {

		String id = "form";
		String instanceData = "{\"captchaAnswer\": \"12345\"}";

		byte[] content = Files.readAllBytes(Paths.get("C:/temp/Performance_Tuning_Michael_Han.pdf"));

		when(processService.startPublicProcess(any(String.class), any(WfProcessInstance.class),
				any(MultipartFile[].class))).thenReturn(new WfProcessInstance());

		MockMultipartFile data = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_VALUE,
				instanceData.getBytes());
		MockMultipartFile file = new MockMultipartFile("file", "Performance_Tuning_Michael_Han.pdf",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, content);

		mockMvc.perform(fileUpload("/api/v2/public/form/{id}/start", id).file(data).file(file)
				.contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isOk())
				.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void startMobileProcess() throws Exception {

		String id = "form";
		String instanceData = "{}";
		WfProcessInstance processInstance = new WfProcessInstance();

		when(processService.startPublicMobileProcess(any(String.class), any(WfProcessInstance.class)))
				.thenReturn(processInstance);

		ResultActions resultActions = mockMvc.perform(post("/api/v2/mobile/form/{id}/start", id).content(instanceData)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void startMobileProcessWithDocuments() throws Exception {

		String id = "form";
		String instanceData = "{}";

		byte[] content = Files.readAllBytes(Paths.get("C:/temp/Performance_Tuning_Michael_Han.pdf"));

		when(processService.startPublicMobileProcess(any(String.class), any(WfProcessInstance.class),
				any(MultipartFile[].class))).thenReturn(new WfProcessInstance());

		MockMultipartFile data = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_VALUE,
				instanceData.getBytes());
		MockMultipartFile file = new MockMultipartFile("file", "Performance_Tuning_Michael_Han.pdf",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, content);

		mockMvc.perform(fileUpload("/api/v2/mobile/form/{id}/start", id).file(data).file(file)
				.contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isOk())
				.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void deleteExecution() throws Exception {

		int id = 1;

		doNothing().when(processService).deleteProcessCompletedInstance(any(String.class));

		mockMvc.perform(delete("/api/v2/execution/{id}", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void cancelExecution() throws Exception {

		int id = 1;

		doNothing().when(processService).cancelProcessInstance(any(String.class));

		mockMvc.perform(put("/api/v2/execution/{id}/cancel", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void changeExecutionStatus() throws Exception {

		String id = "execution";
		String action = "suspend";

		when(processService.suspendProcessInstance(any(String.class))).thenReturn(new WfProcessInstance());
		when(processService.resumeProcessInstance(any(String.class))).thenReturn(new WfProcessInstance());

		ResultActions resultActions = mockMvc
				.perform(put("/api/v2/execution/{id}/{action}", id, action).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$.*").exists());
	}

	@Test
	public void getExecutionForm() throws Exception {

		int id = 1;
		List<WfFormProperty> form = Arrays.asList(new WfFormProperty[] { new WfFormProperty() });

		when(processService.getStartFormByInstanceId(any(String.class))).thenReturn(form);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/execution/{id}/form", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void getProcessInstanceDocuments() throws Exception {

		int id = 1;
		List<WfDocument> documents = Arrays.asList(new WfDocument[] { new WfDocument() });

		when(processService.getProcessInstanceDocuments(any(Integer.class))).thenReturn(documents);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/execution/{id}/document", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void getExecutionDiagram() throws Exception {

		int id = 1;
		InputStreamResource diagram = new InputStreamResource(new FileInputStream("C:/temp/diagram.jpg"));

		when(processService.getInstanceProgressDiagram(any(String.class))).thenReturn(diagram);

		mockMvc.perform(get("/api/v2/execution/{id}/diagram", id).accept(MediaType.IMAGE_JPEG))
				.andExpect(status().isOk());
	}
	
	@Test
	public void changeProcessInstanceSupervisor() throws Exception {
		String instanceId = "35301";
		String supervisor = "xxxx";
		
		doNothing().when(processService).changeInstanceSupervisor(any(String.class), any(String.class));
		
		mockMvc.perform(post("/api/v2/instance/{instanceId}/supervisor?supervisor=" + supervisor, instanceId).content(supervisor)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}
	
	@Test
	public void getInProgressInstances() throws Exception {
		List<WfProcessInstance> wfProcessInstances = Arrays.asList(new WfProcessInstance[] { new WfProcessInstance() });
		
		when(processService.getInProgressInstances()).thenReturn(wfProcessInstances);
		
		mockMvc.perform(get("/api/v2/inprogress/instances").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
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
