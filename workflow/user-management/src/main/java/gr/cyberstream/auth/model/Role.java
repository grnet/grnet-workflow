package gr.cyberstream.auth.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class Role {

	private int id;
	private String name;
	private String description;
	
	private List<User> members;
	
	public static String ROLE_ADMIN = "Administrators";
	public static String ROLE_USER = "SimpleUsers";
	
	public Role() {
	}

	@Id @GeneratedValue(strategy=GenerationType.AUTO) 
	public int getId() {
	
		return id;
	}

	public void setId(int id) {
	
		this.id = id;
	}

	public String getName() {
	
		return name;
	}

	public void setName(String name) {
	
		this.name = name;
	}

	public String getDescription() {
	
		return description;
	}

	public void setDescription(String description) {
	
		this.description = description;
	}

	@ManyToMany 
	@JoinTable(
			name="User_Role",
			joinColumns = { 
					@JoinColumn(name = "role_id") }, 
			inverseJoinColumns = { 
					@JoinColumn(name = "user_id") })
	public List<User> getMembers() {
	
		return members;
	}

	public void setMembers(List<User> members) {
	
		this.members = members;
	}

	@Override
	public boolean equals(Object obj) {

		return (obj != null && obj instanceof Role && this.name.equals(((Role) obj).getName()));
	}

	@Override
	public int hashCode() {

		return this.getName().hashCode();
	}

	@Override
	public String toString() {

		return "ROLE_" + this.getName();
	}
}
