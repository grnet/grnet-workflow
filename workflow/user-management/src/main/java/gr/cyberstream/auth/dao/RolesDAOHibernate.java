package gr.cyberstream.auth.dao;

import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;

import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

public class RolesDAOHibernate implements RolesDAO {

	private SessionFactory sessionFactory;
	
	@Override
	@Transactional
	public void saveRole(Role role) {

		Session session = sessionFactory.getCurrentSession();

		session.save(role);
	}
	
	@Override
	@Transactional
	public void removeRole(Role role) {

		Session session = sessionFactory.getCurrentSession();
		
		role = (Role) session.get(Role.class, role.getId());
		role.getMembers().clear();
		
		session.update(role);

		session.delete(role);
	}

	@Override
	@Transactional
	public Role getRole(String name) {

		Session session = sessionFactory.getCurrentSession();
		
		Query query = session.createQuery("from Role where name = :name");
		query.setParameter("name", name);
		
		Role role = null;
		
		try {
			role = (Role) query.uniqueResult();
			
		} catch (NonUniqueResultException e) {
			e.printStackTrace();
		}
		
		return role;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public List<Role> getRoles() {

		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from Role");

		return query.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public List<Role> getUserRoles(User user) {
		
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from Role r where :user in r.members");
		query.setParameter("user", user);
		
		return query.list();
	}
	
	@Override
	@Transactional
	public void addUserRole(User user, Role role) {
		
		Session session = sessionFactory.getCurrentSession();
		
		role = (Role) session.get(Role.class, role.getId());
		role.getMembers().add(user);
		
		session.update(role);
	}
	
	@Override
	@Transactional
	public void addUserRole(User user, String roleName) {
		
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from Role where name = :name");
		query.setParameter("name", roleName);
		
		Role role = (Role) query.uniqueResult();
		
		role.getMembers().add(user);
		
		session.update(role);
	}
	
	@Override
	@Transactional
	public void removeUserRole(User user, Role role) {
		
		Session session = sessionFactory.getCurrentSession();
		
		role = (Role) session.get(Role.class, role.getId());
		user = (User) session.get(User.class, user.getId());
		role.getMembers().remove(user);
		
		session.update(role);
	}
	
	@Override
	@Transactional
	public void removeRoles(User user) {
		
		Session session = sessionFactory.getCurrentSession();
		
		user = (User) session.get(User.class, user.getId());
		user.getRoles().clear();
		
		session.update(user);
	}
	
	public SessionFactory getSessionFactory() {
	
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
	
		this.sessionFactory = sessionFactory;
	}

}
