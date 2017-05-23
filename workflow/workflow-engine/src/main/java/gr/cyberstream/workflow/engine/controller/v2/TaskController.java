package gr.cyberstream.workflow.engine.controller.v2;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cyberstream.workflow.engine.controller.v1.ProcessController;
import gr.cyberstream.workflow.engine.model.api.*;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController("taskV2Controller")
@RequestMapping(value = "/api/v2")
@MultipartConfig(fileSizeThreshold = 20971520)
public class TaskController {

	final static Logger logger = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private TaskService taskService;

	/**
	 * <code>GET: /api/v2/task/process/version/{id}</code>
	 * 
	 * @param id
	 *            the process version id
	 *
	 * @return list of {@link WfTaskDetails}
	 */
	@RequestMapping(value = "/task/process/version/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTaskDetails> getVersionsTaskDetails(@PathVariable int id) {
		
		return taskService.getVersionTaskDetails(id);
	}

	/**
	 * <code>GET: /api/v2/task/{id}/candidates</code>
	 * 
	 * Returns all candidates that can claim the specified task.
	 * 
	 * @param id
	 *            the task id
	 * 
	 * @return list of {@link WfUser}
	 */
	@RequestMapping(value = "/task/{id}/candidates", method = RequestMethod.GET)
	@ResponseBody
	public List<WfUser> getCandidatesForTask(@PathVariable String id) {

		return taskService.getCandidatesByTaskId(id);
	}
	
	/**
	 * Get all active tasks (no groups/roles will be checked)
	 * 
	 * @return List of {@link WfTask}
	 */
	@RequestMapping(value = "/task", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getAllActiveTasks() {

		return taskService.getAllActiveTasks();
	}

	/**
	 * <code>GET: /api/v2/task/execution/{id}</code>
	 * 
	 * Returns all tasks of the specified execution.
	 * 
	 * @param instanceId
	 *            the instance id
	 * 
	 * @return list of {@link WfTask}
	 */
	@RequestMapping(value = "/task/execution/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getTaskByExecutionId(@PathVariable("id") String instanceId) {

		return taskService.getTasksByInstanceId(instanceId);
	}

	/**
	 * <code>GET: /api/v2/task/supervised</code>
	 * 
	 * Returns supervised tasks if the current user is a Supervisor or all tasks
	 * if the user has the role Admin
	 * 
	 * @return list of {@link WfTask}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/supervised", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin')")
	public List<WfTask> getSupervisedTasks() throws InvalidRequestException {

		return taskService.getSupervisedTasks();
	}

	/**
	 * <code>GET: /api/v2/task/assigned</code>
	 * 
	 * Returns tasks assigned to the current user
	 * 
	 * @return list of {@link WfTask}
	 */
	@RequestMapping(value = "/task/assigned", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getTasksForUser() {

		return taskService.getTasksForUser();
	}

	/**
	 * <code>GET: /api/v2/task/completed</code>
	 * 
	 * Returns completed tasks by the current user (logged in user)
	 * 
	 * @return list of {@link WfTask}
	 */
	@RequestMapping(value = "/task/completed", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserCompletedTasks() throws InvalidRequestException {

		return taskService.getUserCompletedTasks();
	}

	/**
	 * <code>GET: /tasks/completed/{user};instance={...}</code>
	 * 
	 * Get user's completed task by selected instances ids
	 * 
	 * @param user
	 * @param values
	 * @return list of {@link WfTask}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/tasks/completed/{user}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserCompletedTasksByInstances(@PathVariable("user") String user,
			@MatrixVariable(pathVar = "user", required = false) MultiValueMap<String, String> values)
			throws InvalidRequestException {

		if (user.equals("user") && values.containsKey("instance")) {

			return taskService.getUserCompledTasksByInstanceIds(values.get("instance"));

		} else if (user.equals("all") && values.containsKey("instance")) {

			return taskService.getCompletedTasksByInstances(values.get("instance"));

		} else {

			throw new InvalidRequestException(
					"Invalid request parameters. GET: /engine/api/v2/tasks/completed/{user};instance=...}");
		}
	}

	/**
	 * <code>GET: /api/v2/task/claim</code>
	 * 
	 * Returns the tasks that the current user can claim
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/claim", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getClaimTasks() throws InvalidRequestException {

		return taskService.getCandidateUserTasks();
	}

	/**
	 * <code>GET: /api/v2/task/{id}</code>
	 * 
	 * Returns a task
	 * 
	 * @param id
	 *            the task's id
	 * 
	 * @return {@link WfTask}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{id}", method = RequestMethod.GET)
	@ResponseBody
	public WfTask getTask(@PathVariable String id) throws InvalidRequestException {

		return taskService.getTask(id);
	}

	/**
	 * <code>GET: /api/v2/task/execution/ended/search:{title:.+},{after:\\d+},{before:\\d+},{anonymous:.+}</code>
	 * 
	 * Search ended executions' tasks
	 * 
	 * @param title
	 *            The title of the execution
	 * @param after
	 *            Date after which the queried executions should have ended.
	 * @param before
	 *            Date by which the queried executions should have ended.
	 * @param anonymous
	 *            true: no execution supervisor
	 * 
	 * @return list of {@link WfTask}
	 */
	@RequestMapping(value = "/task/execution/ended/search:{title:.+},{after:\\d+},{before:\\d+},{anonymous:.+}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getEndedProcessInstancesTasks(@PathVariable String title, @PathVariable long after,
			@PathVariable long before, @PathVariable boolean anonymous) {

		List<WfTask> wfTasks = taskService.getEndedProcessInstancesTasks(title, after, before, anonymous);

		return wfTasks;
	}

	/**
	 * <code>GET: /api/v2/task/search:{after:\\d+},{before:\\d+}/assignee/{userId}</code>
	 * 
	 * Returns all tasks for the specified user
	 * 
	 * @param after
	 *            Date after which the queried tasks should have being created.
	 * @param before
	 *            Date before which the queried tasks should have being created.
	 * @param userId
	 *            The assignee which the queried tasks should have.
	 * 
	 * @return list of {@link WfTask}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/search:{after:\\d+},{before:\\d+}/assignee/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserActivity(@PathVariable long after, @PathVariable long before,
			@PathVariable String userId) throws InvalidRequestException {

		List<WfTask> wfTasks = taskService.getUserActivity(after, before, userId);

		return wfTasks;
	}

	/**
	 * code>GET: /api/v2/task/completed/search:{definitionKey},{instanceTitle},{after},{before},{isSupervisor}</code>
	 * 
	 * Returns completed tasks, completed by current user or supervised by the
	 * current user
	 * 
	 * @param definitionKey
	 *            the process definition key
	 * @param executionTitle
	 *            the process execution title
	 * @param after
	 *            Date after which the queried tasks should have being
	 *            completed.
	 * @param before
	 *            Date before which the queried tasks should have being
	 *            completed.
	 * @param isSupervisor
	 *            if the user is a Supervisor then tasks supervised by the
	 *            current user are returned.
	 * 
	 * @return list of {@link WfTask}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/completed/search:{definitionKey},{executionTitle},{after},{before},{isSupervisor}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getSearchedCompletedTasks(@PathVariable String definitionKey,
			@PathVariable String executionTitle, @PathVariable long after, @PathVariable long before,
			@PathVariable String isSupervisor) throws InvalidRequestException {

		return taskService.searchCompletedTasks(definitionKey, executionTitle, after, before, isSupervisor);
	}

	/**
	 * <code>PUT: /api/v2/task</code>
	 * 
	 * Update the task's details
	 * 
	 * @param wfTaskDetails
	 * @return {@link WfTaskDetails}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfTaskDetails updateTaskDetails(@RequestBody WfTaskDetails wfTaskDetails) throws InvalidRequestException {

		return taskService.updateTaskDetails(wfTaskDetails);
	}

	/**
	 * <code>PUT: /api/v2/task/{id}/claim</code>
	 * 
	 * Assign the specified task to the current user
	 * 
	 * @param id task's id to be claimed
	 * 
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{id}/claim", method = RequestMethod.PUT)
	@ResponseBody
	public void claimTask(@PathVariable String id) throws InvalidRequestException {

		taskService.claimTask(id);
	}

	/**
	 * <code>PUT: /api/v2/task/{id}/unclaim</code>
	 * 
	 * Remove the assignment of the specified task to the current user
	 * 
	 * @param id
	 *            task's id to remove assignee from
	 * 
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{id}/unclaim", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin','User')")
	public void unClaimTask(@PathVariable String id) throws InvalidRequestException {

		taskService.unClaimTask(id);
	}

	/**
	 * <code>POST: /api/v2/task/assign/{assignee}</code> Assign the task to the
	 * specified assignee
	 * 
	 * @param assignee
	 *            the assignee's mail to assign task to
	 * @param wfTask
	 *            task to be assigned
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/assign/{assignee}", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin')")
	public void setAssigneeToTask(@PathVariable String assignee, @RequestBody WfTask wfTask) throws InvalidRequestException {

		taskService.assignTask(wfTask, assignee);
	}

	/**
	 * <code>POST: /api/v2/task/assign/{assignee}</code>
	 * 
	 * Assign the task to the specified assignee with file
	 * 
	 * @param assignee
	 *            the assignee's mail in order to assign the task to
	 * @param taskData
	 *            the task
	 * @param files
	 *            files from task
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/assign/{assignee}", method = RequestMethod.POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin')")
	public void setAssigneeToTask(@PathVariable String assignee, @RequestPart("json") String taskData, @RequestParam("file") MultipartFile[] files) 
			throws InvalidRequestException {

		ObjectMapper mapper = new ObjectMapper();

		try {
			WfTask wfTask = mapper.readValue(taskData, WfTask.class);

			taskService.assignTask(wfTask, assignee, files);

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <code>POST: /api/v2/task/save</code>
	 * 
	 * Saves task's form data
	 * 
	 * @param task
	 *            the form's data task
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/save", method = RequestMethod.POST)
	@ResponseBody
	public void saveTask(@RequestBody WfTask task) throws InvalidRequestException {

		taskService.tempTaskSave(task);
	}

	/**
	 * <code>POST: /api/v2/task/save</code>
	 * 
	 * Saves task's form data with documents
	 * 
	 * @param taskData
	 * @param files
	 * 
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/save", method = RequestMethod.POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseBody
	public void saveTask(@RequestPart("json") String taskData, @RequestParam("file") MultipartFile[] files)
			throws InvalidRequestException {

		ObjectMapper mapper = new ObjectMapper();

		try {
			WfTask wfTask = mapper.readValue(taskData, WfTask.class);

			taskService.tempTaskSave(wfTask, files);

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <code>POST: /api/v2/task/complete</code>
	 * 
	 * Completes the specified task
	 * 
	 * @param task
	 *            the task to be completed
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/complete", method = RequestMethod.POST)
	@ResponseBody
	public void completeTask(@RequestBody WfTask task) throws InvalidRequestException {

		taskService.completeTask(task);
	}

	/**
	 * <code>POST: /api/v2/task/complete</code>
	 * 
	 * Completes the specified task with files
	 * 
	 * @param taskData
	 *            task to be completed
	 * @param files
	 *            task's files
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/complete", method = RequestMethod.POST, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseBody
	public void completeTask(@RequestPart("json") String taskData, @RequestParam("file") MultipartFile[] files) throws InvalidRequestException {

		ObjectMapper mapper = new ObjectMapper();

		try {
			WfTask wfTask = mapper.readValue(taskData, WfTask.class);

			taskService.completeTask(wfTask, files);

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <code>GET: /api/v2/task/definition/{taskDefinitionKey}/process/{processDefinitionId}</code>
	 * 
	 * Returns the form of the task specified by the task definition key and the
	 * process definition key
	 * 
	 * @param taskDefinitionKey
	 *            the task definition key
	 * @param processDefinitionId
	 *            the process definition id
	 * 
	 * @return list of {@link WfFormProperty}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/definition/{taskDefinitionKey}/process/{processDefinitionId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfFormProperty> getTaskFormProperties(@PathVariable String taskDefinitionKey, @PathVariable String processDefinitionId) throws InvalidRequestException {

		return taskService.getTaskFormPropertiesByTaskDefintionKey(taskDefinitionKey, processDefinitionId);
	}

	/**
	 * <code>PUT: /api/v2/task/definition/{taskDefinitionKey}/process/{processDefinitionKey}/formelement</code>
	 * 
	 * Creates a new task form element from form property
	 * 
	 * @param processDefinitionKey
	 *            the process definition key
	 * @param taskDefintionKey
	 *            the task definition key
	 * @param formProperty
	 *            task's formProperty
	 */
	@RequestMapping(value = "/task/definition/{taskDefintionKey}/process/{processDefinitionKey}/formelement", method = RequestMethod.PUT)
	public void saveUserTaskFormElement(@PathVariable String processDefinitionKey, @PathVariable String taskDefintionKey,
			@RequestBody WfFormProperty formProperty) {

		taskService.saveTaskFormElement(formProperty, taskDefintionKey, processDefinitionKey);
	}

	/**
	 * <code>GET: /api/v2/task/{id}/document</code>
	 * 
	 * Returns the all the documents of the execution of the task specified
	 * 
	 * @param id
	 *            the task id
	 * 
	 * @return list of {@link WfDocument}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{id}/document", method = RequestMethod.GET)
	@ResponseBody
	public List<WfDocument> getDocumentsByTask(@PathVariable int id) throws InvalidRequestException {

		return taskService.getProcessInstanceDocumentsByTask(id);
	}

	/**
	 * Syntax error exception handler
	 * 
	 * @param req
	 *            the request been made
	 * @param exception
	 *            the occured exception
	 * @return {@link ErrorResponse}
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InvalidRequestException.class)
	@ResponseBody
	public ErrorResponse handleSyntaxError(HttpServletRequest req, InvalidRequestException exception) {
		logger.error("Request: " + req.getRequestURL() + " raised " + exception);
		return exception.getError();
	}

	/**
	 * Internal server error exception handler
	 * 
	 * @param req
	 *            the request been made
	 * @param exception
	 *            the occured exception
	 * @return {@link ErrorResponse}
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(InternalException.class)
	@ResponseBody
	public ErrorResponse handleInternalError(HttpServletRequest req, InternalException exception) {
		logger.error("Request: " + req.getRequestURL() + " raised " + exception);
		return exception.getError();
	}
}
