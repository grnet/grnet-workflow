package gr.cyberstream.auth.dao;

import java.util.List;

import gr.cyberstream.auth.model.Role;
import gr.cyberstream.auth.model.User;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

public class UsersDAOHibernate implements UsersDAO {

	private SessionFactory sessionFactory;

	@Transactional
	public boolean comparePassword(User user, String password) {
		
		Session session = sessionFactory.getCurrentSession();
		
		Query query = session.createQuery("from User where email = :email and password := ");
		query.setParameter("email", user.getPassword());
		query.setParameter("password", password);
		
		return query.uniqueResult() != null;
	}
	
	@Transactional
	public void saveUser(User user) {

		Session session = sessionFactory.getCurrentSession();

		session.save(user);
	}
	
	@Transactional
	public void updateUser(User user) {

		Session session = sessionFactory.getCurrentSession();

		session.update(user);
	}
	
	@Transactional
	public void removeUser(User user) {
		
		Session session = sessionFactory.getCurrentSession();

		session.delete(user);
	}

	@Transactional
	public User getUser(String email) {

		Session session = sessionFactory.getCurrentSession();
		
		Query query = session.createQuery("from User where email = :email");
		query.setParameter("email", email);
		
		User user = null;
		
		try {
			user = (User) query.uniqueResult();
			
		} catch (NonUniqueResultException e) {
			e.printStackTrace();
		}
		
		return user; 
	}

	@Transactional
	public User getUserByKey(String key) {

		Session session = sessionFactory.getCurrentSession();
		
		Query query = session.createQuery("from User where activationKey = :key");
		query.setParameter("key", key);

		return (User) query.uniqueResult();
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	public List<User> searchUsers(String keyword, String... statusArray) {
		
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from User where status in (:status) and (lastname like :keyword or email like :keyword) order by lastname");
		query.setParameterList("status", statusArray);
		query.setParameter("keyword", keyword + "%");
		
		return query.list();
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	public List<User> getUsers(String... statusArray) {
		
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from User where status in (:status) order by lastname desc");
		query.setParameterList("status", statusArray);

		return query.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<User> getRoleMembers(Role role) {
		
		Session session = sessionFactory.getCurrentSession();
		
		Query query = session.createQuery("select u from Role r inner join r.members u where r = :role");
		query.setParameter("role", role);

		return query.list();
	}

	public SessionFactory getSessionFactory() {

		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {

		this.sessionFactory = sessionFactory;
	}

}
