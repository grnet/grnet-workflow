package gr.cyberstream.workflow.engine.service.test;

import java.nio.file.Files;
import java.nio.file.Paths;
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
import gr.cyberstream.workflow.engine.customservicetasks.DocumentMail;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.ProcessService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("deprecation")
public class ActivitiIntegrationTest {

	@Autowired
	private ProcessService processService;

	@Autowired
	private Processes processRepository;

	@Mock
	DocumentMail configurationService;

	private static final Logger logger = LoggerFactory.getLogger(ActivitiIntegrationTest.class);

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);
		String name = "Kostas Koutros";
		String email = "kostas.koutros@cyberstream.gr";
		Set<String> roles = Sets.newSet(new String[] { "ROLE_Admin" });
		List<String> groups = Arrays.asList(new String[] { "HR" });

		KeycloakAuthenticationToken authentication = new MockKeycloakAuthenticationToken(
				new MockKeycloakAccount(name, email, roles, groups));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 * Main integration function used in order to create a new process
	 * definition
	 * 
	 * @throws Exception
	 */
	@Test
	@Rollback
	@Transactional
	public void integrate() throws Exception {

		try {
			// the bpmn file
			byte[] content = Files.readAllBytes(Paths.get("C:/temp/bpmn/integrationTest.bpmn"));

			MockMultipartFile file = new MockMultipartFile("file", "", MediaType.TEXT_XML_VALUE, content);

			// create wfProcess from BPMN File
			WfProcess wfProcess = processService.createNewProcessDefinition(file.getInputStream(),"DocumentTestProcess.bpmn");
			wfProcess.setOwner("HR");
			wfProcess.setAssignBySupervisor(true);
			wfProcess.setActive(true);

			for (WfProcessVersion wfDefinitionVersion : wfProcess.getProcessVersions()) {
				processService.setActiveVersion(wfProcess.getId(), wfDefinitionVersion.getId());
			}
			processService.update(wfProcess);

			WfProcessInstance wfProcessInstance = startInstance(wfProcess);
			checkUsersTasks(wfProcessInstance);

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * Starts an instance
	 * 
	 * @param wfProcess
	 * @return
	 * @throws Exception
	 */
	private WfProcessInstance startInstance(WfProcess wfProcess) throws Exception {
		List<WfFormProperty> formProperties = new ArrayList<WfFormProperty>();
		String positionValue = "{\"latitude\": 38.04085845949551, \"longitude\": 23.8362979888916}";

		WfFormProperty nameProperty = new WfFormProperty("name", "Όνοματεπώνυμο", "string", "kostas koutros", true,
				true, true, null, "", "", "");
		WfFormProperty emailProperty = new WfFormProperty("email", "Ηλεκτρονική διεύθυνση", "email",
				"kostas.koutros@cyberstream.gr", true, true, true, null, "", "", "");
		WfFormProperty phoneProperty = new WfFormProperty("phoneNo", "Τηλέφωνο", "string", "21012345678", true, true,
				true, null, "", "", "");
		WfFormProperty addressProperty = new WfFormProperty("position", "Διεύθυνση", "string", "Διεύθυνση 59", true,
				true, true, null, "", "", "");

		WfFormProperty positionProperty = new WfFormProperty("position", "θέση στο χάρτη", "position", positionValue,
				true, true, true, null, "", "", "");

		formProperties.add(nameProperty);
		formProperties.add(emailProperty);
		formProperties.add(phoneProperty);
		formProperties.add(addressProperty);
		formProperties.add(positionProperty);

		WorkflowDefinition definition = processRepository.getById(wfProcess.getId());

		WorkflowInstance instance = new WorkflowInstance();
		instance.setTitle("A test instance");
		instance.setReference("Reference");
		instance.setSupervisor("kostas.koutros@cyberstream.gr");
		instance.setDefinitionVersion(definition.getActiveVersion());

		WfProcessInstance instanceData = new WfProcessInstance(instance);
		instanceData.setProcessForm(formProperties);

		WfProcessInstance wfProcessInstance = processService.startProcess(wfProcess.getId(), instanceData);

		return wfProcessInstance;
	}

	private void checkUsersTasks(WfProcessInstance wfProcessInstance) throws Exception {
		String checkResult = "spotCheck";
		String spotCheckResult = "repair";

		int phase = 1;

		List<WfTask> instanceTasks = new ArrayList<>();

		try {
			// get instance tasks
			instanceTasks = processService.getTasksByInstanceId(wfProcessInstance.getId());

			while (instanceTasks.size() > 0) {
				logger.info("Phase " + phase + " task list size: " + instanceTasks.size());

				for (WfTask task : instanceTasks) {

					// set assignee if null
					if (task.getAssignee() == null) 
						task.setAssignee("kostas.koutros@cyberstream.gr");

					// means that task has form properties
					if (task.getTaskForm() != null) {
						
						List<WfFormProperty> taskFormProperties = new ArrayList<>();

						for (WfFormProperty property : task.getTaskForm()) {
							String formPropertType = property.getType();
							boolean isWritable = property.isWritable();
							WfFormProperty formProperty = new WfFormProperty();

							if (isWritable) {
								switch (formPropertType) {

								case "string":
									formProperty.setType(property.getType());
									formProperty.setId(property.getId());
									formProperty.setValue("A Test String");
									formProperty.setReadable(property.isReadable());
									formProperty.setWritable(property.isWritable());
									formProperty.setRequired(property.isRequired());
									break;

								case "enum":
									formProperty.setType(property.getType());
									formProperty.setId(property.getId());

									if (formProperty.getId().equals("spotCheckResult"))
										formProperty.setValue(spotCheckResult);
									else if (formProperty.getId().equals("checkResult"))
										formProperty.setValue(checkResult);

									formProperty.setFormValues(property.getFormValues());
									formProperty.setReadable(property.isReadable());
									formProperty.setWritable(property.isWritable());
									formProperty.setRequired(property.isRequired());
									break;

								case "textarea":
									formProperty.setType(property.getType());
									formProperty.setId(property.getId());
									formProperty.setReadable(property.isReadable());
									formProperty.setWritable(property.isWritable());
									formProperty.setRequired(property.isRequired());
									formProperty.setValue("No comments");
									break;
								}
								taskFormProperties.add(formProperty);
								task.setTaskForm(taskFormProperties);
							}
						}
					}

					if (task.getEndDate() == null) {
						logger.info("Complete task: " + task.getName() + " with id: " + task.getId());
						processService.completeTask(task);
					}
					
					// get instance tasks
					instanceTasks = processService.getTasksByInstanceId(wfProcessInstance.getId());
					phase++;

				} // end of for loop
				
				if(phase > 20)
					return;
			}// end of while

		} catch (Exception e) {
			throw e;
		}
	}

}
