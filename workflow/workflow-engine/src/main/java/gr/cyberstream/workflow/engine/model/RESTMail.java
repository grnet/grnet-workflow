package gr.cyberstream.workflow.engine.model;

import java.util.List;
import java.util.Map;

public class RESTMail {

	private String app;
	
	private String subject;
	private String from;
	private List<RESTRecipient> to;
	
	private String jobID;
	
	private Map<String, Object> parameters;
	
	public RESTMail() {
	}

	public RESTMail(String app, String jobID, String subject, String from, List<RESTRecipient> to) {
		
		this.app = app;
		this.jobID = jobID;
		this.subject = subject;
		this.from = from;
		this.to = to;
	}
	
	public RESTMail(String jobID, String subject, List<RESTRecipient> to) {
		
		this.jobID = jobID;
		this.subject = subject;
		this.to = to;
	}
	
	public String getApp() {
	
		return app;
	}

	public void setApp(String app) {
	
		this.app = app;
	}

	public String getSubject() {
	
		return subject;
	}

	public void setSubject(String subject) {
	
		this.subject = subject;
	}

	public Map<String, Object> getParameters() {
	
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
	
		this.parameters = parameters;
	}

	public String getFrom() {
	
		return from;
	}

	public void setFrom(String from) {
	
		this.from = from;
	}

	public List<RESTRecipient> getTo() {
	
		return to;
	}

	public void setTo(List<RESTRecipient> to) {
	
		this.to = to;
	}

	public String getJobID() {
	
		return jobID;
	}

	public void setJobID(String jobID) {
	
		this.jobID = jobID;
	}
}
