package gr.cyberstream.workflow.engine.service.test;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.service.MailService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MailServiceTest {

	final static Logger logger = LoggerFactory.getLogger(MailServiceTest.class);

	@Autowired
	private MailService mailService;
	
	@Test
	public void shouldSendMail() {
		
		mailService.sendTaskAssignedMail("george.tylissanakis@cyberstream.gr", "10023", "Form Review", new Date());
	}
}
