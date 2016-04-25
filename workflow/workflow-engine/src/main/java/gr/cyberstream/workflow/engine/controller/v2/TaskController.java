package gr.cyberstream.workflow.engine.controller.v2;

import java.io.IOException;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cyberstream.workflow.engine.controller.v1.ProcessController;
import gr.cyberstream.workflow.engine.model.api.ErrorResponse;
import gr.cyberstream.workflow.engine.model.api.WfDocument;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.ProcessService;

@RestController
@RequestMapping(value = "/api/v2")
@MultipartConfig(fileSizeThreshold = 20971520)
public class TaskController {

	final static Logger logger = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private ProcessService processService;
	
	/**
	 * <code>GET: /api/v2/task/process/version/{id}</code>
	 * 
	 * @param id the process version id
	 * 
	 * return
	 */
	@RequestMapping(value = "/task/process/version/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTaskDetails> getTaskDetailsByDefinition(@PathVariable int id) {
		return processService.getVersionTaskDetails(id);
	}
	
	/**
	 * <code>GET: /api/v2/task/{id}/candidates</code>
	 * 
	 * Returns all candidates that can claim the specified task.
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/task/{id}/candidates", method = RequestMethod.GET)
	@ResponseBody
	public List<WfUser> getCandidatesForTask(@PathVariable String id){
		
		logger.info("Getting candidates for task: " + id);
		
		return processService.getCandidatesByTaskId(id);
	}
	
	/**
	 * <code>GET: /api/v2/task/execution/{id}</code>
	 * 
	 * Returns all tasks of the specified execution.
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/task/execution/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getTaskByExecutionId(@PathVariable String id){
		
		return processService.getTasksByInstanceId(id);
	}
	
	/**
	 * <code>GET: /api/v2/task/supervised</code>
	 * 
	 * Returns supervised tasks if the current user is a Supervisor 
	 * or all tasks if the user has the role Admin
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/supervised", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin')") 
	public List<WfTask> getSupervisedTasks() throws InvalidRequestException {
		
		return processService.getSupervisedTasks();
	}
	
	/**
	 * <code>GET: /api/v2/task/assigned</code>
	 * 
	 * Returns tasks assigned to the current user
	 * 
	 * @return
	 */
	@RequestMapping(value = "/task/assigned", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getTasksForUser(){
		
		logger.info("Requested tasks in progress for user in context");
		
		return processService.getTasksForUser();
	}
	
	/**
	 * <code>GET: /api/v2/task/completed</code>
	 * 
	 * Returns completed tasks by the current user
	 * 
	 * @return
	 */
	@RequestMapping(value = "/task/completed", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserCompletedTasks() throws InvalidRequestException {
		
		return processService.getUserCompletedTasks();
	}
	
	/**
	 * <code>GET: /api/v2/task/completed/{option};instance={...}</code>
	 * 
	 * Get user's completed task by selected instances ids
	 * 
	 * @param instanceIds
	 * @return
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/tasks/completed/{user}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserCompletedTasksByInstances(@PathVariable("user") String user,
			@MatrixVariable(pathVar="user", required=false) MultiValueMap<String, String> values) throws InvalidRequestException{
		
		if (user.equals("user") && values.containsKey("instance")) {
			
			return processService.getUserCompledTasksByInstanceIds(values.get("instance"));
			
		} else if (user.equals("all") && values.containsKey("instance")) {
			
			return processService.getCompletedTasksByInstances(values.get("instance"));
			
		} else {
			
			throw new InvalidRequestException("Invalid request parameters. GET: /engine/api/v2/tasks/completed/{user};instance=...}");
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
		
		logger.info("Requesting tasks to be claimed");
		
		return processService.getCandidateUserTasks();
	}
	
	/**
	 * <code>GET: /api/v2/task/{id}</code>
	 * 
	 * Returns a task
	 *  
	 * @param id
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{id}", method = RequestMethod.GET)
	@ResponseBody
	public WfTask getTask(@PathVariable String id) throws InvalidRequestException {
		
		return processService.getTask(id);
	}
	
	/**
	 * <code>GET: /api/v2/task/execution/ended/search:{title:.+},{after:\\d+},{before:\\d+},{anonymous:.+}</code>
	 * 
	 * Search ended executions' tasks
	 * 
	 * @param title The title of the execution
	 * @param after Date after which the queried executions should have ended.
	 * @param before Date by which the queried executions should have ended.
	 * @param anonymous true: no execution supervisor
	 * 
	 * @return
	 */
	@RequestMapping(value = "/task/execution/ended/search:{title:.+},{after:\\d+},{before:\\d+},{anonymous:.+}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getEndedProcessInstancesTasks(@PathVariable String title, @PathVariable long after, 
			@PathVariable long before, @PathVariable boolean anonymous) {
		
		logger.info("instance title::"+title);
		logger.info("after::"+after);
		logger.info("before::"+before);
		logger.info("anonymous::"+anonymous);
		
		List<WfTask> wfTasks = processService.getEndedProcessInstancesTasks(title, after, before, anonymous);
		
		return wfTasks;
	}	
	
	/**
	 * <code>GET: /api/v2/task/search:{after:\\d+},{before:\\d+}/assignee/{userId}</code>
	 * 
	 * Returns all tasks for the specified user
	 * 
	 * @param after Date after which the queried tasks should have being created.
	 * @param before Date before which the queried tasks should have being created.
	 * @param userId The assignee which the queried tasks should have.
	 * 
	 * @return
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/task/search:{after:\\d+},{before:\\d+}/assignee/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserActivity(@PathVariable long after, @PathVariable long before, 
			@PathVariable String userId) throws InvalidRequestException{
		
		logger.info("Request all tasks for the specified user.");
		
		logger.info("after::"+after);
		logger.info("before::"+before);
		logger.info("user id::"+userId);
		
		List<WfTask> wfTasks = processService.getUserActivity(after, before, userId);
		
		return wfTasks;
	}
	
	/**
	 * code>GET: /api/v2/task/completed/search:{definitionKey},{instanceTitle},{after},{before},{isSupervisor}</code>
	 * 
	 * Returns completed tasks, completed by current user 
	 * or supervised by the current user 
	 * 
	 * @param definitionKey the process definition key
	 * @param executionTitle the process execution title
	 * @param after Date after which the queried tasks should have being completed.
	 * @param before Date before which the queried tasks should have being completed.
	 * @param isSupervisor 
	 * 		if the user is a Supervisor then tasks supervised by the current user are returned.
	 * 
	 * @return
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/task/completed/search:{definitionKey},{executionTitle},{after},{before},{isSupervisor}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getSearchedCompletedTasks(@PathVariable String definitionKey, @PathVariable String executionTitle,
			@PathVariable long after, @PathVariable long before, @PathVariable String isSupervisor) throws InvalidRequestException {

		logger.info("Definition key " + definitionKey + " instance title " + executionTitle + " After " + after
				+ " before " + before);

		return processService.getSearchedCompletedTasks(definitionKey, executionTitle, after, before, isSupervisor);
	}
	
	/**
	 * <code>PUT: /api/v2/task</code>
	 * 
	 * Update the task 
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfTaskDetails updateTaskDetails(@RequestBody WfTaskDetails wfTaskDetails) throws InvalidRequestException{
		
		return processService.updateTaskDetails(wfTaskDetails);
	}
	
	/**
	 * <code>PUT: /api/v2/task/{id}/claim</code>
	 * 
	 * Assign the specified task to the current user
	 * 
	 * @param id
	 * 
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{id}/claim", method = RequestMethod.PUT)
	@ResponseBody
	public void claimTask(@PathVariable String id) throws InvalidRequestException{
		
		processService.claimTask(id);
	}
	
	/**
	 * <code>PUT: /api/v2/task/{id}/unclaim</code>
	 * 
	 * Remove the assignment of the specified task to the current user
	 * 
	 * @param id
	 * 
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{id}/unclaim", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin','User')") 
	public void unClaimTask(@PathVariable String id) throws InvalidRequestException{
		
		processService.unClaimTask(id);
	}
	
	/**
	 * <code>POST: /api/v2/task/assign/{assignee}</code>
	 * 
	 * Assign the task to the specified assignee
	 * 
	 * @param assignee
	 * 
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/task/assign/{assignee}", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin')") 
	public void setAssigneeToTask(@PathVariable String assignee, @RequestBody WfTask wfTask) throws InvalidRequestException{
		
		logger.info("Set assignee with mail " + assignee + " to task with id " + wfTask.getId() );
		
		processService.assignTask(wfTask, assignee);
	}
	
	/**
	 * <code>POST: /api/v2/task/assign/{assignee}</code>
	 * 
	 * Assign the task to the specified assignee with file
	 * 
	 * @param assignee
	 * 
	 * @throws InvalidRequestException  
	 */
	@RequestMapping(value = "/task/assign/{assignee}", method = RequestMethod.POST,
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin')") 
	public void setAssigneeToTask(@PathVariable String assignee, @RequestPart("json") String taskData,
			@RequestParam("file") MultipartFile[] files) throws InvalidRequestException {
		
		logger.info("Assign task to " + assignee + " with " + "file");
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			WfTask wfTask = mapper.readValue(taskData, WfTask.class);
			
			processService.assignTask(wfTask, assignee, files);
			
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
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/save", method = RequestMethod.POST)
	@ResponseBody
	public void saveTask(@RequestBody WfTask task) throws InvalidRequestException{
		
		processService.tempTaskSave(task);
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
	@RequestMapping(value = "/task/save", method = RequestMethod.POST,
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public void saveTask(@RequestPart("json") String taskData, @RequestParam("file") MultipartFile[] files) throws InvalidRequestException{
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			WfTask wfTask = mapper.readValue(taskData, WfTask.class);
			
			processService.tempTaskSave(wfTask, files);
			
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
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/complete", method = RequestMethod.POST)
	@ResponseBody
	public void completeTask(@RequestBody WfTask task) throws InvalidRequestException{
		
		logger.info("Request complete task without multipart request");
		
		processService.completeTask(task);
	}
	
	/**
	 * <code>POST: /api/v2/task/complete</code>
	 * 
	 * Completes the specified task with files
	 * 
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/complete", method = RequestMethod.POST, consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public void completeTask(@RequestPart("json") String taskData, @RequestParam("file") MultipartFile[] files) throws InvalidRequestException{
		
		logger.info("Request complete task with multipart request");
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			WfTask wfTask = mapper.readValue(taskData, WfTask.class);
			
			processService.completeTask(wfTask, files);
			
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
	 * Returns the form of the task specified by
	 * the task definition key and the process definition key
	 * 
	 * @param taskDefinitionKey
	 * @param processDefinitionId
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/definition/{taskDefinitionKey}/process/{processDefinitionId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfFormProperty> getTaskFormProperties(@PathVariable String taskDefinitionKey, @PathVariable String processDefinitionId)
			throws InvalidRequestException {
		
		return processService.getTaskFormPropertiesByTaskDefintionKey(taskDefinitionKey, processDefinitionId);
	}
	
	/**
	 * <code>PUT: /api/v2/task/definition/{taskDefinitionKey}/process/{processDefinitionKey}/formelement</code>
	 * 
	 * Creates a new task form element from form property
	 * 
	 * @param processDefinitionKey
	 * @param taskDefintionKey
	 * @param formProperty
	 */
	@RequestMapping(value = "/task/definition/{taskDefintionKey}/process/{processDefinitionKey}/formelement", method = RequestMethod.PUT)
	public void saveUserTaskFormElement(@PathVariable String processDefinitionKey, @PathVariable String taskDefintionKey,
			@RequestBody WfFormProperty formProperty) {
		
		processService.saveTaskFormElement(formProperty, taskDefintionKey, processDefinitionKey);
	}
	
	/**
	 * <code>GET: /api/v2/task/{id}/document</code>
	 * 
	 * Returns the all the documents of the execution 
	 * of the task specified
	 * 
	 * @param id
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{id}/document", method = RequestMethod.GET)
	@ResponseBody
	public List<WfDocument> getDocumentsByTask(@PathVariable int id)
			throws InvalidRequestException {
		
		logger.info("Getting documents for task: " + id);
		
		return processService.getProcessInstanceDocumentsByTask(id);
	}
	
	
	
	/**
	 * Syntax error exception handler
	 * 
	 * @param req
	 * @param exception
	 * @return
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
	 * @param exception
	 * @return
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(InternalException.class)
	@ResponseBody
	public ErrorResponse handleInternalError(HttpServletRequest req, InvalidRequestException exception) {
		logger.error("Request: " + req.getRequestURL() + " raised " + exception);
		return exception.getError();
	}
}
