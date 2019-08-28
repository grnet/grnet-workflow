package gr.cyberstream.workflow.engine.service;

import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import org.activiti.engine.task.Task;

public interface MailService {

	/**
	 * When a task is assigned, sends an email to the "assignee"
	 *
	 * @param recipient
	 *            The assignee's email
	 *
	 * @param task
	 *            The task which is assigned
	 */
	public void sendTaskAssignedMail(String recipient, Task task);

	/**
	 * When a task is has a candidate group, sends an email to the members of the group
	 *
	 * @param recipient
	 *            The assignee's email
	 *
	 * @param task
	 *            The task which is assigned
	 */
	public void sendCandidateGroupMail(String recipient, Task task);

	/**
	 * When a task is assigned, sends an email to the "assignee"
	 *
	 * @param recipient
	 *            The assignee's email
	 *
	 * @param task
	 *            The task which is assigned
	 */
	public void sendTaskAssignedMail(String recipient, WfTask task);

	/**
	 * Send an email during the evaluation of alerts to the .
	 * 
	 * @param recipient
	 *            The user's email to send the email to
	 * 
	 * @param task
	 *            The task which is about to expire
	 * 
	 * @param unAssigned
	 *            If the task is assigned
	 * 
	 */
	public void sendDueTaskMail(String recipient, Task task, boolean unAssigned);

	/**
	 * Send an email during the evaluation of alerts to the assignee of a task,
	 * if the task has passed the due date to complete it
	 * 
	 * @param recipient
	 *            The user's email to send the email to
	 * 
	 * @param task
	 *            The task which has been expired
	 * 
	 * @param unAssigned
	 *            If the task is assigned
	 */
	public void sendTaskExpiredMail(String recipient, Task task, boolean unAssigned);

	/**
	 * Sends an email to supervisor of an instance, when no candidates for task
	 * found
	 * 
	 * @param supervisor
	 *            The supervisor's email
	 * 
	 * @param workflow
	 *            The definition of the instance
	 *
	 * @param task
	 *            The task for which no candidates were found
	 *
	 * @param instance
	 *            The instance of the task for which no candidates were found
	 */
	public void sendBpmnErrorEmail(String supervisor, WorkflowDefinition workflow, Task task, WorkflowInstance instance);

	/**
	 * Send an email to assignee of a task if the task has passed its the due
	 * date to complete the task
	 *
	 * @param task
	 *            The task
	 *
	 * @param content
	 *            The user's message (mail body)
	 *
	 * @throws InternalException
	 */
	public void sendTaskDueDateNotification(Task task, String content) throws InternalException;

	/**
	 * Sends an email to the administrator, to add a candidate to a task
	 *
	 * @param administrator
	 *            The administrator
	 *
	 * @param task
	 *            The task
	 *
	 * @param username
	 *            The username of the user to be added
	 *
	 * @throws InternalException
	 */
	public void sendNoCandidatesErrorEmail(String administrator, Task task, String username) throws InternalException;
}
