package gr.cyberstream.workflow.engine.service.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAccount;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAuthenticationToken;
import gr.cyberstream.workflow.engine.model.UserTaskFormElement;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;
import gr.cyberstream.workflow.engine.service.ProcessService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional // in order to user rollback
/**
 * Few tests could throw a handled exception and fail.
 * 
 * Unhandled exceptions are issues.
 * 
 * On error function name will be printed as well for better logging.
 * 
 * @author kkoutros
 *
 */
public class ProcessServiceTest {

	private Logger logger = LoggerFactory.getLogger(ProcessServiceTest.class);

	@Inject
	private ProcessService processService;

	@Before
	public void setup() {
		String name = "Kostas Koutros";
		String email = "kostas.koutros@cyberstream.gr";
		Set<String> roles = Sets.newSet(new String[] { "ROLE_User", "ROLE_Admin" });
		List<String> groups = Arrays.asList(new String[] { "WaterSupply" });

		KeycloakAuthenticationToken authentication = new MockKeycloakAuthenticationToken(new MockKeycloakAccount(name, email, roles, groups));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	public void shouldGetProcessById() {
		try {
			int processId = 8;

			WfProcess wfProcess = processService.getProcessById(processId);
			assertTrue(wfProcess != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetAllProcesses() {
		try {
			List<WfProcess> processList = processService.getAll();
			assertTrue(processList != null && processList.size() > 0);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetAllActiveDefinitions() {
		try {
			List<WfProcess> processList = processService.getActiveProcessDefinitions();
			assertTrue(processList != null && processList.size() > 0);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetVersionTaskDetails() {
		try {
			int versionId = 8;

			List<WfTaskDetails> userTaskDetails = processService.getVersionTaskDetails(versionId);
			assertTrue(userTaskDetails != null && userTaskDetails.size() > 0);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetgetActiveProcessInstances() {
		try {
			int processId = 8;

			List<WfProcessInstance> instances = processService.getActiveProcessInstances(processId);
			assertTrue(instances != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldCancelInstance() {
		try {
			String instanceId = "20001";

			processService.cancelProcessInstance(instanceId);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldDeleteInstance() {
		try {
			String instanceId = "20001";

			processService.deleteInstance(instanceId);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldSuspendInstance() {
		try {
			String instanceId = "20001";

			processService.suspendProcessInstance(instanceId);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldResumeInstance() {
		try {
			String instanceId = "20001";

			processService.resumeProcessInstance(instanceId);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldReturnDefinitionsByOwners() {
		try {
			List<String> owners = new ArrayList<>();
			owners.add("WaterSupply");
			owners.add("HR");

			List<WfProcess> definitions = processService.getDefinitionsByOwners(owners);
			assertTrue(definitions != null && definitions.size() > 0);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldUpdateProcess() {
		try {
			WfProcess wfProcess = new WfProcess();
			wfProcess.setId(8);
			wfProcess.setDescription("A description");
			wfProcess.setOwner("WaterSupply");

			assertTrue(processService.update(wfProcess) != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldUpdateProcessVersion() {
		try {
			int processId = 8;

			WfProcessVersion wfProcessVersion = new WfProcessVersion();
			wfProcessVersion.setId(8);
			wfProcessVersion.setDeploymentdate(new Date());
			wfProcessVersion.setStatus("active");
			wfProcessVersion.setVersion(1);

			assertTrue(processService.updateVersion(processId, wfProcessVersion) != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldDeleteProcessDefinition() {
		try {
			int processId = 8;

			processService.deleteProcessDefinition(processId);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldDeleteProcessDefinitionVersion() {
		try {
			int processId = 8;
			String deploymentId = "15001";

			assertTrue(processService.deleteProcessDefinitionVersion(processId, deploymentId) != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldSetActiveVersion() {
		try {
			int processId = 8;
			int versionId = 8;

			processService.setActiveVersion(processId, versionId);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Rollback(true)
	public void shouldDeactivateVersion() {
		try {
			int processId = 8;
			int versionId = 8;

			processService.deactivateVersion(processId, versionId);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	public void shouldGetProcessMetadata() {
		try {
			int processId = 8;
			String device = UserTaskFormElement.ALL_DEVICES;

			assertTrue(processService.getProcessMetadata(processId, device) != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	/**
	 * Will throw an exception if mailService is not started
	 * 
	 * A process with form properties will require to create them in order to
	 * start the instance successfully
	 */
	@Test
	@Rollback(true)
	public void shouldStartInstance() {
		try {
			int processId = 9;

			WfProcessInstance wfProcessInstance = new WfProcessInstance();
			wfProcessInstance.setTitle("Instance v.Test v11"); // should be
																// unique since
																// a folder with
																// that name
																// will be
																// created. As
																// well the
																// folder id
			wfProcessInstance.setFolderId("a folder id v11");
			wfProcessInstance.setSupervisor("kostas.koutros@cyberstream.gr");
			wfProcessInstance.setStartDate(new Date());
			wfProcessInstance.setStatus(WorkflowInstance.STATUS_RUNNING);

			assertTrue(processService.startProcess(processId, wfProcessInstance) != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetSupervisedInstances() {
		try {
			assertTrue(processService.getSupervisedInstances() != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetInstanceDocuments() {
		try {
			String instanceId = "52533";

			assertTrue(processService.getDocumentsByInstance(instanceId) != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetInstanceDocumentsByTaskId() {
		try {
			int taskId = 52562;

			assertTrue(processService.getProcessInstanceDocumentsByTask(taskId) != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetSupervisedTasks() {
		try {
			assertTrue(processService.getSupervisedTasks() != null);

		} catch (Exception e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e.getMessage());
			assertTrue(false);
		}
	}
}
