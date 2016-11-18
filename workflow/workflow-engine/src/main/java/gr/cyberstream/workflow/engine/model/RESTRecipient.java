package gr.cyberstream.workflow.engine.model;

public class RESTRecipient {

	private String email;
	private String name;
	private String status;

	public RESTRecipient() {
		
	}

	public RESTRecipient(String email) {
		this.email = email;
	}

	public RESTRecipient(String email, String name, String status) {
		this.email = email;
		this.name = name;
		this.status = status;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
