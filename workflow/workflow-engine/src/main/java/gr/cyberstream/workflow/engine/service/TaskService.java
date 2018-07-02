package gr.cyberstream.workflow.engine.service;

import gr.cyberstream.workflow.engine.model.UserTaskFormElement;
import gr.cyberstream.workflow.engine.model.api.*;
import org.activiti.engine.task.Task;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskService {

	/**
	 * 
	 * @param versionId
	 *            The version id which TaskDetails will be retrieved
	 * 
	 * @return List of {@link WfTaskDetails}
	 */
	public List<WfTaskDetails> getVersionTaskDetails(int versionId);

	/**
	 * Get supervised tasks if user has role admin then all tasks returned
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getSupervisedTasks();

	/**
	 * Returns a list of tasks by instance id
	 * 
	 * @param instanceId
	 *            The instanceId which tasks will be retrieved
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getTasksByInstanceId(String instanceId);

	/**
	 * Get the user's completed tasks
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getUserCompletedTasks();

	/**
	 * Search for complete tasks based on given criteria
	 * 
	 * @param definitionKey
	 *            The definition key if user selects a specific definition
	 * 
	 * @param instanceTitle
	 *            Instance title
	 * 
	 * @param after
	 *            Tasks which will be retrieved, will be completed after that
	 *            date
	 * 
	 * @param before
	 *            Tasks which will be retrieved, will be completed after that
	 *            before
	 * 
	 * @param isSupervisor
	 *            Indicates if the person who is searches for tasks is
	 *            supervisor. There are 2 pages using the same function call.
	 *            One of them is for supervisor usage, so the value will be
	 *            "true".
	 * 
	 * 
	 * @return List of {@link WfTask}
	 * @throws InvalidRequestException
	 */
	public List<WfTask> searchCompletedTasks(String definitionKey, String instanceTitle, long after, long before,
			String isSupervisor) throws InvalidRequestException;

	/**
	 * Get user's completed tasks by selected ids
	 * 
	 * @param instanceIds
	 *            Instance ids which completed tasks will be retrieved
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getUserCompledTasksByInstanceIds(List<String> instanceIds);

	/**
	 * Probably same as
	 * {@link TaskService#getUserCompledTasksByInstanceIds(List)} <br>
	 * Get completed tasks by selected ids
	 * 
	 * @param instanceIds
	 *            Instance ids which completed tasks will be retrieved
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getCompletedTasksByInstances(List<String> instanceIds);

	/**
	 * Return a task by taskId
	 * 
	 * @param taskId
	 *            Task Id
	 * 
	 * @return {@link WfTask}
	 * @throws InvalidRequestException
	 */
	public WfTask getTask(String taskId) throws InvalidRequestException;

	/**
	 * Get task's formProperties
	 * 
	 * @param taskDefinitionKey
	 *            The task's definition key
	 * 
	 * @param processDefinitionId
	 *            The process definition id
	 * 
	 * @return List of {@link WfFormProperty}
	 * @throws InvalidRequestException
	 */
	public List<WfFormProperty> getTaskFormPropertiesByTaskDefintionKey(String taskDefinitionKey,
			String processDefinitionId) throws InvalidRequestException;

	/**
	 * Completes given task (without files)
	 * 
	 * @param task
	 *            Task to be completed
	 * 
	 * @throws InvalidRequestException
	 */
	public void completeTask(WfTask task) throws InvalidRequestException;

	/**
	 * Completes given task (with files)
	 * 
	 * @param task
	 *            Task to be completed
	 * 
	 * @param files
	 *            Task's files
	 * 
	 * @throws InvalidRequestException
	 */
	public void completeTask(WfTask task, MultipartFile[] files) throws InvalidRequestException;

	/**
	 * Temporary saves task (with files)
	 * 
	 * @param task
	 *            Task to be temporary saved
	 * 
	 * @param files
	 *            Task's files
	 * 
	 * @throws InvalidRequestException
	 */
	public void tempTaskSave(WfTask task, MultipartFile[] files) throws InvalidRequestException;

	/**
	 * Temporary saves task (without files)
	 * 
	 * @param task
	 *            Task to be temporary saved
	 * 
	 * @throws InvalidRequestException
	 */
	public void tempTaskSave(WfTask task) throws InvalidRequestException;

	/**
	 * Get candidates by task
	 * 
	 * @param taskId
	 *            Task's id which candidates will be retrieved
	 * 
	 * @return List of {@link WfUser}
	 */
	public List<WfUser> getCandidatesByTaskId(String taskId);

	/**
	 * Returns Assigned tasks for the user in context (Logged in user)
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getTasksForUser();

	/**
	 * Set assignee to a task (without files)
	 * 
	 * @param wfTask
	 * @param assigneeId
	 * @throws InvalidRequestException
	 */
	public void assignTask(WfTask wfTask, String assigneeId) throws InvalidRequestException;

	/**
	 * Set assignee to a task (with files)
	 * 
	 * @param wfTask
	 *            Task to be assigned
	 * 
	 * @param assigneeId
	 *            Assignee's email address
	 * 
	 * @param files
	 *            Task's files
	 * 
	 * @throws InvalidRequestException
	 */
	public void assignTask(WfTask wfTask, String assigneeId, MultipartFile[] files) throws InvalidRequestException;

	/**
	 * Removes assignee from a task
	 * 
	 * @param taskId
	 *            Task's id which assignee will be removed from
	 * 
	 * @throws InvalidRequestException
	 */
	public void unClaimTask(String taskId) throws InvalidRequestException;

	/**
	 * Set logged in user as assignee to given task
	 * 
	 * @param taskId
	 *            Task's id which will be claimed by logged in user
	 * 
	 * @throws InvalidRequestException
	 */
	public void claimTask(String taskId) throws InvalidRequestException;

	/**
	 * Returns a list of Wftasks to be claim by user according to user role
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getCandidateUserTasks();

	/**
	 * Update task's Details such as description, if is assigned by supevisor,
	 * etc.
	 * 
	 * @param wfTaskDetails
	 *            The updated task's details
	 * 
	 * @return {@link WfTaskDetails}
	 * @throws InvalidRequestException
	 */
	public WfTaskDetails updateTaskDetails(WfTaskDetails wfTaskDetails) throws InvalidRequestException;

	/**
	 * Returns all active tasks (no role/group check will be made)
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getAllActiveTasks();

	/**
	 * Returns active tasks by given criteria
	 *
	 * @param definitionName
	 * @param taskName
	 * @param after
	 * @param before
	 *
	 * @return
	 */
	public List<WfTask> getActiveTasks(String definitionName, String taskName, long after, long before);

	/**
	 * Returns tasks of ended processes based on given criteria.
	 * 
	 * @param title
	 *            Instance's title
	 * 
	 * @param after
	 *            Corresponds to date Instance is completed after
	 * 
	 * @param before
	 * 
	 * @param anonymous
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getEndedProcessInstancesTasks(String title, long after, long before, boolean anonymous);

	/**
	 * Get all tasks of specified user
	 * 
	 * @param after
	 * @param before
	 * @param userId
	 * @return
	 * @throws InvalidRequestException
	 */
	public List<WfTask> getUserActivity(long after, long before, String userId) throws InvalidRequestException;

	/**
	 * Apply current workflow settings
	 * 
	 * @param task
	 */
	public void applyTaskSettings(Task task);

	/**
	 * Saves a task form element
	 * 
	 * @param wfFormProperty
	 * @param taskDefinitionKey
	 * @param definitionVersion
	 * @return {@link UserTaskFormElement}
	 */
	public UserTaskFormElement saveTaskFormElement(WfFormProperty wfFormProperty, String taskDefinitionKey,
			String definitionVersion);

	/**
	 * Get instance's documents by task
	 * 
	 * @param id
	 *            Task's id
	 * 
	 * @return List of {@link WfDocument}
	 * @throws InvalidRequestException
	 */
	public List<WfDocument> getProcessInstanceDocumentsByTask(int id) throws InvalidRequestException;


	/**
	 * This function is called when there is no candidate for a specific task.
	 *
	 * @param taskId
	 *            The ID of the task that has no candidates
	 */
	public void notifyAdminForTask(String taskId, String username) throws InvalidRequestException;
}
