/**
 * @author nlyk
 */

package gr.cyberstream.workflow.engine.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gr.cyberstream.workflow.engine.model.Registry;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.ErrorResponse;
import gr.cyberstream.workflow.engine.model.api.WfExternalForm;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfProcessMetadata;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.ProcessService;

/**
 * Implements all RESTfull requests related to Process Definitions and Instances
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/api")
@MultipartConfig(fileSizeThreshold = 20971520)
public class ProcessController {

	final static Logger logger = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private ProcessService processService;

	/**
	 * Returns all process definitions in the system
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/process", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcess> getProcessDefinitions(HttpServletRequest request) {

		return processService.getAll();
	}

	/**
	 * Returns process definitions by owners
	 * 
	 * @param request
	 * @return definitionsByOwner
	 */
	@RequestMapping(value = "/process/filter/{filter}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcess> getProcessDefinitionsByOwner(@PathVariable String filter,
			@MatrixVariable(required = false) String owners[], HttpServletRequest request) {

		if (filter.equals("all")) {
			return processService.getProcessDefinitions();
		} else {
			return processService.getDefinitionsByOwners(Arrays.asList(owners));
		}
	}

	/**
	 * Return instances by supervisor
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/process/supervised", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getSupervisedProcesses(HttpServletRequest request) {
		
		return processService.getSupervisedInstances();
	}


	/**
	 * Returns specific process definition by id
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/process/{id}", method = RequestMethod.GET)
	@ResponseBody
	public WfProcess getProcessDefinition(@PathVariable int id, HttpServletRequest request) 
			throws InvalidRequestException{
		return processService.getProcessById(id);
	}

	// TODO:vpap
	/**
	 * 
	 */
	@RequestMapping(value = "/process/version/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTaskDetails> getVersionsTaskDetails(@PathVariable int id, HttpServletRequest request) {
		return processService.getVersionTaskDetails(id);
	}


	//TODO:vpap
	/**
	 * Returns instances of process specified by its id.
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/process/{id}/instance", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getProcessInstances(@PathVariable int id, HttpServletRequest request) {
		return processService.getActiveProcessInstances(id);
	}

	//TODO:vpap
	/**
	 * Search ended instances
	 * 
	 * @param title The title of the instance
	 * @param after Date after which the queried instances should have ended.
	 * @param before Date by which the queried instances should have ended.
	 * @param anonymous true: no assignee
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/process/instance/ended/search:{title:.+},{after:\\d+},{before:\\d+},{anonymous:.+}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getEndedProcessInstancesTasks(@PathVariable String title, @PathVariable long after, 
			@PathVariable long before, @PathVariable boolean anonymous, HttpServletRequest request) {
		
		System.out.println("instance title::"+title);
		System.out.println("after::"+after);
		System.out.println("before::"+before);
		System.out.println("anonymous::"+anonymous);
		
		List<WfTask> wfTasks = processService.getEndedProcessInstancesTasks(title, after, before, anonymous);
		
		return wfTasks;
	}	
	
	
	//TODO:vpap
	/**
	 * Request all tasks for the specified user
	 * 
	 * @param userId
	 * @return
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/task/search:{after:\\d+},{before:\\d+}/assignee/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserActivity(@PathVariable long after, @PathVariable long before, 
			@PathVariable String userId) throws InvalidRequestException{
		
		logger.info("Request all tasks for the specified user.");
		
		System.out.println("after::"+after);
		System.out.println("before::"+before);
		System.out.println("user id::"+userId);
		
		List<WfTask> wfTasks = processService.getUserActivity(after, before, userId);
		
		return wfTasks;
	}
	
	
	//TODO:vpap
	/**
	 * Update the UserTaskDetails entity with the specified id 
	 * 
	 * @param id
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/taskdetails", method = RequestMethod.PUT)
	@ResponseBody
	public WfTaskDetails updateTaskDetails(@RequestBody WfTaskDetails wfTaskDetails, HttpServletRequest request) 
			throws InvalidRequestException{
		WfTaskDetails updatedTask = new WfTaskDetails();
		updatedTask = processService.updateTaskDetails(wfTaskDetails);
		return updatedTask;
	}

	//TODO::vpap
	/**
	 * Return the current settings
	 * 
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	@ResponseBody
	public WorkflowSettings getSettings(HttpServletRequest request)
			throws InvalidRequestException {
		
		WorkflowSettings settings = processService.getSettings();
		
		return settings;
	}
	
	//TOOD:vpap
	/**
	 * Updates the settings
	 * 
	 * @param settings
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/settings", method = RequestMethod.PUT)
	@ResponseBody
	public WorkflowSettings updateSettings(@RequestBody WorkflowSettings settings, 
			HttpServletRequest request) throws InvalidRequestException {

		System.out.println("Auto Assignment::" + settings.isAutoAssignment());
		System.out.println("Duedate Alert Period::" + settings.getDuedateAlertPeriod());
		System.out.println("Assignment Notification::" + settings.isAssignmentNotification());
		
		return processService.updateSettings(settings);
	}
	
	
	//TODO:vpap
	/**
	 * Return external forms for specific process
	 * 
	 * @param id
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}/externalform", method = RequestMethod.GET)
	@ResponseBody
	public List<WfExternalForm> getProcessExternalForms(@PathVariable int id, HttpServletRequest request)
			throws InvalidRequestException {
		
		List<WfExternalForm> wfForms = processService.getProcessExternalForms(id);
		
		return wfForms;
	}
	
	
	//TODO:vpap
	/**
	 * Create external form
	 * 
	 * @param xform
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/externalform", method = RequestMethod.POST)
	@ResponseBody
	public WfExternalForm createExternalForm(@RequestBody WfExternalForm wfXform)
			throws InvalidRequestException {

		return processService.createExternalForm(wfXform);
	}
	
	/**
	 * Update external form
	 * 
	 * @param wfXform
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/externalform/update", method = RequestMethod.POST)
	@ResponseBody
	public WfExternalForm updateExternalForm(@RequestBody WfExternalForm wfXform) throws InvalidRequestException {

		return processService.updateExternalForm(wfXform);
	}
	
	
	//TODO:vpap
	/**
	 * Delete external form
	 * 
	 * @param id
	 * @param request
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/externalform/{id}", method = RequestMethod.DELETE)
	public void deleteExternalForm(@PathVariable String id, HttpServletRequest request) throws InvalidRequestException{
		
		System.out.println("Delete external form " + id);

		processService.deleteExternalForm(id);
	}
	
	
	//TODO:vpap
	/**
	 * Return available registries
	 * 
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/registry", method = RequestMethod.GET)
	@ResponseBody
	public List<Registry> getRegistries(HttpServletRequest request)
			throws InvalidRequestException {
		
		List<Registry> registries = processService.getRegistries();		
		return registries;
	}
	
	/**
	 * Updates registry
	 * 
	 * @param registryId
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/registry", method = RequestMethod.PUT)
	@ResponseBody
	public void updateRegistry(@RequestBody Registry registry, HttpServletRequest request) 
			throws InvalidRequestException{
		
		processService.updateRegistry(registry);
	}
	
	/**
	 * Create registry
	 * 
	 * @param registryId
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/registry", method = RequestMethod.POST)
	@ResponseBody
	public void createRegistry(@RequestBody Registry registry, HttpServletRequest request) 
			throws InvalidRequestException{
		
		processService.createRegistry(registry);
	}
	
	/**
	 * 
	 * @param registry
	 * @param request
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/registry/{registryId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteRegistry(@PathVariable String registryId, HttpServletRequest request) 
			throws InvalidRequestException{
		
		processService.deleteRegistry(registryId);
	}
	
	//TODO:vpap
	/**
	 * Cancel a process instance...equivalent to deleting the instance.
	 * 
	 * @param id
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/process/instance/{id}", method = RequestMethod.DELETE)
	public void cancelProcessInstance(@PathVariable String id) throws InvalidRequestException{
		System.out.println("Cancel (ie delete) process instance.");
		processService.cancelProcessInstance(id);
	}
	
	
	//TODO:vpap
	/**
	 * Either suspend or resume a process instance.
	 * 
	 * @param id
	 * @param action
	 * @return
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/process/instance/{id}/{action}", method = RequestMethod.PUT)
	public WfProcessInstance modifyProcessInstanceStatus(@PathVariable String id, @PathVariable String action) throws InvalidRequestException{
		
		System.out.println(action + " action on process instance.");
		
		WfProcessInstance instance;
		
		if(action.equals("suspend"))	instance = processService.suspendProcessInstance(id);
		else if(action.equals("resume"))	instance = processService.resumeProcessInstance(id);
		else throw new InvalidRequestException("Non valid action on process instance");
				
		return instance;
	}
	
	//TODO:vpap
	/**
	 * Suspend / Resume an external form
	 * 
	 * @param id
	 * @param action
	 * @return
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/externalform/{id}/{action}", method = RequestMethod.PUT)
	public WfExternalForm modifyExternalFormStatus(@PathVariable String id, @PathVariable String action) 
			throws InvalidRequestException{
		
		System.out.println(action + " action on external form.");
		
		WfExternalForm wfXForm;
		
		if(action.equals("suspend"))	wfXForm = processService.modifyExternalFormStatus(id, false);
		else if(action.equals("resume"))	wfXForm = processService.modifyExternalFormStatus(id, true);
		else throw new InvalidRequestException("Non valid action on external form");
				
		return wfXForm;
	}

	
	/**
	 * Returns specific process definition by id
	 * 
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}/form", method = RequestMethod.GET)
	@ResponseBody
	public WfProcess getProcessMetadata(@PathVariable int id)
			throws InvalidRequestException {
		
		return processService.getProcessMetadata(id);
	}
	
	/**
	 * Returns process metadata and start form for the 
	 * specfied external form id
	 * 
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/public/process/form/{id}", method = RequestMethod.GET)
	public WfProcessMetadata getPublicProcessMetadata(@PathVariable String id)
			throws InvalidRequestException {
		
		logger.info("Get Process external form " + id);
		
		return processService.getPublicProcessMetadata(id);
	}

	/**
	 * Deletes a Process Definition and all related versions
	 * 
	 * @param id
	 *            the process id
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ErrorResponse deleteProcessDefinition(@PathVariable int id, HttpServletRequest request)
			throws InvalidRequestException {

		ErrorResponse result = new ErrorResponse();
		result.setCode(ErrorResponse.noerror);
		result.setMessage("no error");

		processService.deleteProcessDefinition(id);

		return result;
	}

	/**
	 * Deletes a Process Definition version
	 * 
	 * @param id
	 *            the process id
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/{deploymentId}", method = RequestMethod.DELETE)
	@ResponseBody
	public WfProcess deleteProcessDefinitionVersion(@PathVariable int processId,
			@PathVariable String deploymentId, HttpServletRequest request) throws InvalidRequestException {

		return processService.deleteProcessDefinitionVersion(processId, deploymentId);
	}

	/**
	 * Sets the active version for the workflow definition
	 * 
	 * @param processId
	 *            the process id
	 * @param versionId
	 *            the version id to become active
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/version/active/{versionId}", method = RequestMethod.PUT)
	@ResponseBody
	public WfProcess setActiveVersion(@PathVariable int processId, @PathVariable int versionId,
			HttpServletRequest request) throws InvalidRequestException {

		return processService.setActiveVersion(processId, versionId);
	}

	/**
	 * Deactivate the version of the workflow definition
	 * 
	 * @param processId
	 *            the process id
	 * @param versionId
	 *            the version id to be deactivated
	 * @param request
	 * @return the modified workflow definition version
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/version/inactive/{versionId}", method = RequestMethod.PUT)
	@ResponseBody
	public WfProcessVersion deactivateVersion(@PathVariable int processId, @PathVariable int versionId,
			HttpServletRequest request) throws InvalidRequestException {

		return processService.deactivateVersion(processId, versionId);
	}

	/**
	 * Updated the process definition
	 * 
	 * @param definition
	 *            the workflow definition object to be updated
	 * @param request
	 * @return the updated workflow definition object
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process", method = RequestMethod.PUT)
	@ResponseBody
	public WfProcess updateProcessDefinition(@RequestBody WfProcess process,
			HttpServletRequest request) throws InvalidRequestException {

		return processService.update(process);
	}

	/**
	 * Updates the process definition version
	 * 
	 * @param processId
	 *            the process id
	 * @param deploymentId
	 *            the deployment id of the specific version
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/version", method = RequestMethod.PUT)
	@ResponseBody
	public WfProcessVersion updateProcessDefinitionVersion(@PathVariable int processId,
			@RequestBody WfProcessVersion definitionVersion, HttpServletRequest request)
					throws InvalidRequestException {

		return processService.updateVersion(processId, definitionVersion);
	}

	/**
	 * Processes an uploaded BPMN file and creates a new ProcessDefinition entry
	 * upon successful processing
	 * 
	 * @param uploadedFileRef
	 * @return the newly created process definition
	 * @throws InternalException
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/processbpmn", method = RequestMethod.POST)
	@ResponseBody
	public WfProcess createProcessDefinition(@RequestPart("file") MultipartFile uploadedFileRef)
			throws InternalException, InvalidRequestException {

		try {

			return processService.createNewProcessDefinition(uploadedFileRef.getInputStream(),
					uploadedFileRef.getOriginalFilename());
		} catch (IOException e) {
			logger.error("Unable to read the BPMN input file " + uploadedFileRef.getOriginalFilename());
			logger.error(e.getMessage());

			throw new InternalException("There was a problem getting the input BPMN file");
		}
	}

	/**
	 * Creates a new process definition in the system. This requests is not of
	 * common use. Normally a new process definition is created uploading a new
	 * BPMN file to the system.
	 * 
	 * @param process
	 *            the new process definition to be added to the system
	 * @return the process definition updated
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process", method = RequestMethod.POST)
	@ResponseBody
	public WfProcess createProcessDefinition(@RequestBody WfProcess process)
			throws InvalidRequestException {

		return processService.createNewProcessDefinition(process);
	}

	/**
	 * Create a new version for the given process based on an uploaded BPMN file
	 * 
	 * @param id
	 *            the process id
	 * @param uploadedFileRef
	 * @return the new process version
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}", method = RequestMethod.POST)
	@ResponseBody
	public WfProcessVersion createProcessVersion(@PathVariable int id,
			@RequestPart("file") MultipartFile uploadedFileRef) throws InternalException, InvalidRequestException {

		try {
			return processService.createNewProcessVersion(id, uploadedFileRef.getInputStream(),
					uploadedFileRef.getOriginalFilename());
		} catch (IOException e) {
			logger.error("Unable to read the BPMN input file " + uploadedFileRef.getOriginalFilename());
			logger.error(e.getMessage());

			throw new InternalException("There was a problem getting the input BPMN file");
		}
	}
	
	/**
	 * Return the process diagram as an image
	 * 
	 * @param processId
	 *            the process id
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/process/{processId}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public Resource getProcessDiagram(@PathVariable int processId, HttpServletRequest request) {

		return processService.getProcessDiagram(processId);
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
