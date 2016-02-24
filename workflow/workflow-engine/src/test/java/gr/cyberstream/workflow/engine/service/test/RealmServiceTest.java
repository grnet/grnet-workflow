package gr.cyberstream.workflow.engine.service.test;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.service.RealmService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RealmServiceTest {

	final static Logger logger = LoggerFactory.getLogger(RealmServiceTest.class);

	@Autowired
	private RealmService realmService;
	
	@Test
	public void getRealmGroups() {
		
		List<String> realmGroups = realmService.getRealmGroups();
		
		assertNotNull("Getting realm goups failed.", realmGroups);
	}
	
	@Test
	public void getUsersByRole() {
		
		List<WfUser> users = realmService.getUsersByRole("supervisor");
		
		assertNotNull("Getting realm goups failed.", users);
	}
	
}
