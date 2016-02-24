package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Registry implements Serializable {

	private static final long serialVersionUID = 1847022657860380926L;
	
	@Id
	private String id;
	private String template;
	private int next;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public int getNext() {
		return next;
	}
	public void setNext(int next) {
		this.next = next;
	}

}
