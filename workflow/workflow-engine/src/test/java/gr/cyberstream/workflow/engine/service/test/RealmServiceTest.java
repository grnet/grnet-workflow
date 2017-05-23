package gr.cyberstream.workflow.engine.service.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.model.api.WfOwner;
import gr.cyberstream.workflow.engine.model.api.WfRole;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.service.RealmService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class RealmServiceTest {

	final static Logger logger = LoggerFactory.getLogger(RealmServiceTest.class);

	@Autowired
	private RealmService realmService;

	@Test
	public void getRealmGroups() {

		List<WfOwner> realmGroups = realmService.getRealmGroups();

		assertNotNull("Getting realm goups failed.", realmGroups);
	}

	@Test
	public void getUsersByRole() {

		List<WfUser> users = realmService.getUsersByRole("ROLE_Supervisor");

		assertNotNull("Getting realm goups failed.", users);
	}

	@Test
	public void getUser() {
		WfUser user;

		try {
			user = realmService.getUser("9680998b-1999-4c1a-8df6-55b38f343bac");
			logger.info(user.getEmail());
			assertTrue(user != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void getAllUsers() {
		List<WfUser> userList = new ArrayList<>();

		try {
			userList = realmService.getAllUsers();
			for (WfUser user : userList) {
				logger.info("Username " + user.getUsername());
			}

			assertTrue(userList.size() > 0);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void getUsersByGroup() {
		List<WfUser> userList = new ArrayList<>();
		
		try {
			userList = realmService.getUsersByGroup("Τεχνική Υπηρεσία");
			
			for(WfUser user : userList) {
				logger.info(user.getLastName());
			}
			assertTrue(userList.size() > 0);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void getUsersByGroupAndRole() {
		List<WfUser> users = new ArrayList<>();
		
		try {
			users = realmService.getUsersByGroupAndRole("Τεχνική Υπηρεσία", "ROLE_Manager");
			
			for(WfUser wfUser : users) {
				logger.info(wfUser.getEmail());
			}
			
			assertTrue(users.size() > 0);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void syncRoles() {
		
		try {
			
			List<WfRole> roles = realmService.synchronizeRoles();
			
			assert(roles != null && roles.size() > 0);
		
		} catch(Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	@Test
	public void syncOwners() {
		
		try {
			
			List<WfOwner> owners = realmService.synchronizeOwners();
			
			assert(owners != null && owners.size() > 0);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			assert(false);
		}
	}
}
