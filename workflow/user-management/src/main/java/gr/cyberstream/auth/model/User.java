package gr.cyberstream.auth.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class User {

	private int id;
	private String email;
	private String password;
	private String firstname;
	private String lastname;
	private String section;
	private String status;
	private String activationKey;
	private String accountType;
	private String refreshToken;
	private Date creationDate;
	private Date expirationDate;
	
	private List<Role> roles;

	public static String STATUS_VERIFIED = "verified";
	public static String STATUS_PENDING = "pending";
	public static String STATUS_MODERATED = "moderated";
	public static String STATUS_BANNED = "banned";
	public static String STATUS_DISABLED = "disabled";

	public static String GOOGLE_ACCOUNT = "google";
	public static String FACEBOOK_ACCOUNT = "facebook";
	public static String SIMPLE_ACCOUNT = "default";

	public User() {

		status = STATUS_PENDING;
		accountType = SIMPLE_ACCOUNT;
	}
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO) 
	public int getId() {

		return id;
	}

	public void setId(int id) {

		this.id = id;
	}

	public String getEmail() {

		return email;
	}

	public void setEmail(String email) {

		this.email = email;
	}

	public String getPassword() {

		return password;
	}

	public void setPassword(String password) {

		this.password = password;
	}

	public String getFirstname() {

		return firstname;
	}

	public void setFirstname(String firstname) {

		this.firstname = firstname;
	}

	public String getLastname() {

		return lastname;
	}

	public void setLastname(String lastname) {

		this.lastname = lastname;
	}

	public String getStatus() {

		return status;
	}

	public void setStatus(String status) {

		this.status = status;
	}

	public String getActivationKey() {

		return activationKey;
	}

	public void setActivationKey(String activationKey) {

		this.activationKey = activationKey;
	}

	public Date getCreationDate() {

		return creationDate;
	}

	public void setCreationDate(Date creationDate) {

		this.creationDate = creationDate;
	}

	public Date getExpirationDate() {

		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {

		this.expirationDate = expirationDate;
	}

	public String getAccountType() {

		return accountType;
	}

	public void setAccountType(String accountType) {

		this.accountType = accountType;
	}

	public String getRefreshToken() {

		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {

		this.refreshToken = refreshToken;
	}
	@ManyToMany 
	@JoinTable(
			name="User_Role",
			joinColumns = { 
					@JoinColumn(name = "user_id") }, 
			inverseJoinColumns = { 
					@JoinColumn(name = "role_id") })
	public List<Role> getRoles() {
	
		return roles;
	}

	public void setRoles(List<Role> roles) {
	
		this.roles = roles;
	}

	@Override
	public boolean equals(Object obj) {

		return (obj != null && obj instanceof User && this.email.equals(((User) obj).getEmail()));
	}

	@Override
	public int hashCode() {

		return this.getEmail().hashCode();
	}

	@Override
	public String toString() {

		return this.email + "/" + this.lastname + "-" + this.firstname;
	}

	public String getSection() {
	
		return section;
	}

	public void setSection(String section) {
	
		this.section = section;
	}
}
