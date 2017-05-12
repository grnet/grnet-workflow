package gr.cyberstream.workflow.engine.service.impl;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.activiti.engine.task.Task;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import gr.cyberstream.workflow.engine.config.SettingsStatus;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.MailService;

@Service
public class MailServiceImpl implements MailService {

	private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

	@Inject
	private Environment environment;

	@Autowired
	private Processes processRepository;

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

	@PostConstruct
	public void initializeService() {
		from = environment.getProperty("mail.from");
		workspaceURL = environment.getProperty("workspaceURL");
		managerURL = environment.getProperty("managerURL");
		sendDueTask = environment.getProperty("mail.sendDueTask").equals("true");
		sendTaskExpired = environment.getProperty("mail.sendTaskExpired").equals("true");
	}

	@Override
	public void sendTaskAssignedMail(String recipient, String taskId, String taskName, Date dueDate) {

		WorkflowSettings settings = getSettings();

		if (!settings.isAssignmentNotification())
			return;

		String taskAssignedSubject = "Νέα εργασία";
		String taskAssignedContent = "";

		if (taskName != null && !taskName.isEmpty()) {
			taskAssignedSubject += " '" + taskName + "'";
			taskAssignedContent += "<p>Σας έχει ανατεθεί η εργασία '" + taskName + "'.</p>";

		} else
			taskAssignedContent += "<p>Σας έχει ανατεθεί μία εργασία.</p>";

		if (dueDate != null)
			taskAssignedContent += "<p>Η χρονική περιόδος για την εκτέλεση της εργασίας είναι μέχρι τις " + DateFormatUtils.format(dueDate, datePattern) + ".</p>";

		taskAssignedContent += "<p><a href=\"" + workspaceURL + "/#/task/" + taskId + "\">Επιλέξτε για να δείτε την εργασία</a></p>";

		try {
			sendMail(recipient, taskAssignedSubject, taskAssignedContent);
		} catch (MessagingException e) {
			logger.warn("Unable to send task assignment email to " + recipient);
		}
	}

	@Override
	public void sendDueTaskMail(String recipient, String taskId, String taskName, Date dueDate, boolean unAssigned) {

		if (!sendDueTask)
			return;

		logger.info("Sending Due Task Email to " + recipient);

		String dueTaskSubject = "Ημερομηνία εκτέλεσης εργασίας";
		String dueTaskContent = "";

		if (taskName != null && !taskName.isEmpty()) {
			dueTaskSubject += " '" + taskName + "'";
			dueTaskContent += "<p>Η" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία") + " '" + taskName + "'";
		} else {
			dueTaskContent += "<p>Μία" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία");
		}

		if (dueDate != null)
			dueTaskContent += " έχει ημερομηνία εκτέλεσης μέχρι τις " + DateFormatUtils.format(dueDate, datePattern);
		else
			dueTaskContent += " πρόκειται να λήξει";

		dueTaskContent += ".</p>";

		dueTaskContent += "<p><a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId + "\">" + "Επιλέξτε για να δείτε την εργασία</a></p>";

		try {
			sendMail(recipient, dueTaskSubject, dueTaskContent);

		} catch (MessagingException e) {
			logger.warn("Unable to send due task email to " + recipient);
		}
	}

	@Override
	public void sendTaskExpiredMail(String recipient, String taskId, String taskName, Date dueDate,
			boolean unAssigned) {

		if (!sendTaskExpired)
			return;

		logger.info("Sending Expired Task Email to " + recipient);

		String taskExpiredSubject = "Η χρονική περίοδος εκτέλεσης";
		String taskExpiredContent = "";

		if (taskName != null && !taskName.isEmpty()) {
			taskExpiredSubject += " της εργασίας '" + taskName + "' έχει λήξει";
			taskExpiredContent += "<p>Η" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία") + " '" + taskName + "' έχει λήξει.</p>";
		} else {
			taskExpiredSubject += " μιας εργασίας έχει λήξει";
			taskExpiredContent += "<p>Μία" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία έχει λήξει.</p>");
		}

		if (dueDate != null)
			taskExpiredContent += "<p><b>Ημερομηνία λήξης:</b> " + DateFormatUtils.format(dueDate, datePattern) + "</p>";

		taskExpiredContent += "<p><a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId + "\">" + "Επιλέξτε για να δείτε την εργασία</a></p>";

		try {
			sendMail(recipient, taskExpiredSubject, taskExpiredContent);

		} catch (MessagingException e) {
			logger.warn("Unable to send task expired email to " + recipient);
		}
	}

	@Override
	public void sendBpmnErrorEmail(String supervisor, WorkflowDefinition workflow, String taskName) {

		String subject = "Σφάλμα ορισμού διαδικασίας";

		String content = "<p>Δεν βρέθηκαν υποψήφιοι για την εκτέλεση της εργασίας με όνομα '" + taskName
				+ "' δεδομένου ότι υπάρχει σφάλμα στο αρχείο BPMN της διαδικασίας με όνομα '" + workflow.getName()
				+ "' και κωδικό '" + workflow.getKey() + "'.</p>";

		content += "<p>Για περισσότερες επιλογές επισκεφθείτε την σελίδα <a href=\"" + managerURL + "/#/process/"
				+ workflow.getId() + " \"> διαχείρισης ορισμού διαδικασίας </a> page.</p>";

		try {
			sendMail(supervisor, subject, content);

		} catch (MessagingException e) {
			logger.warn("Unable to send bpmn error email to " + supervisor);
		}
	}

	@Override
	public void sendTaskDueDateNotification(Task task, String content) throws InternalException {

		String subject = "Ολοκλήρωση εργασίας";

		try {
			content += "<p>Για περισσότερες επιλογές επισκεφθείτε την σελίδα <a href=\"" + workspaceURL + "/#/task/"
					+ task.getId() + " \"> εκτέλεσης εργασίας</a></p>";

			sendMail(task.getAssignee(), subject, content);

		} catch (MessagingException e) {
			logger.warn("Unable to send task due date notification to:" + task.getAssignee());
			throw new InternalException("Unable to send notification " + e.getMessage());
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

	private WorkflowSettings getSettings() {
		WorkflowSettings settings = settingsStatus.getWorkflowSettings();

		if (settings == null) {
			settings = processRepository.getSettings();
			settingsStatus.setWorkflowSettings(settings);
		}

		return settings;
	}

}
