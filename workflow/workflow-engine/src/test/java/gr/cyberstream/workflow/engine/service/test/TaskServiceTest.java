package gr.cyberstream.workflow.engine.service.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAccount;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAuthenticationToken;
import gr.cyberstream.workflow.engine.model.UserTaskDetails;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.TaskService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class TaskServiceTest {

	@Autowired
	private Processes processRepository;
	
	@Autowired
	private TaskService taskService;

	private static final Logger logger = LoggerFactory.getLogger(TaskServiceTest.class);

	@Before
	public void setup() {
		String name = "Kostas Koutros";
		String email = "kostas.koutros@cyberstream.gr";
		Set<String> roles = Sets.newSet(new String[] { "ROLE_Admin" });
		List<String> groups = Arrays.asList(new String[] { "HR" });

		KeycloakAuthenticationToken authentication = new MockKeycloakAuthenticationToken(
				new MockKeycloakAccount(name, email, roles, groups));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	public void shouldGetVersionTaskDetails() {
		int versionId = 87;

		try {
			List<WfTaskDetails> taskDetails = taskService.getVersionTaskDetails(versionId);
			assertTrue(taskDetails != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetSupervisedTasks() {

		try {
			List<WfTask> tasks = taskService.getSupervisedTasks();
			assertTrue(tasks != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetTasksByInstanceId() {
		String instanceId = "240026";

		try {
			List<WfTask> tasks = taskService.getTasksByInstanceId(instanceId);
			assertTrue(tasks != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetUserCompletedTasks() {

		try {
			List<WfTask> tasks = taskService.getUserCompletedTasks();
			assertTrue(tasks != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldSearchForCompletedTasks() {
		String definitionKey = "waterSupplyNetworkDamage:1:227504";
		String instanceTitle = "Αναφορά βλάβης δικτύου ύδρευσης";
		Date currentDate = new Date();
		Long before = currentDate.getTime() - 259200000;
		String isSupervisor = "true";

		try {
			List<WfTask> tasks = taskService.searchCompletedTasks(definitionKey, instanceTitle, currentDate.getTime(), before, isSupervisor);
			assertTrue(tasks != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetUserCompledTasksByInstanceIds() {
		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("240026");

		try {
			List<WfTask> tasks = taskService.getUserCompledTasksByInstanceIds(instanceIds);
			assertTrue(tasks != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetTaskById() {
		String taskId = "240052";

		try {
			WfTask task = taskService.getTask(taskId);
			assertTrue(task != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback
	public void shouldGetTaskFormPropertiesByTaskDefintionKey() {
		String taskDefinitionKey = "requestCheck";
		String processDefinitionId = "waterSupplyNetworkDamage:1:227504";

		try {
			List<WfFormProperty> wfFormProperties = taskService.getTaskFormPropertiesByTaskDefintionKey(taskDefinitionKey, processDefinitionId);
			assertTrue(wfFormProperties != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldCompleteTask() throws Exception {
		String taskId = "240052";
		
		try {
			WfTask task = taskService.getTask(taskId);
			// it will throw exception since there is not set "checkResult" value 
			taskService.completeTask(task);
			assertTrue(true);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
			throw e;
		}
	}
	
	@Test
	public void shouldCompleteTaskWithFiles() throws Exception {
		String taskId = "240052";
		byte[] content = Files.readAllBytes(Paths.get("C:/temp/bpmn/integrationTest.bpmn"));
		MockMultipartFile file = new MockMultipartFile("file", "", MediaType.TEXT_XML_VALUE, content);
		MockMultipartFile[] files = new MockMultipartFile[1];
		files[0] = file;
		
		try {
			WfTask task = taskService.getTask(taskId);
			// it will throw exception since there is not set "checkResult" value 
			taskService.completeTask(task, files);
			assertTrue(true);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
			throw e;
		}
	}
	
	@Test
	public void shouldTemporarySaveTask() throws Exception {
		String taskId = "240052";
		
		try {
			WfTask task = taskService.getTask(taskId);
			taskService.tempTaskSave(task);
			assertTrue(true);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
			throw e;
		}
	}
	
	@Test
	public void shouldTemporarySaveTaskWithFiles() throws Exception {
		String taskId = "240052";
		byte[] content = Files.readAllBytes(Paths.get("C:/temp/bpmn/integrationTest.bpmn"));
		MockMultipartFile file = new MockMultipartFile("file", "", MediaType.TEXT_XML_VALUE, content);
		MockMultipartFile[] files = new MockMultipartFile[1];
		files[0] = file;
		
		try {
			WfTask task = taskService.getTask(taskId);
			taskService.tempTaskSave(task, files);
			assertTrue(true);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
			throw e;
		}
	}
	
	@Test
	public void shouldGetCandidatesByTask() throws Exception {
		String taskId = "240052";
		
		try {
			// will return an empty list if no candidates found
			List<WfUser> candidates = taskService.getCandidatesByTaskId(taskId);
			assertTrue(candidates != null);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
			throw e;
		}
	}
	
	@Test
	public void shouldGetTasksForLoggedInUser() {
		
		try {
			// will return an empty list if no candidates found
			List<WfTask> WfTasks = taskService.getTasksForUser();
			assertTrue(WfTasks != null);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldAssignTask() {
		String assignee = "kostas.koutros@cyberstream.gr";
		String taskId = "240052";
		
		try {
			WfTask task = taskService.getTask(taskId);
			taskService.assignTask(task, assignee);
			assertTrue(true);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldAssignTaskWithFiles() throws IOException {
		String assignee = "kostas.koutros@cyberstream.gr";
		String taskId = "240052";
		byte[] content = Files.readAllBytes(Paths.get("C:/temp/bpmn/integrationTest.bpmn"));
		MockMultipartFile file = new MockMultipartFile("file", "", MediaType.TEXT_XML_VALUE, content);
		MockMultipartFile[] files = new MockMultipartFile[1];
		files[0] = file;
		
		try {
			WfTask task = taskService.getTask(taskId);
			taskService.assignTask(task, assignee, files);
			assertTrue(true);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shoulUnClaimTask() {
		String taskId = "240052";
		
		try {
			taskService.unClaimTask(taskId);
			assertTrue(true);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shoulClaimTask() {
		String taskId = "240052";
		
		try {
			taskService.claimTask(taskId);
			assertTrue(true);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetClaimedTasks() {
		
		try {
			// will return an empty list if no tasks found
			List<WfTask> tasks = taskService.getCandidateUserTasks();
			assertTrue(tasks != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldUpdateTaskDetails() {
		String taskDefinitionKey = "requestCheck";
		String processDefinitionId = "waterSupplyNetworkDamage:1:227504";
		
		try {
			UserTaskDetails taskDetails = new UserTaskDetails();
			taskDetails = processRepository.getUserTaskDetailByDefinitionKey(taskDefinitionKey, processDefinitionId);
			assertTrue(taskService.updateTaskDetails(new WfTaskDetails(taskDetails)) != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetAllActiveTasks() {
		
		try {
			List<WfTask> tasks = taskService.getAllActiveTasks();
			assertTrue(tasks != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

}
