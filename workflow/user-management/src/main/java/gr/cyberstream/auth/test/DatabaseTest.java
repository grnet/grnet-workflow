package gr.cyberstream.auth.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;

import gr.cyberstream.auth.model.User;
import gr.cyberstream.auth.service.UserManagementService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

@ContextConfiguration("applicationContext.xml")
@Transactional
@TransactionConfiguration
public class DatabaseTest extends AbstractTransactionalTestNGSpringContextTests {

	@Autowired
	private UserManagementService userManagementService;
		
	@BeforeClass
	public void setUp(){
		
	}
	
	@Test
	@Rollback(false)
	public void insertUser(){
		
		User user = new User();
		user.setEmail("george.tylissanakis@cyberstream.gr");
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		user.setPassword(encoder.encode("abc123@"));
		
		user.setFirstname("Giorgos");
		user.setLastname("Tylissanakis");
		
		user.setStatus(User.STATUS_VERIFIED);
		
		userManagementService.saveUser(user);
	}
	
	@Test
	public void getUser(){
		
		User user = userManagementService.getUser("george.tylissanakis@cyberstream.gr");
		
		assert user != null;
		
	}
	
}
