/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.controller.v1;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import gr.cyberstream.workflow.engine.model.api.WfDocument;
import gr.cyberstream.workflow.engine.model.api.WfExternalUser;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfProcessStatus;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.service.CustomException;
import gr.cyberstream.workflow.engine.service.InternalException;
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

	@Autowired
	private ProcessService processService;
	
	private static final Logger logger = LoggerFactory.getLogger(ExecutionController.class);

	/**
	 * Starts a new process instance using form data
	 * 
	 * @param processId
	 * @param instanceData
	 * @return
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Deprecated
	@RequestMapping(value = "/process/{processId}/start", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Admin','ProcessAdmin')") 
	public WfProcessInstance startProcess(@PathVariable int processId, @RequestBody WfProcessInstance instanceData) throws InvalidRequestException, InternalException {
		
		logger.info("Start process: " + processId);
		
		instanceData.setClient("WORKSPACE");
		return processService.startProcess(processId, instanceData);
	}

	/**
	 * Starts a new process instance using form data and files
	 * 
	 * @param id external form id
	 * @param instanceData the form data in key-value pairs
	 * @param files
	 * @return
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Deprecated
	@RequestMapping(value = "/public/process/form/{id}/document/start", method = RequestMethod.POST, consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public WfProcessInstance startPublicProcessWithDocuments(@PathVariable String id, @RequestParam("json") String instanceData,
			@RequestParam("file") MultipartFile[] files) throws InvalidRequestException, InternalException {
		
		logger.info("Start process using form: " + id);
		
		ObjectMapper mapper = new ObjectMapper();
		
		WfProcessInstance wfProcessInstance;
		
		try {
			wfProcessInstance = mapper.readValue(instanceData, WfProcessInstance.class);
			wfProcessInstance.setClient("BROWSER");
			
			if (wfProcessInstance.getCaptchaAnswer() == null || wfProcessInstance.getCaptchaAnswer().isEmpty())
				throw new InvalidRequestException("Captcha answer is null or empty."); 
			
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
	 * Starts external instance from mobile client.
	 * 
	 * @param id
	 * @param instanceData
	 * @param files
	 * @return
	 * @throws InternalException 
	 * @throws InvalidRequestException 
	 * @throws Exception 
	 */
	@Deprecated
	@RequestMapping(value = "/public/mobile/process/form/{id}/document/start", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseBody
	public WfProcessInstance startPublicMobileProcessWithDocuments(@PathVariable String id,
			@RequestParam("json") String instanceData, @RequestParam("file") MultipartFile[] files)
			throws InvalidRequestException, InternalException {

		logger.info("Start process using form: " + id);

		ObjectMapper mapper = new ObjectMapper();

		WfProcessInstance wfProcessInstance;

		try {
			wfProcessInstance = mapper.readValue(instanceData, WfProcessInstance.class);
			wfProcessInstance.setClient("MOBILE");

			return processService.startPublicMobileProcess(id, wfProcessInstance, files);

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
	 * Returns the status by the reference id
	 * 
	 * @param referenceId
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/public/process/instance/status", method = RequestMethod.GET)
	@ResponseBody
	public WfProcessStatus getProcessStatus(@RequestParam("referenceId") String referenceId) throws InvalidRequestException {
		
		return processService.getProcessStatusByReferenceId(referenceId);
	}
	
	/**
	 * Starts a new process instance using form data
	 * 
	 * @param id external form id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws InternalException 
	 * @throws CustomException 
	 */
	@Deprecated
	@RequestMapping(value = "/public/process/form/{id}/start", method = RequestMethod.POST)
	@ResponseBody
	public WfProcessInstance startPublicProcess(@PathVariable String id, @RequestBody WfProcessInstance instanceData) throws InvalidRequestException, InternalException {
		
		logger.info("Start process using form: " + id);
		
		if (instanceData.getCaptchaAnswer() == null || instanceData.getCaptchaAnswer().isEmpty())
			throw new InvalidRequestException("Captcha answer is null or empty."); 
		
		instanceData.setClient("BROWSER");
		return processService.startPublicProcess(id, instanceData);
	}
	
	/**
	 * Starts a new process instance using form data used by mobile client.
	 * That means no captcha or any other security is used
	 * 
	 * @param id external form id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws InternalException 
	 * @throws CustomException 
	 */
	@Deprecated
	@RequestMapping(value = "/public/mobile/process/form/{id}/start", method = RequestMethod.POST)
	@ResponseBody
	public WfProcessInstance startPublicMobileProcess(@PathVariable String id, @RequestBody WfProcessInstance instanceData) throws InvalidRequestException, InternalException {
		
		logger.info("Start process from mobile client using form: " + id);
		
		instanceData.setClient("MOBILE");
		return processService.startPublicMobileProcess(id, instanceData);
	}

	/**
	 * Starts a new process instance using form data and files
	 * 
	 * @param processId the workflow definition id
	 * @param instanceData the form data in key-value pairs
	 * @param files
	 * @return
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Deprecated
	@RequestMapping(value = "/process/{processId}/document/start", method = RequestMethod.POST,consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	@PreAuthorize("hasAnyRole('Admin','ProcessAdmin')") 
	public WfProcessInstance startProcessWithDocuments(@PathVariable int processId, @RequestParam("json") String instanceData,
			@RequestParam("file") MultipartFile[] files) throws InvalidRequestException, InternalException {
		
		logger.info("Start process: " + processId);
		
		ObjectMapper mapper = new ObjectMapper();
		
		WfProcessInstance wfProcessInstance;
		
		try {
			wfProcessInstance = mapper.readValue(instanceData, WfProcessInstance.class);
			wfProcessInstance.setClient("WORKSPACE");
			
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
	
	@Deprecated
	@RequestMapping(value = "/task/{taskId}/candidates", method = RequestMethod.GET)
	@ResponseBody
	public List<WfUser> getCandidatesForTask(@PathVariable String taskId){
		
		logger.debug("Getting candidates for task: " + taskId);
		
		return processService.getCandidatesByTaskId(taskId);
	}
	
	/**
	 * Returns all users as candidates
	 * 
	 * @param taskId
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/task/candidates/all", method = RequestMethod.GET)
	@ResponseBody
	public List<WfUser> getAllCandidates(HttpServletRequest request){
		
		return processService.getAllCandidates();
	}
	
	
	@Deprecated
	@RequestMapping(value = "/tasks/instance/{instanceId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getTaskByInstanceId(@PathVariable String instanceId) throws InvalidRequestException{
		
		return processService.getTasksByInstanceId(instanceId);
	}
	
	/**
	 * Creates an user task form element object from form property and then
	 * saves it
	 * 
	 * @param formProperty
	 */
	@Deprecated
	@RequestMapping(value = "/process/{processDefinitionKey}/task/{taskDefintionKey}/formelement", method = RequestMethod.PUT)
	public void saveUserTaskFormElement(@PathVariable String processDefinitionKey, @PathVariable String taskDefintionKey, @RequestBody WfFormProperty formProperty) {
		
		processService.saveTaskFormElement(formProperty, taskDefintionKey, processDefinitionKey);
	}

	/**
	 * Get supervised tasks for supervisor or admin
	 * if user has role admin then all tasks returned
	 * 
	 * @param instanceIds
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/tasks/supervised", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin')") 
	public List<WfTask> getUnassingedTasksByInstancesIds() throws InvalidRequestException {
		
		return processService.getSupervisedTasks();
	}
	
	/**
	 * Returns Assigned tasks for the user in context (Logged in user)
	 * @return
	 * @throws InvalidRequestException 
	 */
	@Deprecated
	@RequestMapping(value = "/task/inprogress/user", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getTasksForUser() throws InvalidRequestException{
		
		logger.debug("Requested tasks in progress for user in contenxt");
		
		return processService.getTasksForUser();
	}
	
	/**
	 * Returns Completed tasks for the user in context (Logged in user)
	 * 
	 * @return
	 */
	@Deprecated
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
	@Deprecated
	@RequestMapping(value = "/task/{taskId}", method = RequestMethod.GET)
	@ResponseBody
	public WfTask getTask(@PathVariable String taskId) throws InvalidRequestException {
		
		return processService.getTask(taskId);
	}
	
	/**
	 * Returns a task by definition key
	 * 
	 * @param taskDefinitionKey
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/task/taskdefinition/{taskDefinitionKey}/process/{processDefinitionId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfFormProperty> getTaskFormProperties(@PathVariable String taskDefinitionKey, @PathVariable String processDefinitionId) throws InvalidRequestException {
		
		return processService.getTaskFormPropertiesByTaskDefintionKey(taskDefinitionKey, processDefinitionId);
	}
	
	/**
	 * Returns completed task
	 *  
	 * @param taskId
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
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
	@Deprecated
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
	 * @throws InvalidRequestException 
	 */
	@Deprecated
	@RequestMapping(value = "/tasks/completed/search:{definitionKey},{instanceTitle},{after},{before},{isSupervisor}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getSearchedCompletedTasks(@PathVariable String definitionKey, @PathVariable String instanceTitle,
			@PathVariable long after, @PathVariable long before, @PathVariable String isSupervisor, HttpServletRequest request) throws InvalidRequestException {

		logger.debug("Definition key " + definitionKey + " instance title " + instanceTitle + " After " + after + " before " + before);

		return processService.getSearchedCompletedTasks(definitionKey, instanceTitle, after, before, isSupervisor);
	}
	
	/**
	 * Get user's completed instances
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
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
	@Deprecated
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
	@Deprecated
	@RequestMapping(value = "/tasks/instances", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getCompletedTasksByInstances(@RequestParam("i") List<String> instanceIds){
		return processService.getCompletedTasksByInstances(instanceIds);
	}
	
	/**
	 * Completes task with given id with multipart request
	 * 
	 * @param taskId
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/task/complete", method = RequestMethod.POST, consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public void completeTask(@RequestParam("json") String taskData, @RequestParam("file") MultipartFile[] files) throws InvalidRequestException{
		
		logger.debug("Request complete task with multipart request");
		
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
	 * Completes task with given id without multipart request
	 * 
	 * @param taskId
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/task/complete", method = RequestMethod.POST)
	@ResponseBody
	public void completeTask(@RequestBody WfTask task) throws InvalidRequestException{
		
		logger.debug("Request complete task without multipart request");
		
		processService.completeTask(task);
	}

	/**
	 * Temporary saves task with document as form data
	 * 
	 * @param taskData
	 * @param files
	 * @throws InvalidRequestException
	 */
	@Deprecated
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
	
	/**
	 * Temporary saves task's form data
	 * 
	 * @param task
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/task/tempsave", method = RequestMethod.POST)
	@ResponseBody
	public void tempSaveTask(@RequestBody WfTask task) throws InvalidRequestException{
		
		processService.tempTaskSave(task);
	}
	
	@Deprecated
	@RequestMapping(value = "/process/exec/{execId}/document/{variable}", method = RequestMethod.POST, 
			consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	public WfDocument saveDocument(@PathVariable String execId, @PathVariable String variable, @RequestPart("json") WfDocument document, 
			@RequestPart("file") MultipartFile file) throws InvalidRequestException {

		logger.debug("Saving document.");
		
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
	
	@Deprecated
	@RequestMapping(value = "/process/{processId}/document/{variable}", method = RequestMethod.PUT)
	public WfDocument updateDocument(@PathVariable String execId, @PathVariable String variable, @RequestBody WfDocument document)
			throws InvalidRequestException {

		logger.debug("Updating document.");
		
		return processService.updateDocument(execId, variable, document);
	}
	
	/**
	 * Set assignee to a task
	 * 
	 * @param taskId
	 * @param assigneeId
	 * @throws InvalidRequestException 
	 */
	@Deprecated
	@RequestMapping(value = "/task/assignee/{assigneeId}", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin')") 
	public void setAssigneeToTask(@PathVariable String assigneeId, @RequestBody WfTask wfTask) throws InvalidRequestException{
		
		logger.debug("Set assignee with mail " + assigneeId + " to task with id " + wfTask.getId() );
		
		processService.assignTask(wfTask, assigneeId);
	}
	
	/**
	 * Set assignee to a task with file
	 * 
	 * @param taskId
	 * @param assigneeId
	 * @throws InvalidRequestException  
	 */
	@Deprecated
	@RequestMapping(value = "/task/assignee/{assigneeId}", method = RequestMethod.POST, consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin')") 
	public void setAssigneeToTask(@PathVariable String assigneeId, @RequestParam("json") String taskData,
			@RequestParam("file") MultipartFile[] files) throws InvalidRequestException {
		
		logger.debug("Assign task to " + assigneeId + " with " + "file");
		
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
	
	@Deprecated
	@RequestMapping(value = "/tasks/claim", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getClaimTasks() throws InvalidRequestException {
		
		logger.debug("Requesting tasks to be claimed");
		
		return processService.getCandidateUserTasks();
	}
	
	/**
	 * Removes assignee from a task
	 * 
	 * @param taskId
	 * @param reques
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/task/{taskId}/unclaim", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Supervisor','Admin','User')") 
	public void unClaimTask(@PathVariable String taskId) throws InvalidRequestException{
		
		processService.unClaimTask(taskId);
	}
	
	/**
	 * Set assignee to a task
	 * 
	 * @param taskId
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/task/{taskId}/claim", method = RequestMethod.POST)
	@ResponseBody
	public void claimTask(@PathVariable String taskId) throws InvalidRequestException{
		
		processService.claimTask(taskId);
	}
	
	@Deprecated
	@RequestMapping(value = "/task", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getAllActiveTasks(){
		
		List<WfTask> wfTasks = new ArrayList<WfTask>();
		logger.debug("Requesting all active tasks");
		wfTasks = processService.getAllActiveTasks();
		return wfTasks;
	}
	
	/* 
	 * @param id
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/process/{id}/document", method = RequestMethod.GET)
	@ResponseBody
	public List<WfDocument> getProcessInstanceDocuments(@PathVariable int id)
			throws InvalidRequestException {
		
		logger.debug("Getting documents for process instance: " + id);
		
		return processService.getProcessInstanceDocuments(id);
	}
	
	/* 
	 * @param id
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/task/{id}/document", method = RequestMethod.GET)
	@ResponseBody
	public List<WfDocument> getProcessInstanceDocumentsByTask(@PathVariable int id)
			throws InvalidRequestException {
		
		logger.debug("Getting documents for task: " + id);
		
		return processService.getProcessInstanceDocumentsByTask(id);
	}
	
	/**
	 * Returns instance's documents
	 * 
	 * @param instanceId
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/instance/{instanceId}/documents", method = RequestMethod.GET)
	@ResponseBody
	public List<WfDocument> getDocumentsByInstanceId(@PathVariable String instanceId) throws InvalidRequestException {
		
		return processService.getDocumentsByInstance(instanceId);
	}
	
	/**
	 * Returns the start event form by instance id
	 *  
	 * @param instanceId
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/instance/{instanceId}/startform", method = RequestMethod.GET)
	@ResponseBody
	public List<WfFormProperty> getStartForm(@PathVariable String instanceId) throws InvalidRequestException {
		
		return processService.getStartFormByInstanceId(instanceId);
	}
	
	/**
	 * Returns instance by id.
	 * Used by mobile client.
	 * 
	 * @param instanceId
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/public/instance/{instanceId}", method = RequestMethod.GET)
	@ResponseBody
	public WfProcessInstance getInstanceById(@PathVariable String instanceId) throws InvalidRequestException {

		return processService.getProcessInstanceById(instanceId);
	}
	
	/**
	 * Returns the progress of an instance as image
	 * 
	 * @param processId
	 *            the process id
	 * @param request
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/instance/{instanceId}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public Resource getProcessDiagram(@PathVariable String instanceId) {

		return processService.getInstanceProgressDiagram(instanceId);
	}
	
	/**
	 * Deletes a process instance
	 * @param instanceId
	 */
	@Deprecated
	@RequestMapping(value = "/delete/completed/instance/{instanceId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('Admin')")
	public void deleteProcessCompletedInstance(@PathVariable String instanceId) {
		
		processService.deleteProcessCompletedInstance(instanceId);
	}
	
	/**
	 * A list of all in progress instances
	 * 
	 * @return a list of all in progress instances
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/inprogress/instances", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getInProgressInstances() throws InvalidRequestException {
		
		return processService.getInProgressInstances();
	}
	
	/**
	 * Changes instance's supervisor
	 * 
	 * @param instanceId
	 * @param supervisor
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/instance/{instanceId}/supervisor", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	@Deprecated
	public void changeInstanceSupervisor(@PathVariable String instanceId, 
			@RequestParam("supervisor") String supervisor) throws InvalidRequestException {
		
		processService.changeInstanceSupervisor(instanceId, supervisor);
	}
	
	/**
	 * Request to check if server is online
	 * 
	 * V2 PublicFormController
	 * @throws InvalidRequestException 
	 * 
	 */
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/getstatus", method = RequestMethod.GET)
	public void getServerStatus() {
		
	}
	
	/**
	 * 
	 * @param wfMobileUser
	 */
	@RequestMapping(value = "/public/mobile/user/preferences", method = RequestMethod.POST)
	public void saveMobileUser(@RequestBody WfExternalUser wfExternalUser) {
		
		processService.saveExternalUser(wfExternalUser);
	}
	
	@RequestMapping(value = "/task/{taskId}/notification", method = RequestMethod.POST)
	public void sendDueDateNotificationEmail(@PathVariable String taskId, @RequestBody String content) throws InternalException {
		
		processService.sendTaskDueDateNotification(taskId, content);
		
	}
}