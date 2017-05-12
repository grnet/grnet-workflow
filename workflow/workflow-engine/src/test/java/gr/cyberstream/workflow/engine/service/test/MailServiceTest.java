package gr.cyberstream.workflow.engine.service.test;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.MailService;
import gr.cyberstream.workflow.engine.service.ProcessService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class MailServiceTest {

	@Autowired
	private MailService mailService;

	@Autowired
	private Processes processRepository;

	@Autowired
	private ProcessService processService;

	private static final Logger logger = LoggerFactory.getLogger(MailServiceTest.class);

	private String recipient = "kostas.koutros@cyberstream.gr";
	private String taskName = "Έλεγχος Αναφοράς";
	private String taskId = "60056";

	@Test
	public void shouldSendTaskAssignedMail() {

		try {
			mailService.sendTaskAssignedMail(recipient, taskId, taskName, new Date());
			assertTrue(true);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldSendDueTaskMail() {

		try {
			mailService.sendDueTaskMail(recipient, taskId, taskName, new Date(), true);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldSendTaskExpiredMail() {

		try {
			mailService.sendTaskExpiredMail(recipient, taskId, taskName, new Date(), false);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldSendBpmnErrorEmail() {

		try {
			WorkflowDefinition workflowDefinition = processRepository.getById(8);
			mailService.sendBpmnErrorEmail(recipient, workflowDefinition, taskName);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void evaluateAlerts() {

		try {
			processService.evaluateAlerts();
			assertTrue(true);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

}
