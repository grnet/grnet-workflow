package gr.cyberstream.workflow.engine.service;

import gr.cyberstream.workflow.engine.config.SettingsStatus;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.persistence.Processes;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

@Service
public class MailService {

	private static final Logger logger = LoggerFactory.getLogger(MailService.class);

	@Autowired
	Processes processRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private SettingsStatus settingsStatus;

	private final String datePattern = "dd/MM/yyyy HH:mm:ss";
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

	public void sendTaskAssignedMail(String recipient, WfTask task) {

		WorkflowSettings settings = getSettings();
		String taskName = task.getName();
		Date dueDate = task.getDueDate();
		String taskId = task.getId();

		if (!settings.isAssignmentNotification())
			return;

		String taskAssignedSubject = "Νέα εργασία";
		String taskAssignedContent = "Διαδικασία: '" + task.getDefinitionName() + "'\n";
		taskAssignedContent += "Εκτέλεση: '" + task.getProcessInstance().getTitle() + "'\n";

		if (taskName != null && !taskName.isEmpty()) {
			taskAssignedSubject += " '" + taskName + "'";
			taskAssignedContent += "Σας έχει ανατεθεί η εργασία '" + taskName + "'.";
		} else {
			taskAssignedContent += "Σας έχει ανατεθεί μία εργασία.";
		}

		if (dueDate != null) {
			taskAssignedContent += "\nΗ χρονική περιόδος για την εκτέλεση της εργασίας είναι μέχρι τις "
					+ DateFormatUtils.format(dueDate, datePattern);
			taskAssignedContent += ".";
		}

		taskAssignedContent += "\n<a href=\"" + workspaceURL + "/#/task/" + taskId
				+ "\">Επιλέξτε για να δείτε την εργασία</a>";

		try {
			sendMail(recipient, taskAssignedSubject, taskAssignedContent, "");
		} catch (MessagingException e) {
			logger.warn("Unable to send task assignment email to " + recipient);
		}
	}

	public void sendDueTaskMail(String recipient, Task task, boolean unAssigned) {
		String taskName = task.getName();
		Date dueDate = task.getDueDate();
		String taskId = task.getId();

		if (!sendDueTask)
			return;

		logger.info("Sending Due Task Email to " + recipient);
		String dueTaskSubject = "Ημερομηνία εκτέλεσης εργασίας";
		String dueTaskContent = "";
		try {
			dueTaskContent = "Διαδικασία: " + processRepository.getProcessByDefinitionId(task.getProcessDefinitionId()).getName() + "\n";
			dueTaskContent += "Εκτέλεση: " + processRepository.getInstanceById(task.getProcessInstanceId()).getTitle() + "\n";
		} catch (Exception exception) {
			logger.warn("Unable to get definition or execution of task.");
		}

		if (taskName != null && !taskName.isEmpty()) {
			dueTaskSubject += " '" + taskName + "'";
			dueTaskContent += "Η" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία") + " '" + taskName + "'";
		} else {
			dueTaskContent += "Μία" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία");
		}

		if (dueDate != null) {
			dueTaskContent += " έχει ημερομηνία εκτέλεσης μέχρι τις " + DateFormatUtils.format(dueDate, datePattern);
		} else {
			dueTaskContent += " πρόκειται να λήξει";
		}

		dueTaskContent += ".\n";

		dueTaskContent += " <a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId + "\">"
				+ "Επιλέξτε για να δείτε την εργασία</a>";

		try {
			sendMail(recipient, dueTaskSubject, dueTaskContent, "");
		} catch (MessagingException e) {

			logger.warn("Unable to send due task email to " + recipient);
		}
	}

	public void sendTaskExpiredMail(String recipient, Task task, boolean unAssigned) {
		String taskName = task.getName();
		Date dueDate = task.getDueDate();
		String taskId = task.getId();

		if (!sendTaskExpired)
			return;

		logger.info("Sending Expired Task Email to " + recipient);
		String taskExpiredSubject = "Η χρονική περίοδος εκτέλεσης";

		String taskExpiredContent = "";
		try {
			taskExpiredContent = "Διαδικασία: " + processRepository.getProcessByDefinitionId(task.getProcessDefinitionId()).getName() + "\n";
			taskExpiredContent += "Εκτέλεση: " + processRepository.getInstanceById(task.getProcessInstanceId()).getTitle() + "\n";
		} catch (Exception exception) {
			logger.warn("Unable to get definition or execution of task.");
		}

		if (taskName != null && !taskName.isEmpty()) {
			taskExpiredSubject += " της εργασίας '" + taskName + "' έχει λήξει";
			taskExpiredContent += "Η" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία") + " '" + taskName + "' έχει λήξει.\n";
		} else {
			taskExpiredSubject += " μιας εργασίας έχει λήξει";
			taskExpiredContent += "Μία" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία έχει λήξει.\n");
		}

		if (dueDate != null) {
			taskExpiredContent += "Ημερομηνία λήξης: " + DateFormatUtils.format(dueDate, datePattern) + "\n";
		}

		taskExpiredContent += "<a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId
				+ "\">" + "Επιλέξτε για να δείτε την εργασία</a>";

		try {
			sendMail(recipient, taskExpiredSubject, taskExpiredContent, "");
		} catch (MessagingException e) {
			logger.warn("Unable to send task expired email to " + recipient);
		}
	}

	public void sendDefinitionErrorMail(String administrator, WorkflowDefinition workflow, Task task,
										WorkflowInstance instance) {

		String subject = "Σφάλμα ορισμού διαδικασίας";

		String content = "<p><b>Διαδικασία:</b> " + workflow.getName() + "</p>" +
				"<p><b>Εκτέλεση:</b> " + instance.getTitle() + "</p>" +
				"<p>Δεν βρέθηκαν υποψήφιοι για την εκτέλεση της εργασίας με όνομα '" + task.getName()
				+ "' και κωδικό '" + task.getId() + "'.</p>";

		content += "<p>Παρακαλώ επικοινωνήστε με το συντονιστή της εκτέλεσης " + instance.getSupervisor() + ".</p>";

		try {
			sendMail(instance.getSupervisor(), subject, content, administrator);
		} catch (MessagingException e) {
			logger.warn("Unable to send definition error email to " + instance.getSupervisor());
		}
	}

	public void sendNoCandidatesErrorEmail(String administrator, Task task, String username) throws MessagingException {
		WorkflowDefinition workflowDef = processRepository.getProcessByDefinitionId(task.getProcessDefinitionId());
		WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());

		String subject = "Σφάλμα ανάθεσης εργασίας";

		String content = "<p><b>Διαδικασία:</b> " + workflowDef.getName() + "</p>" +
				"<p><b>Εκτέλεση:</b> " + instance.getTitle() + "</p>" +
				"<p>Δεν βρέθηκαν υποψήφιοι για την εκτέλεση της εργασίας με όνομα '" + task.getName()
				+ "' και κωδικό '" + task.getId() + "'.</p>";

		content += "<p>Παρακαλώ αναθέστε τον κατάλληλο ρόλο στον χρήστη '" + username + "'</p>";

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
