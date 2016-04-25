package gr.cyberstream.workflow.engine.model.api;

import gr.cyberstream.workflow.engine.model.Registry;

public class WfRegistry {

	private String id;
	private String template;
	private int next;
	
	public WfRegistry() {
	}
	
	public WfRegistry(Registry registry) {
		
		id = registry.getId();
		template = registry.getTemplate();
		next = registry.getNext();
	}
	
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
