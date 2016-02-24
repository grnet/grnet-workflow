/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

import gr.cyberstream.workflow.engine.model.api.ErrorResponse;
import gr.cyberstream.workflow.engine.model.api.WfDocument;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.service.CustomException;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.ProcessService;

/**
 * Implements all RESTfull requests related to process execution
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/api")
@MultipartConfig(fileSizeThreshold = 20971520)
public class ExecutionController {

	final static Logger logger = LoggerFactory.getLogger(ExecutionController.class);

	@Autowired
	private ProcessService processService;

	/**
	 * Starts a new process instance using form data
	 * 
	 * @param processId
	 *            the workflow definition id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws CustomException 
	 */
	@RequestMapping(value = "/process/{processId}/start", method = RequestMethod.POST)
	@ResponseBody
	public WfProcessInstance startProcess(@PathVariable int processId, @RequestBody WfProcessInstance instanceData) throws InvalidRequestException {
		
		logger.info("Start process: " + processId);
		
		return processService.startProcess(processId, instanceData);
	}

	/**
	 * Starts a new process instance using form data and files
	 * 
	 * @param id external form id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/public/process/form/{id}/document/start", method = RequestMethod.POST, 
			consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public WfProcessInstance startPublicProcessWithDocuments(@PathVariable String id, @RequestParam("json") String instanceData,
			@RequestParam("file") MultipartFile[] files)
			throws InvalidRequestException {
		
		logger.info("Start process using form: " + id);
		
		ObjectMapper mapper = new ObjectMapper();
		
		WfProcessInstance wfProcessInstance;
		
		try {
			wfProcessInstance = mapper.readValue(instanceData, WfProcessInstance.class);
			
			if (wfProcessInstance.getCaptchaAnswer() == null
					|| wfProcessInstance.getCaptchaAnswer().isEmpty()) {
				
				throw new InvalidRequestException("Captcha answer is null or empty."); 
			}
			
			return processService.startPublicProcess(id, wfProcessInstance, files);
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Starts a new process instance using form data
	 * 
	 * @param id external form id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws CustomException 
	 */
	@RequestMapping(value = "/public/process/form/{id}/start", method = RequestMethod.POST)
	@ResponseBody
	public WfProcessInstance startPublicProcess(@PathVariable String id, @RequestBody WfProcessInstance instanceData) 
			throws InvalidRequestException {
		
		logger.info("Start process using form: " + id);
		
		if (instanceData.getCaptchaAnswer() == null
				|| instanceData.getCaptchaAnswer().isEmpty()) {
			
			throw new InvalidRequestException("Captcha answer is null or empty."); 
		}
		
		return processService.startPublicProcess(id, instanceData);
	}

	/**
	 * Starts a new process instance using form data and files
	 * 
	 * @param processId
	 *            the workflow definition id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/document/start", method = RequestMethod.POST, 
			consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public WfProcessInstance startProcessWithDocuments(@PathVariable int processId, @RequestParam("json") String instanceData,
			@RequestParam("file") MultipartFile[] files)
			throws InvalidRequestException {
		
		logger.info("Start process: " + processId);
		
		ObjectMapper mapper = new ObjectMapper();
		
		WfProcessInstance wfProcessInstance;
		
		try {
			wfProcessInstance = mapper.readValue(instanceData, WfProcessInstance.class);
			
			return processService.startProcess(processId, wfProcessInstance, files);
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	@RequestMapping(value = "/task/{taskId}/candidates", method = RequestMethod.GET)
	@ResponseBody
	public List<WfUser> getCandidatesForTask(@PathVariable String taskId){
		
		logger.info("Getting candidates for task: " + taskId);
		
		return processService.getCandidatesByTaskId(taskId);
	}
	
	/**
	 * Returns all users as candidates
	 * 
	 * @param taskId
	 * @return
	 */
	@RequestMapping(value = "/task/candidates/all", method = RequestMethod.GET)
	@ResponseBody
	public List<WfUser> getAllCandidates(HttpServletRequest request){
		
		return processService.getAllCandidates();
	}
	
	
	@RequestMapping(value = "/tasks/instance/{instanceId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getTaskByInstanceId(@PathVariable String instanceId){
		
		return processService.getTasksByInstanceId(instanceId);
	}

	/**
	 * 
	 * 
	 * @param instanceIds
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/tasks/supervised", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUnassingedTasksByInstancesIds() throws InvalidRequestException {
		
		return processService.getSupervisedTasks();
	}
	
	/**
	 * Returns Assigned tasks for the user in context (Logged in user)
	 * @return
	 */
	@RequestMapping(value = "/task/inprogress/user", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getTasksForUser(){
		
		logger.info("Requested tasks in progress for user in contenxt");
		
		return processService.getTasksForUser();
	}
	
	/**
	 * Returns Completed tasks for the user in context (Logged in user)
	 * 
	 * @return
	 */
	@RequestMapping(value = "/task/completed/user", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getCompletedTasksForUser(){
		
		return processService.getCompletedTasksForUser();
	}

	
	/**
	 * Returns a task (instance)
	 *  
	 * @param taskId
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{taskId}", method = RequestMethod.GET)
	@ResponseBody
	public WfTask getTask(@PathVariable String taskId) throws InvalidRequestException {
		
		return processService.getTask(taskId);
	}
	

	/**
	 * Returns completed task
	 *  
	 * @param taskId
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{taskId}/completed", method = RequestMethod.GET)
	@ResponseBody
	public WfTask getCompletedTask(@PathVariable String taskId) throws InvalidRequestException {
		
		return processService.getCompletedTask(taskId);
	}
	
	
	/**
	 * Get user's completed task
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/tasks/completed", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserCompletedTasks() throws InvalidRequestException {
		
		return processService.getUserCompletedTasks();
	}
	

	/**
	 * 
	 * @param definitionKey
	 * @param instanceTitle
	 * @param after
	 * @param before
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/tasks/completed/search:{definitionKey},{instanceTitle},{after},{before},{isSupervisor}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getSearchedCompletedTasks(@PathVariable String definitionKey, @PathVariable String instanceTitle,
			@PathVariable long after, @PathVariable long before, @PathVariable String isSupervisor, HttpServletRequest request) {

		logger.info("Definition key " + definitionKey + " instance title " + instanceTitle + " After " + after
				+ " before " + before);

		return processService.getSearchedCompletedTasks(definitionKey, instanceTitle, after, before, isSupervisor);
	}
	
	/**
	 * Get user's completed instances
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/instances/completed", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getUserCompletedInstances() throws InvalidRequestException {
		
		return processService.getUserCompletedInstances();
	}
	
	/**
	 * Get user's completed task by selected instances ids
	 * 
	 * @param instanceIds
	 * @return
	 */
	@RequestMapping(value = "/tasks/completed/instances", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserCompletedTasksByInstances(@RequestParam("i") List<String> instanceIds){
		
		return processService.getUserCompledTasksByInstanceIds(instanceIds);
	}
	
	
	/**
	 * Get completed tasks by selected instances ids
	 * 
	 * @param instanceIds
	 * @return
	 */
	@RequestMapping(value = "/tasks/instances", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getCompletedTasksByInstances(@RequestParam("i") List<String> instanceIds){
		return processService.getCompletedTasksByInstances(instanceIds);
	}
	
	/**
	 * Completes task with given id
	 * @param taskId
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/complete", method = RequestMethod.POST, 
			consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public void completeTask(@RequestParam("json") String taskData,
			@RequestParam("file") MultipartFile[] files) throws InvalidRequestException{
		
		logger.info("Request complete task");
		
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
	 * 
	 * @param taskData
	 * @param files
	 * @throws InvalidRequestException
	 */
	
	@RequestMapping(value = "/task/tempsave", method = RequestMethod.POST,consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public void tempSaveTask(@RequestParam("json") String taskData, @RequestParam("file") MultipartFile[] files) throws InvalidRequestException{
		
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
	
	@RequestMapping(value = "/task/tempsave", method = RequestMethod.POST)
	@ResponseBody
	public void tempSaveTask(@RequestBody WfTask task) throws InvalidRequestException{
		
		processService.tempTaskSave(task);
	}
	
	/**
	 * Completes task with given id
	 * @param taskId
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/complete", method = RequestMethod.POST)
	@ResponseBody
	public void completeTask(@RequestBody WfTask task) throws InvalidRequestException{
		
		logger.info("Request complete task with id " + task.getId());
		
		processService.completeTask(task);
	}
	
	
	@RequestMapping(value = "/process/exec/{execId}/document/{variable}", method = RequestMethod.POST, 
			consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	public WfDocument saveDocument(@PathVariable String execId, @PathVariable String variable, @RequestPart("json") WfDocument document, 
			@RequestPart("file") MultipartFile file) throws InvalidRequestException {

		logger.info("Saving document.");
		
		InputStream inputStream = null;
		
		try {
			inputStream = file.getInputStream();
			
		} catch (IOException e) {
			
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new InvalidRequestException("Unable to get document file.");
		}
		
		String variableName = "";
		
		return processService.saveDocument(execId, variableName, document, inputStream, file.getContentType());
	}
	
	@RequestMapping(value = "/process/{processId}/document/{variable}", method = RequestMethod.PUT)
	public WfDocument updateDocument(@PathVariable String execId, @PathVariable String variable, @RequestBody WfDocument document)
			throws InvalidRequestException {

		logger.info("Updating document.");
		
		return processService.updateDocument(execId, variable, document);
	}
	
	/**
	 * Set assignee to a task
	 * @param taskId
	 * @param assigneeId
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/task/assignee/{assigneeId}", method = RequestMethod.POST)
	@ResponseBody
	public void setAssigneeToTask(@PathVariable String assigneeId, @RequestBody WfTask wfTask) throws InvalidRequestException{
		
		logger.info("Set assignee with id " + assigneeId + " to task with id " + wfTask.getId() );
		
		processService.assignTask(wfTask, assigneeId);
	}
	
	/**
	 * Set assignee to a task
	 * @param taskId
	 * @param assigneeId
	 * @throws InvalidRequestException  
	 */
	@RequestMapping(value = "/task/assignee/{assigneeId}", method = RequestMethod.POST, 
			consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public void setAssigneeToTask(@PathVariable String assigneeId, @RequestParam("json") String taskData,
			@RequestParam("file") MultipartFile[] files) throws InvalidRequestException {
		
		logger.info("Assign task to " + assigneeId);
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			WfTask wfTask = mapper.readValue(taskData, WfTask.class);
			
			processService.assignTask(wfTask, assigneeId, files);
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/tasks/claim", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getClaimTasks() throws InvalidRequestException {
		
		logger.info("Requesting tasks to be claimed");
		
		return processService.getCandidateUserTasks();
	}
	
	@RequestMapping(value = "/task/{taskId}/unclaim", method = RequestMethod.DELETE)
	@ResponseBody
	public void unClaimTask(@PathVariable String taskId, HttpServletRequest reques) throws InvalidRequestException{
		
		processService.unClaimTask(taskId);
	}
	
	@RequestMapping(value = "/task/{taskId}/claim", method = RequestMethod.POST)
	@ResponseBody
	public void claimTask(@PathVariable String taskId, HttpServletRequest reques) throws InvalidRequestException{
		
		processService.claimTask(taskId);
	}
	
	@RequestMapping(value = "/task", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getAllActiveTasks(){
		
		List<WfTask> wfTasks = new ArrayList<WfTask>();
		logger.info("Requesting all active tasks");
		wfTasks = processService.getAllActiveTasks();
		return wfTasks;
	}
	
	/* 
	 * @param id
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}/document", method = RequestMethod.GET)
	@ResponseBody
	public List<WfDocument> getProcessInstanceDocuments(@PathVariable int id)
			throws InvalidRequestException {
		
		logger.info("Getting documents for process instance: " + id);
		
		return processService.getProcessInstanceDocuments(id);
	}
	
	/* 
	 * @param id
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/{id}/document", method = RequestMethod.GET)
	@ResponseBody
	public List<WfDocument> getProcessInstanceDocumentsByTask(@PathVariable int id)
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
}
