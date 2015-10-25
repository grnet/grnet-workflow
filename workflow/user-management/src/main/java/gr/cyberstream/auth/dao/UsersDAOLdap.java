package gr.cyberstream.auth.dao;

import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Name;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class UsersDAOLdap implements UsersDAO {

	private LdapTemplate ldapTemplate;
	private Logger logger;
	
	private SimpleDateFormat dateFormat;
	
	public UsersDAOLdap() {

		logger = Logger.getLogger("auth");
		dateFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
	}
	
	@Override
	public boolean comparePassword(User user, String password) {
		
		SpringSecurityLdapTemplate securityLdapTemplate = new SpringSecurityLdapTemplate(ldapTemplate.getContextSource());
		
		return securityLdapTemplate.compare("uid=" + user.getEmail() + ",ou=Users", "userPassword", password);
	}

	@Override
	public void saveUser(User user) {

		Name dn = LdapNameBuilder.newInstance().add("ou", "Users").add("uid", user.getEmail()).build();
		DirContextOperations context = new DirContextAdapter(dn);

		mapToContext(user, context);

		ldapTemplate.bind(context);
				
		ldapTemplate.authenticate(dn, "(status=verified)", user.getPassword());
	}
	
	@Override
	public void updateUser(User user) {

		Name dn = LdapNameBuilder.newInstance().add("ou", "Users").add("uid", user.getEmail()).build();
		DirContextOperations context = ldapTemplate.lookupContext(dn);
		
		mapToContext(user, context);
		
		ldapTemplate.modifyAttributes(context);
	}
	
	public void removeUser(User user) {
		
		Name dn = LdapNameBuilder.newInstance().add("ou", "Users").add("uid", user.getEmail()).build();
		ldapTemplate.unbind(dn);
	}

	@Override
	public User getUser(String email) {

		Name dn = LdapNameBuilder.newInstance().add("ou", "Users").add("uid", email).build();

		User user = null;
		
		try {
			
			user = ldapTemplate.lookup(dn, new UserContextMapper());
			
		} catch (NameNotFoundException e) {
			
			logger.info("User " + dn + " not found.");
		}
		
		return user;
	}

	@Override
	public User getUserByKey(String key) {

		User user = null;
		
		try {
			user = ldapTemplate.searchForObject(
				query().where("objectclass").is("csUser").and("activationKey").is(key),
				new UserContextMapper());
			
		} catch (EmptyResultDataAccessException e) {
		
			logger.warn("No User found for key: " + key);
		}
		
		return user;
	}
	
	@Override
	public List<User> searchUsers(String keyword, String... statusArray) {
		
		ContainerCriteria criteria = query().where("objectclass").is("csUser");
		
		if (statusArray != null) {
			
			ContainerCriteria statusCriteria = null;
			
			for (int i = 0; i < statusArray.length; i++) {
				
				if (i == 0)
					statusCriteria = query().where("status").is(statusArray[i]);
				else
					statusCriteria = statusCriteria.or("status").is(statusArray[i]);
			}
			
			if (statusCriteria != null)
				criteria = criteria.and(statusCriteria);
		}
		
		if (keyword != null && keyword.length() > 0)
			criteria = criteria.and(query().where("sn").like(keyword + "*").or("email").like(keyword + "*"));
		
		return ldapTemplate.search(criteria, new UserContextMapper());
	}
	
	@Override
	public List<User> getUsers(String... statusArray) {
		
		ContainerCriteria criteria = query().where("objectclass").is("csUser");
		
		if (statusArray != null) {
			
			ContainerCriteria statusCriteria = null;
			
			for (int i = 0; i < statusArray.length; i++) {
				
				if (i == 0)
					statusCriteria = query().where("status").is(statusArray[i]);
				else
					statusCriteria = statusCriteria.or("status").is(statusArray[i]);
			}
			
			if (statusCriteria != null)
				criteria = criteria.and(statusCriteria);
		}
		
		return ldapTemplate.search(criteria,
				new UserContextMapper());
	}
	
	@Override
	public List<User> getRoleMembers(Role role) {
		
		Name dn = LdapNameBuilder.newInstance().add("ou", "Roles").add("cn", role.getName()).build();
		DirContextOperations context = ldapTemplate.lookupContext(dn);
		
		List<User> users = new ArrayList<User>();
		String[] members = context.getStringAttributes("member");
		
		if (members != null)
			for (String member : members) {
				
				try {
					String relativeDN = LdapUtils.getRelativeName(member, 
							ldapTemplate.lookupContext(LdapNameBuilder.newInstance().build()));
					
					users.add(ldapTemplate.lookup(relativeDN, new UserContextMapper()));
					
				} catch (NamingException e) {
					
					e.printStackTrace();
				}
			}
		
		return users;
	}

	public LdapTemplate getLdapTemplate() {

		return ldapTemplate;
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {

		this.ldapTemplate = ldapTemplate;
	}
	
	protected void mapToContext(User user, DirContextOperations context) {
		
		context.setAttributeValues("objectclass", new String[] { "top", "person", "csUser" });
		context.setAttributeValue("cn", user.getFirstname());
		context.setAttributeValue("sn", user.getLastname());
		
		if (user.getPassword() != null)
			context.setAttributeValue("userPassword", user.getPassword());
		
		if (user.getPassword() != null)
			context.setAttributeValue("section", user.getSection());
		
		context.setAttributeValue("email", user.getEmail());
		context.setAttributeValue("accountType", user.getAccountType());
		context.setAttributeValue("activationKey", user.getActivationKey());
		
		context.setAttributeValue("creationDate", dateFormat.format(user.getCreationDate()));
		
		if (user.getExpirationDate() != null)
			context.setAttributeValue("expirationDate", dateFormat.format(user.getExpirationDate()));
		
		context.setAttributeValue("status", user.getStatus());
		
		context.setAttributeValue("uniqueName", user.getEmail().replace("@", "-"));
	}
	
	private class UserContextMapper implements ContextMapper<User> {

		public User mapFromContext(Object ctx) {

			DirContextAdapter context = (DirContextAdapter) ctx;

			User user = new User();
			user.setFirstname(context.getStringAttribute("cn"));
			user.setLastname(context.getStringAttribute("sn"));
			user.setSection(context.getStringAttribute("section"));
			user.setEmail(context.getStringAttribute("email"));
			user.setAccountType(context.getStringAttribute("accountType"));
			user.setActivationKey(context.getStringAttribute("activationKey"));
			
			String creationDate = context.getStringAttribute("creationDate");
			
			try {
				user.setCreationDate(dateFormat.parse(creationDate));
				
			} catch (ParseException e) {

				logger.warn("Unable to parse creationDate (" + creationDate + ") for user " + context.getDn());
			}
			
			String expirationDate = context.getStringAttribute("expirationDate");
			
			if (expirationDate != null)
				try {
					user.setExpirationDate(dateFormat.parse(expirationDate));
					
				} catch (ParseException e) {

					logger.warn("Unable to parse expirationDate (" + expirationDate + ") for user " + context.getDn());
				}
			
			user.setStatus(context.getStringAttribute("status"));
			
			return user;
		}
	}
}
