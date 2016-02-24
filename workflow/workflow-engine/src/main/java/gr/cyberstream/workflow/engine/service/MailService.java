package gr.cyberstream.workflow.engine.service;

import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import gr.cyberstream.workflow.engine.config.SettingsStatus;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.persistence.Processes;

@Service
public class MailService {

	final static Logger logger = LoggerFactory.getLogger(MailService.class);
	
	@Autowired
	Processes processRepository;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private SettingsStatus settingsStatus;
	
	private final String datePattern = "d/M/yyyy";
	private String from;
	private String workspaceURL;
	private String managerURL;
	
	private boolean sendDueTask;
	private boolean sendTaskExpired;
	
	public MailService() {
		
		PropertyResourceBundle properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");
		
		from = properties.getString("mail.from");
		workspaceURL = properties.getString("workspaceURL");
		managerURL = properties.getString("managerURL");
		
		sendDueTask = properties.getString("mail.sendDueTask").equals("true");
		sendTaskExpired = properties.getString("mail.sendTaskExpired").equals("true");
	}
	
	public void sendTaskAssignedMail(String recipient, String taskId, String taskName, Date dueDate) {
		
		WorkflowSettings settings = getSettings();
		
		if (!settings.isAssignmentNotification())
			return;
		
		String taskAssignedSubject = "New task";
		String taskAssignedContent = "You were assigned a new";
		
		if (taskName != null && !taskName.isEmpty()) {
			taskAssignedSubject += " '" + taskName + "'";
			taskAssignedContent += " '" + taskName + "'";
		}
		
		taskAssignedContent += " task";
		
		if (dueDate != null) {
			
			taskAssignedContent += ", due on " + DateFormatUtils.format(dueDate, datePattern);
			
		}
		
		taskAssignedContent += ".";
		
		taskAssignedContent += " <a href=\"" + workspaceURL + "/#/task/" + taskId + "\">See Task</a>";
		
		try {
			
			sendMail(recipient, taskAssignedSubject, taskAssignedContent);
			
		} catch (MessagingException e) {
			
			logger.warn("Unable to send task assignment email to " + recipient);
		}
	}
	
	public void sendDueTaskMail(String recipient, String taskId, String taskName, Date dueDate, boolean unAssigned) {
		
		if (!sendDueTask)
			return;
		
		logger.info("Sending Due Task Email to " + recipient);
		String dueTaskSubject = "Due task";
		String dueTaskContent = (unAssigned ? "Unassigned " : "") + "Task";
		
		if (taskName != null && !taskName.isEmpty()) {
			dueTaskSubject += " '" + taskName + "'";
			dueTaskContent += " '" + taskName + "'";
		}
		
		if (dueDate != null) {
			
			dueTaskContent += ", is due on " + DateFormatUtils.format(dueDate, datePattern);
			
		}
		
		dueTaskContent += ".";
		
		dueTaskContent += " <a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId + "\">" 
				+ (unAssigned ? "Assign" : "See") + " Task</a>";
		
		try {
			
			sendMail(recipient, dueTaskSubject, dueTaskContent);
			
		} catch (MessagingException e) {
			
			logger.warn("Unable to send due task email to " + recipient);
		}
	}
	
	public void sendTaskExpiredMail(String recipient, String taskId, String taskName, Date dueDate, boolean unAssigned) {
		
		if (!sendTaskExpired)
			return;
		
		logger.info("Sending Expired Task Email to " + recipient);
		String taskExpiredSubject = "Expired Task";
		String taskExpiredContent = (unAssigned ? "Unassigned " : "") + "Task";
		
		if (taskName != null && !taskName.isEmpty()) {
			taskExpiredSubject += " '" + taskName + "'";
			taskExpiredContent += " '" + taskName + "',";
		}
		
		taskExpiredContent += " has expired";
		
		if (dueDate != null) {
			
			taskExpiredContent += " since " + DateFormatUtils.format(dueDate, datePattern);
			
		}
		
		taskExpiredContent += ".";
		
		taskExpiredContent += " <a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId + "\">" 
				+ (unAssigned ? "Assign" : "See") + " Task</a>";
		
		try {
			
			sendMail(recipient, taskExpiredSubject, taskExpiredContent);
			
		} catch (MessagingException e) {
			
			logger.warn("Unable to send task expired email to " + recipient);
		}
	}
		
	public void sendBpmnErrorEmail(String supervisor, WorkflowDefinition workflow, String taskName){
		
		String subject = "BPMN error";
		
		String content = "<p>No candidates have been found for the Task with name '" + taskName 
				+ "' since there is an error at the BPMN file of the process '" + workflow.getName() 
				+ "' with ProcessDefinitionId '" + workflow.getKey() + "'. </p>";
		
		content += "<p>For further actions visit the <a href=\"" + managerURL + "/#/process/" + workflow.getId() + 
				" \"> process definition manager</a> page.</p>";
		
		try {
			
			sendMail(supervisor, subject, content);
			
		} catch (MessagingException e) {
			
			logger.warn("Unable to send bpmn error email to " + supervisor);
			
		}
	}
	
	private void sendMail(String to, String subject, String content) throws MessagingException {
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		
		helper.setTo(to);
		helper.setFrom(from);
		helper.setSubject(subject);
		
		helper.setText(content, true);
					
		this.mailSender.send(message);
	}
	
	private WorkflowSettings getSettings(){
		
		WorkflowSettings settings = settingsStatus.getWorkflowSettings();
		
		if(settings == null){
			settings = processRepository.getSettings();
			settingsStatus.setWorkflowSettings(settings);
		}
		
		return settings;
	}
}
