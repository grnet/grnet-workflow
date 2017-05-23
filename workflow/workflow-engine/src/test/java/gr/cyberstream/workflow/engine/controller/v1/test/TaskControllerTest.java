package gr.cyberstream.workflow.engine.controller.v1.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import gr.cyberstream.workflow.engine.controller.v2.TaskController;
import gr.cyberstream.workflow.engine.model.api.WfDocument;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.service.ProcessService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("deprecation")
public class TaskControllerTest {

	final static Logger logger = LoggerFactory.getLogger(DefinitionControllerTest.class);

	private boolean integration = false;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	TaskController taskController;

	@Mock
	private ProcessService processService;

	private MockMvc mockMvc;

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);

		TaskController unwrappedController;

		try {

			if (!integration) {
				unwrappedController = (TaskController) unwrapProxy(taskController);
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
	public void getTaskDetailsByDefinition() throws Exception {

		int id = 1;
		List<WfTaskDetails> taskDetails = Arrays.asList(new WfTaskDetails[] { new WfTaskDetails() });

		when(processService.getVersionTaskDetails(any(Integer.class))).thenReturn(taskDetails);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/task/process/version/{id}", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void getTaskCandidates() throws Exception {

		String id = "task";
		List<WfUser> users = Arrays.asList(new WfUser[] { new WfUser() });

		when(processService.getCandidatesByTaskId(any(String.class))).thenReturn(users);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/task/{id}/candidates", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void getTasksByExecutionId() throws Exception {

		String id = "execution";
		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getTasksByInstanceId(any(String.class))).thenReturn(tasks);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/task/execution/{id}", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void getSupervisedTasks() throws Exception {

		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getSupervisedTasks()).thenReturn(tasks);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/task/supervised").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void getAssignedTasks() throws Exception {

		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getTasksForUser()).thenReturn(tasks);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/task/assigned").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void getCompletedTasks() throws Exception {

		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getUserCompletedTasks()).thenReturn(tasks);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/task/completed").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void filterUserCompletedTasks() throws Exception {

		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getUserCompledTasksByInstanceIds(any(List.class))).thenReturn(tasks);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/tasks/completed/user;instance=1,2").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void filterAllCompletedTasks() throws Exception {

		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getCompletedTasksByInstances(any(List.class))).thenReturn(tasks);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/tasks/completed/all;instance=1,2").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void getClaimableTasks() throws Exception {

		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getCandidateUserTasks()).thenReturn(tasks);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/task/claim").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void searchEndedExecutionTasks() throws Exception {

		String title = "title";
		long after = 0;
		long before = 0;
		boolean anonymous = false;

		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getEndedProcessInstancesTasks(any(String.class), any(Long.class), any(Long.class),
				any(Boolean.class))).thenReturn(tasks);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/task/execution/ended/search:{title},{after},{before},{anonymous}", title, after,
						before, anonymous).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void searchUserTasks() throws Exception {

		long after = 0;
		long before = 0;
		String userId = "user";

		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getUserActivity(any(Long.class), any(Long.class), any(String.class))).thenReturn(tasks);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/task/search:{after},{before}/assignee/{userId}", after, before, userId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void searchCompletedTasks() throws Exception {

		String definitionKey = "key";
		String executionTitle = "title";
		long after = 0;
		long before = 0;
		String isSupervisor = "false";

		List<WfTask> tasks = Arrays.asList(new WfTask[] { new WfTask() });

		when(processService.getSearchedCompletedTasks(any(String.class), any(String.class), any(Long.class),
				any(Long.class), any(String.class))).thenReturn(tasks);

		ResultActions resultActions = mockMvc.perform(
				get("/api/v2/task/completed/search:{definitionKey},{executionTitle},{after},{before},{isSupervisor}",
						definitionKey, executionTitle, after, before, isSupervisor).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void getTask() throws Exception {

		String id = "task";
		WfTask task = new WfTask();

		when(processService.getTask(any(String.class))).thenReturn(task);

		ResultActions resultActions = mockMvc.perform(get("/api/v2/task/{id}", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());

	}

	@Test
	public void updateTask() throws Exception {

		when(processService.updateTaskDetails(any(WfTaskDetails.class))).thenReturn(new WfTaskDetails());

		mockMvc.perform(put("/api/v2/task").content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void claimTask() throws Exception {

		String id = "task";

		doNothing().when(processService).claimTask(any(String.class));

		mockMvc.perform(put("/api/v2/task/{id}/claim", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void removeTask() throws Exception {

		String id = "task";

		doNothing().when(processService).unClaimTask(any(String.class));

		mockMvc.perform(put("/api/v2/task/{id}/unclaim", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void assignTask() throws Exception {

		String assignee = "user";

		doNothing().when(processService).assignTask(any(WfTask.class), any(String.class));

		mockMvc.perform(
				post("/api/v2/task/assign/{assignee}", assignee).content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void assignTaskWithFiles() throws Exception {

		String assignee = "user";
		String taskData = "{}";

		byte[] content = Files.readAllBytes(Paths.get("C:/temp/Performance_Tuning_Michael_Han.pdf"));

		doNothing().when(processService).assignTask(any(WfTask.class), any(String.class), any(MultipartFile[].class));

		MockMultipartFile data = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_VALUE,
				taskData.getBytes());
		MockMultipartFile file = new MockMultipartFile("file", "Performance_Tuning_Michael_Han.pdf",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, content);

		mockMvc.perform(fileUpload("/api/v2/task/assign/{assignee}", assignee).file(data).file(file)
				.contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isOk());
	}

	@Test
	public void saveTask() throws Exception {

		doNothing().when(processService).tempTaskSave(any(WfTask.class));

		mockMvc.perform(post("/api/v2/task/save").content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void saveTaskWithFiles() throws Exception {

		String taskData = "{}";

		byte[] content = Files.readAllBytes(Paths.get("C:/temp/Performance_Tuning_Michael_Han.pdf"));

		doNothing().when(processService).tempTaskSave(any(WfTask.class), any(MultipartFile[].class));

		MockMultipartFile data = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_VALUE,
				taskData.getBytes());
		MockMultipartFile file = new MockMultipartFile("file", "Performance_Tuning_Michael_Han.pdf",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, content);

		mockMvc.perform(
				fileUpload("/api/v2/task/save").file(data).file(file).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk());
	}

	@Test
	public void completeTask() throws Exception {

		doNothing().when(processService).completeTask(any(WfTask.class));

		mockMvc.perform(post("/api/v2/task/complete").content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void completeTaskWithFiles() throws Exception {

		String taskData = "{}";

		byte[] content = Files.readAllBytes(Paths.get("C:/temp/Performance_Tuning_Michael_Han.pdf"));

		doNothing().when(processService).completeTask(any(WfTask.class), any(MultipartFile[].class));

		MockMultipartFile data = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_VALUE,
				taskData.getBytes());
		MockMultipartFile file = new MockMultipartFile("file", "Performance_Tuning_Michael_Han.pdf",
				MediaType.APPLICATION_OCTET_STREAM_VALUE, content);

		mockMvc.perform(
				fileUpload("/api/v2/task/complete").file(data).file(file).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk());
	}

	@Test
	public void getTaskForm() throws Exception {

		String taskDefinitionKey = "taskKey";
		String processDefinitionId = "processId";

		List<WfFormProperty> form = Arrays.asList(new WfFormProperty[] { new WfFormProperty() });

		when(processService.getTaskFormPropertiesByTaskDefintionKey(any(String.class), any(String.class)))
				.thenReturn(form);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/task/definition/{taskDefinitionKey}/process/{processDefinitionId}",
						taskDefinitionKey, processDefinitionId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		resultActions.andExpect(jsonPath("$[*]").exists());
	}

	@Test
	public void saveTaskFormElement() throws Exception {

		String formProperty = "{\"id\": \"property\", \"name\": \"property\", "
				+ "\"type\": \"type\", \"readable\": false, \"writable\": false, \"required\": false, "
				+ "\"format\": \"format\", \"description\": \"descripion\", \"value\": \"value\"}";
		String taskDefinitionKey = "taskKey";
		String processDefinitionKey = "processKey";

		when(processService.saveTaskFormElement(any(WfFormProperty.class), any(String.class), any(String.class)))
				.thenReturn(null);

		mockMvc.perform(put("/api/v2/task/definition/{taskDefinitionKey}/process/{processDefinitionKey}/formelement",
				taskDefinitionKey, processDefinitionKey).content(formProperty).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void getDocumentsByTask() throws Exception {

		int id = 1;
		List<WfDocument> documents = Arrays.asList(new WfDocument[] { new WfDocument() });

		when(processService.getProcessInstanceDocumentsByTask(any(Integer.class))).thenReturn(documents);

		ResultActions resultActions = mockMvc
				.perform(get("/api/v2/task/{id}/document", id).accept(MediaType.APPLICATION_JSON))
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
