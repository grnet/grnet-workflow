package gr.cyberstream.workflow.engine.service.impl;

import gr.cyberstream.workflow.engine.config.SettingsStatus;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.MailService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

@Service
public class MailServiceImpl implements MailService {

	private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

	@Inject
	private Environment environment;

	@Autowired
	private Processes processRepository;

	@Autowired
	private RepositoryService activitiRepositorySrv;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private SettingsStatus settingsStatus;

	private final String datePattern = "dd/MM/yyyy HH:mm";
	private String from;
	private String workspaceURL;

	private boolean sendDueTask;
	private boolean sendTaskExpired;

	@PostConstruct
	public void initializeService() {
		from = environment.getProperty("mail.from");
		workspaceURL = environment.getProperty("workspaceURL");
		sendDueTask = environment.getProperty("mail.sendDueTask").equals("true");
		sendTaskExpired = environment.getProperty("mail.sendTaskExpired").equals("true");
	}

	@Override
	public void sendTaskAssignedMail(String recipient, Task task) {
		WorkflowSettings settings = getSettings();
		String taskName = task.getName();
		Date dueDate = task.getDueDate();
		String taskId = task.getId();

		if (!settings.isAssignmentNotification())
			return;

		ProcessDefinition definition = activitiRepositorySrv.getProcessDefinition(task.getProcessDefinitionId());
		WorkflowInstance instance = processRepository.getProcessInstance(task.getProcessInstanceId());

		String taskAssignedSubject = "Νέα εργασία";
		String taskAssignedContent = "<p><b>Διαδικασία:</b> '" + definition.getName() + "'</p>";
		taskAssignedContent += "<p><b>Εκτέλεση:</b> '" + instance.getTitle() + "'</p>";

		if (taskName != null && !taskName.isEmpty()) {
			taskAssignedSubject += " '" + taskName + "'";
			taskAssignedContent += "<p>Σας έχει ανατεθεί η εργασία '" + taskName + "'.</p>";
		} else {
			taskAssignedContent += "<p>Σας έχει ανατεθεί μία εργασία.</p>";
		}

		if (dueDate != null) {
			taskAssignedContent += "<p>Η χρονική περίοδος για την εκτέλεση της εργασίας είναι μέχρι τις "
					+ DateFormatUtils.format(dueDate, datePattern) + ".</p>";
		}

		taskAssignedContent += "<p><a href=\"" + workspaceURL + "/#/task/" + taskId
				+ "\">Επιλέξτε για να δείτε την εργασία</a></p>";

		try {
			sendMail(recipient, taskAssignedSubject, taskAssignedContent);
		} catch (MessagingException e) {
			logger.warn("Unable to send task assignment email to " + recipient);
		}
	}

	@Override
	public void sendTaskAssignedMail(String recipient, WfTask task) {
		WorkflowSettings settings = getSettings();
		String taskName = task.getName();
		Date dueDate = task.getDueDate();
		String taskId = task.getId();

		if (!settings.isAssignmentNotification())
			return;

		ProcessDefinition definition = activitiRepositorySrv.getProcessDefinition(task.getProcessDefinitionId());
		WorkflowInstance instance = processRepository.getProcessInstance(task.getProcessInstance().getId());

		String taskAssignedSubject = "Νέα εργασία";
		String taskAssignedContent = "<p><b>Διαδικασία:</b> '" + definition.getName() + "'</p>";
		taskAssignedContent += "<p><b>Εκτέλεση:</b> '" + instance.getTitle() + "'</p>";

		if (taskName != null && !taskName.isEmpty()) {
			taskAssignedSubject += " '" + taskName + "'";
			taskAssignedContent += "<p>Σας έχει ανατεθεί η εργασία '" + taskName + "'.</p>";
		} else {
			taskAssignedContent += "<p>Σας έχει ανατεθεί μία εργασία.</p>";
		}

		if (dueDate != null) {
			taskAssignedContent += "<p>Η χρονική περίοδος για την εκτέλεση της εργασίας είναι μέχρι τις "
					+ DateFormatUtils.format(dueDate, datePattern) + ".</p>";
		}

		taskAssignedContent += "<p><a href=\"" + workspaceURL + "/#/task/" + taskId
				+ "\">Επιλέξτε για να δείτε την εργασία</a></p>";

		try {
			sendMail(recipient, taskAssignedSubject, taskAssignedContent);
		} catch (MessagingException e) {
			logger.warn("Unable to send task assignment email to " + recipient);
		}
	}

	@Override
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
			dueTaskContent = "<p><b>Διαδικασία:</b> '" + processRepository.getProcessByDefinitionId(task.getProcessDefinitionId()).getName() + "'</p>";
			dueTaskContent += "<p><b>Εκτέλεση:</b> '" + processRepository.getInstanceById(task.getProcessInstanceId()).getTitle() + "'</p>";
		} catch (Exception exception) {
			logger.warn("Unable to get definition or execution of task.");
		}

		if (taskName != null && !taskName.isEmpty()) {
			dueTaskSubject += " '" + taskName + "'";
			dueTaskContent += "<p>Η" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία") + " '" + taskName + "'";
		} else {
			dueTaskContent += "<p>Μία" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία");
		}

		if (dueDate != null) {
			dueTaskContent += " έχει ημερομηνία εκτέλεσης μέχρι τις " + DateFormatUtils.format(dueDate, datePattern);
		} else {
			dueTaskContent += " πρόκειται να λήξει";
		}

		dueTaskContent += ".</p>";

		dueTaskContent += "<p><a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId + "\">"
				+ "Επιλέξτε για να δείτε την εργασία</a></p>";

		try {
			sendMail(recipient, dueTaskSubject, dueTaskContent);
		} catch (MessagingException e) {

			logger.warn("Unable to send due task email to " + recipient);
		}
	}

	@Override
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
			taskExpiredContent = "<p><b>Διαδικασία:</b> '" + processRepository.getProcessByDefinitionId(task.getProcessDefinitionId()).getName() + "'</p>";
			taskExpiredContent += "<p><b>Εκτέλεση:</b> " + processRepository.getInstanceById(task.getProcessInstanceId()).getTitle() + "'</p>";
		} catch (Exception exception) {
			logger.warn("Unable to get definition or execution of task.");
		}

		if (taskName != null && !taskName.isEmpty()) {
			taskExpiredSubject += " της εργασίας '" + taskName + "' έχει λήξει";
			taskExpiredContent += "<p>Η" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία") + " '" + taskName + "' έχει λήξει.</p>";
		} else {
			taskExpiredSubject += " μιας εργασίας έχει λήξει";
			taskExpiredContent += "<p>Μία" + (unAssigned ? " μη ανατεθειμένη εργασία" : " εργασία έχει λήξει.</p>");
		}

		if (dueDate != null) {
			taskExpiredContent += "<p><b>Ημερομηνία λήξης:</b> " + DateFormatUtils.format(dueDate, datePattern) + "</p>";
		}

		taskExpiredContent += "<p><a href=\"" + workspaceURL + "/#/" + (unAssigned ? "assign" : "task") + "/" + taskId
				+ "\">" + "Επιλέξτε για να δείτε την εργασία</a></p>";

		try {
			sendMail(recipient, taskExpiredSubject, taskExpiredContent);
		} catch (MessagingException e) {
			logger.warn("Unable to send task expired email to " + recipient);
		}
	}

	@Override
	public void sendBpmnErrorEmail(String administrator, WorkflowDefinition workflow, Task task, WorkflowInstance instance) {

		String subject = "Σφάλμα ορισμού διαδικασίας";

		String content = "<p><b>Διαδικασία:</b> '" + workflow.getName() + "'</p>" +
				"<p><b>Εκτέλεση:</b> '" + instance.getTitle() + "'</p>" +
				"<p>Δεν βρέθηκαν υποψήφιοι για την εκτέλεση της εργασίας με όνομα '" + task.getName()
				+ "' και κωδικό '" + task.getId() + "'.</p>";

		content += "<p>Παρακαλώ επικοινωνήστε με το συντονιστή της εκτέλεσης " + instance.getSupervisor() + ".</p>";

		try {
			sendMail(instance.getSupervisor(), subject, content);
		} catch (MessagingException e) {
			logger.warn("Unable to send definition error email to " + instance.getSupervisor());
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

	@Override
	public void sendNoCandidatesErrorEmail(String administrator, Task task, String username) {
		WorkflowDefinition workflowDef = processRepository.getProcessByDefinitionId(task.getProcessDefinitionId());
		WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());

		String subject = "Σφάλμα ανάθεσης εργασίας";

		String content = "<p><b>Διαδικασία:</b> '" + workflowDef.getName() + "'</p>" +
				"<p><b>Εκτέλεση:</b> '" + instance.getTitle() + "'</p>" +
				"<p>Δεν βρέθηκαν υποψήφιοι για την εκτέλεση της εργασίας με όνομα '" + task.getName()
				+ "' και κωδικό '" + task.getId() + "'.</p>";

		content += "<p>Παρακαλώ αναθέστε τον κατάλληλο ρόλο στον χρήστη '" + username + "'</p>";

		try {
			sendMail(administrator, subject, content);
		} catch (MessagingException e) {
			logger.warn("Unable to send no candidates error email to " + administrator);
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
