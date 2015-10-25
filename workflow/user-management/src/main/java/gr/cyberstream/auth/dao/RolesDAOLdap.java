package gr.cyberstream.auth.dao;

import static org.springframework.ldap.query.LdapQueryBuilder.query;
import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;

import java.util.List;
import java.util.ResourceBundle;

import javax.naming.InvalidNameException;
import javax.naming.Name;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

public class RolesDAOLdap implements RolesDAO {

	private LdapTemplate ldapTemplate;
	private Logger logger;
	
	private final Name BASE_DN;
	
	public RolesDAOLdap() {

		logger = Logger.getLogger("auth");
		
		BASE_DN = LdapNameBuilder.newInstance().add(ResourceBundle.getBundle("auth").getString("ldapBase")).build();
	}

	@Override
	public void saveRole(Role role) {

		Name dn = LdapNameBuilder.newInstance().add("ou", "Roles").add("cn", role.getName()).build();
		DirContextOperations context = new DirContextAdapter(dn);

		mapToContext(role, context);
		
		ldapTemplate.bind(context);
	}
	
	@Override
	public void removeRole(Role role) {
		
		Name roleDN = LdapNameBuilder.newInstance().add("ou", "Roles").add("cn", role.getName()).build();
		DirContextOperations roleContext = ldapTemplate.lookupContext(roleDN);
		
		String[] members = roleContext.getStringAttributes("member");
		
		if (members != null)
			for (String member : members) {
				
				Name memberDN = LdapNameBuilder.newInstance().add(member).build();
				DirContextOperations memberContext = ldapTemplate.lookupContext(memberDN);
				
				memberContext.removeAttributeValue("memberOf", roleDN);
				ldapTemplate.modifyAttributes(memberContext);
			}
		
		ldapTemplate.unbind(roleDN);
	}	
	
	@Override
	public void removeRoles(User user) {
		
		Name userDN = LdapNameBuilder.newInstance().add("ou", "Users").add("uid", user.getEmail()).build();
		DirContextOperations userContext = ldapTemplate.lookupContext(userDN);
		
		String[] roles = userContext.getStringAttributes("memberOf");
		
		if (roles != null)
			for (String role : roles) {
				
				Name roleDN = LdapNameBuilder.newInstance().add(role).build();
				DirContextOperations roleContext = ldapTemplate.lookupContext(roleDN);
				
				try {
					roleContext.removeAttributeValue("member", userDN.addAll(0, BASE_DN));
					ldapTemplate.modifyAttributes(roleContext);
					
				} catch (InvalidNameException e) {
					
					logger.warn("Unable to remove role member: " + userDN);
				}
				
				userContext.removeAttributeValue("memberOf", roleDN);
				ldapTemplate.modifyAttributes(userContext);
			}
	}

	@Override
	public Role getRole(String name) {

		Name dn = LdapNameBuilder.newInstance().add("ou", "Roles").add("cn", name).build();

		return ldapTemplate.lookup(dn, new RoleContextMapper());
	}

	@Override
	public List<Role> getRoles() {

		return ldapTemplate.search(
				query().where("objectclass").is("csRole"),
				new RoleContextMapper());
	}
	
	@Override
	public List<Role> getUserRoles(User user) {
		
		Name userDN = LdapNameBuilder.newInstance().add(BASE_DN).add("ou", "Users")
				.add("uid", user.getEmail()).build();
		
		return ldapTemplate.search(
				query().where("objectclass").is("csRole").and("member").is(userDN.toString()),
				new RoleContextMapper());
	}
	
	@Override
	public void addUserRole(User user, Role role) {
		
		Name roleDN = LdapNameBuilder.newInstance().add("ou", "Roles").add("cn", role.getName()).build();
		DirContextOperations roleContext = ldapTemplate.lookupContext(roleDN);
		
		Name userDN = LdapNameBuilder.newInstance().add("ou", "Users").add("uid", user.getEmail()).build();
		DirContextOperations userContext = ldapTemplate.lookupContext(userDN);
		
		String[] members = roleContext.getStringAttributes("member");
		
		try {
			
			if (members == null || members.length == 0)
				roleContext.setAttributeValue("member", userDN.addAll(0, BASE_DN));
			else
				roleContext.addAttributeValue("member", userDN.addAll(0, BASE_DN));
			
		} catch (InvalidNameException e) {

			logger.warn("Unable to add role member: " + userDN);
		}
		
		ldapTemplate.modifyAttributes(roleContext);
		
		String[] roles = userContext.getStringAttributes("memberOf");
		
		if (roles == null || roles.length == 0)
			userContext.setAttributeValue("memberOf", roleDN);
		else
			userContext.addAttributeValue("memberOf", roleDN);
		
		ldapTemplate.modifyAttributes(userContext);
	}
	
	@Override
	public void addUserRole(User user, String roleName) {
		
		Name roleDN = LdapNameBuilder.newInstance().add("ou", "Roles").add("cn", roleName).build();
		DirContextOperations roleContext = ldapTemplate.lookupContext(roleDN);
		
		Name userDN = LdapNameBuilder.newInstance().add("ou", "Users").add("uid", user.getEmail()).build();
		DirContextOperations userContext = ldapTemplate.lookupContext(userDN);
		
		String[] members = roleContext.getStringAttributes("member");
		
		try {
			if (members == null || members.length == 0)
				roleContext.setAttributeValue("member", userDN.addAll(0, BASE_DN));
			else
				roleContext.addAttributeValue("member", userDN.addAll(0, BASE_DN));
		} catch (InvalidNameException e) {
	
			logger.warn("Unable to add role member: " + userDN);
		}
	
		ldapTemplate.modifyAttributes(roleContext);
		
		String[] roles = userContext.getStringAttributes("memberOf");
		
		if (roles == null || roles.length == 0)
			userContext.setAttributeValue("memberOf", roleDN);
		else
			userContext.addAttributeValue("memberOf", roleDN);
		
		ldapTemplate.modifyAttributes(userContext);
	}
	
	@Override
	public void removeUserRole(User user, Role role) {
		
		Name roleDN = LdapNameBuilder.newInstance().add("ou", "Roles").add("cn", role.getName()).build();
		DirContextOperations roleContext = ldapTemplate.lookupContext(roleDN);
		
		Name userDN = LdapNameBuilder.newInstance().add("ou", "Users").add("uid", user.getEmail()).build();
		DirContextOperations userContext = ldapTemplate.lookupContext(userDN);
		
		try {
			roleContext.removeAttributeValue("member", userDN.addAll(0, BASE_DN));
			ldapTemplate.modifyAttributes(roleContext);
			
		} catch (InvalidNameException e) {

			logger.warn("Unable to remove role member: " + userDN);
		}
		
		userContext.removeAttributeValue("memberOf", roleDN);
		ldapTemplate.modifyAttributes(userContext);
	}
	
	public LdapTemplate getLdapTemplate() {
		
		return ldapTemplate;
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
	
		this.ldapTemplate = ldapTemplate;
	}

	protected void mapToContext(Role role, DirContextOperations context) {
		
		context.setAttributeValues("objectclass", new String[] { "top", "csRole" });
		context.setAttributeValue("cn", role.getName());
		context.setAttributeValue("description", role.getDescription());
	}

	private static class RoleContextMapper implements ContextMapper<Role> {

		public Role mapFromContext(Object ctx) {

			DirContextAdapter context = (DirContextAdapter) ctx;

			Role role = new Role();
			role.setName(context.getStringAttribute("cn"));
			role.setDescription(context.getStringAttribute("description"));
			
			return role;
		}
	}
}
