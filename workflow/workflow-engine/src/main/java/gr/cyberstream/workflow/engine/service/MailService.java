package gr.cyberstream.workflow.engine.service;

import java.util.Date;

import org.activiti.engine.task.Task;

import gr.cyberstream.workflow.engine.model.WorkflowDefinition;

public interface MailService {

	/**
	 * When a task is assigned, sends an email to the "assignee"
	 * 
	 * @param recipient
	 *            The assignee's email
	 * 
	 * @param taskId
	 *            The task's id which is assigned
	 * 
	 * @param taskName
	 *            The tasks's name
	 * 
	 * @param dueDate
	 *            The task's due date
	 */
	public void sendTaskAssignedMail(String recipient, String taskId, String taskName, Date dueDate);

	/**
	 * Send an email during the evaluation of alerts to the .
	 * 
	 * @param recipient
	 *            The user's email to send the email to
	 * 
	 * @param taskId
	 *            The task's id which has been expired
	 * 
	 * @param taskName
	 *            The task's name which has been expired
	 * 
	 * @param dueDate
	 *            The task's due date which has been expired
	 * 
	 * @param unAssigned
	 *            If the task is assigned
	 * 
	 */
	public void sendDueTaskMail(String recipient, String taskId, String taskName, Date dueDate, boolean unAssigned);

	/**
	 * Send an email during the evaluation of alerts to the assignee of a task,
	 * if the task has passed the due date to complete it
	 * 
	 * @param recipient
	 *            The user's email to send the email to
	 * 
	 * @param taskId
	 *            The task's id which has been expired
	 * 
	 * @param taskName
	 *            The task's name which has been expired
	 * 
	 * @param dueDate
	 *            The task's due date which has been expired
	 * 
	 * @param unAssigned
	 *            If the task is assigned
	 */
	public void sendTaskExpiredMail(String recipient, String taskId, String taskName, Date dueDate, boolean unAssigned);

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
	 * @param taskName
	 *            The task's name, which no candidates found
	 */
	public void sendBpmnErrorEmail(String supervisor, WorkflowDefinition workflow, String taskName);

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

}
