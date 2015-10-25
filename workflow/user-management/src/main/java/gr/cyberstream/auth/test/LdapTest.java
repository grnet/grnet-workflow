package gr.cyberstream.auth.test;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;

import gr.cyberstream.auth.dao.RolesDAO;
import gr.cyberstream.auth.dao.UsersDAO;
import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;
import gr.cyberstream.auth.util.PasswordUtil;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

@ContextConfiguration("applicationContext.xml")
@Transactional
@TransactionConfiguration
public class LdapTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private UsersDAO usersDAO;

	@Autowired
	private RolesDAO rolesDAO;
	
	@Autowired
	private PasswordUtil passwordUtil;

	@BeforeClass
	public void setUp() {

	}

	@Test
	public void insertUser() {

		User user = new User();
		user.setEmail("george.tylissanakis@cyberstream.gr");

		user.setPassword(passwordUtil.encode("abc123@"));

		user.setAccountType(User.SIMPLE_ACCOUNT);
		
		user.setCreationDate(new Date());
		
		user.setFirstname("Giorgos");
		user.setLastname("Tylissanakis");

		user.setStatus(User.STATUS_VERIFIED);

		usersDAO.saveUser(user);
	}

	@Test
	public void updateUser() {

		User user = usersDAO.getUser("gtyliss@gmail.com");
		
		user.setStatus(User.STATUS_VERIFIED);

		usersDAO.updateUser(user);
	}

	@Test
	public void getUsers() {

		List<User> users = usersDAO.getUsers(User.STATUS_VERIFIED);

		assert users != null;
	}

	@Test
	public void getUser() {

		User user = usersDAO.getUser("george.tylissanakis@cyberstream.gr");

		assert user != null;
	}

	@Test
	public void getUserByKey() {

		User user = usersDAO.getUserByKey("george.tylissanakis@cyberstream.gr");

		assert user != null;
	}
	
	@Test
	public void searchUsers() {

		List<User> users = usersDAO.searchUsers("TES", User.STATUS_VERIFIED);

		assert users != null;
	}

	@Test
	public void insertRole() {

		Role role = new Role();
		role.setName("SimpleUsers");
		role.setDescription("Organization Users");

		rolesDAO.saveRole(role);
	}

	@Test
	public void getRoles() {

		List<Role> roles = rolesDAO.getRoles();

		assert roles != null;
	}

	@Test
	public void getRole() {

		Role role = rolesDAO.getRole("Administrators");

		assert role != null;
	}
	
	@Test
	public void addUserRole() {

		Role role = rolesDAO.getRole("Administrators");
		User user = usersDAO.getUser("george.tylissanakis@cyberstream.gr");

		rolesDAO.addUserRole(user, role);
		
	}
	
	@Test
	public void removeUserRole() {

		Role role = rolesDAO.getRole("Administrators");
		User user = usersDAO.getUser("george.tylissanakis@cyberstream.gr");

		rolesDAO.removeUserRole(user, role);
		
		assert role != null;
	}
	
	@Test
	public void getRoleMembers() {

		Role role = rolesDAO.getRole("Administrators");
		
		List<User> users = usersDAO.getRoleMembers(role);
		
		assert users != null;
	}
}
