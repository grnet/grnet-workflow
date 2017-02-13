package gr.cyberstream.workflow.engine.service.test;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.service.MailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class MailServiceTest {

	final static Logger logger = LoggerFactory.getLogger(MailServiceTest.class);

	@Autowired
	private MailService mailService;

	@Test
	public void shouldSendMail() {
		WfTask wfTask = new WfTask();
		wfTask.setName("kostas.koutros@cyberstream.gr");
		Date date = new Date();
		wfTask.setDueDate(date);
		wfTask.setId("10023");
		wfTask.setDefinitionName("Form Review");
		mailService.sendTaskAssignedMail("kostas.koutros@cyberstream.gr", wfTask);
	}
}
