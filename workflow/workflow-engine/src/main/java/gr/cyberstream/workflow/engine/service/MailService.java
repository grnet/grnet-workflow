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

	private static final Logger logger = LoggerFactory.getLogger(MailService.class);

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

		String taskAssignedSubject = "Νέα εργασία";
		String taskAssignedContent = "Σας έχει ανατεθεί ";

		if (taskName != null && !taskName.isEmpty()) {
			taskAssignedSubject += " '" + taskName + "'";
			taskAssignedContent += " '" + taskName + "'";
		}

		taskAssignedContent += " εργασία";

		if (dueDate != null) {

			taskAssignedContent += ", η χρονική περιόδος για την εκτέλεση της εργασίας είναι μέχρι τις "
					+ DateFormatUtils.format(dueDate, datePattern);

		}

		taskAssignedContent += ".";

		taskAssignedContent += " <a href=\"" + workspaceURL + "/#/task/" + taskId
				+ "\">Επιλέξτε για να δείτε την εργασία</a>";

		try {

			sendMail(recipient, taskAssignedSubject, taskAssignedContent, "");

		} catch (MessagingException e) {

			logger.warn("Unable to send task assignment email to " + recipient);
		}
	}

	public void sendDueTaskMail(String recipient, String taskId, String taskName, Date dueDate, boolean unAssigned) {

		if (!sendDueTask)
			return;

		logger.info("Sending Due Task Email to " + recipient);
		String dueTaskSubject = "Ημερομηνία εκτέλεσης εργασίας";
		String dueTaskContent = (unAssigned ? "Μη ανατεθειμένη " : "") + "εργασία";

		if (taskName != null && !taskName.isEmpty()) {
			dueTaskSubject += " '" + taskName + "'";
			dueTaskContent += " '" + taskName + "'";
		}

		if (dueDate != null) {

			dueTaskContent += ", έχει ημερομηνία εκτέλεσης μέχρι τις " + DateFormatUtils.format(dueDate, datePattern);

		}

		dueTaskContent += ".";

		dueTaskContent += " <a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId + "\">"
				+ (unAssigned ? "Ανατεθειμένη" : "Επιλέξτε για να δείτε") + " Task</a>";

		try {

			sendMail(recipient, dueTaskSubject, dueTaskContent, "");

		} catch (MessagingException e) {

			logger.warn("Unable to send due task email to " + recipient);
		}
	}

	public void sendTaskExpiredMail(String recipient, String taskId, String taskName, Date dueDate,
			boolean unAssigned) {

		if (!sendTaskExpired)
			return;

		logger.info("Sending Expired Task Email to " + recipient);
		String taskExpiredSubject = "Χρονική περίοδος εκτέλεσης εργασίας έληξε";
		String taskExpiredContent = (unAssigned ? "Μη ανατεθειμένη " : "") + "εργασία";

		if (taskName != null && !taskName.isEmpty()) {
			taskExpiredSubject += " '" + taskName + "'";
			taskExpiredContent += " '" + taskName + "',";
		}

		taskExpiredContent += " 'έχει λήξει";

		if (dueDate != null) {

			taskExpiredContent += " από τις " + DateFormatUtils.format(dueDate, datePattern);

		}

		taskExpiredContent += ".";

		taskExpiredContent += " <a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId
				+ "\">" + (unAssigned ? "Ανατεθειμένη" : "Επιλέξτε για να δείτε") + " την εργασία</a>";

		try {

			sendMail(recipient, taskExpiredSubject, taskExpiredContent, "");

		} catch (MessagingException e) {

			logger.warn("Unable to send task expired email to " + recipient);
		}
	}

	public void sendBpmnErrorEmail(String administrator, WorkflowDefinition workflow, String taskName,
			String supervisor) {

		String subject = "Σφάλμα ορισμού διαδικασίας";

		String content = "<p>Δεν βρέθηκαν υποψήφιοι για την εκτέλεση της εργασίας με όνομα '" + taskName
				+ "' δεδομένου ότι υπάρχει σφάλμα στο αρχείο BPMN της διαδικασίας με όνομα '" + workflow.getName()
				+ "' και κωδικό '" + workflow.getKey() + "'.</p>";

		content += "<p>Για περισσότερες επιλογές επισκεφθείτε την σελίδα <a href=\"" + managerURL + "/#/process/"
				+ workflow.getId() + " \"> διαχείρισης ορισμού διαδικασίας </a> page.</p>";

		try {

			sendMail(supervisor, subject, content, administrator);

		} catch (MessagingException e) {

			logger.warn("Unable to send bpmn error email to " + supervisor);

		}
	}

	public void sendNoCandidatesEmail(String administrator, String taskName, String taskId) throws MessagingException {
		String subject = "Σφάλμα ανάθεσης εργασίας";

		String content = "<p>Δεν βρέθηκαν υποψήφιοι για την ανάθεση της εργασίας με όνομα '" + taskName
				+ "' και κωδικό '" + taskId + "'.</p>";

		sendMail(administrator, subject, content, "");
	}

	private void sendMail(String to, String subject, String content, String cc) throws MessagingException {

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		helper.setTo(to);
		helper.setFrom(from);

		if (!cc.isEmpty() || !cc.equals(""))
			helper.setCc(cc);

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
