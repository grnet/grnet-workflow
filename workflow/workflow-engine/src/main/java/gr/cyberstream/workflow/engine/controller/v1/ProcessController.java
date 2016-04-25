/**
 * @author nlyk
 */

package gr.cyberstream.workflow.engine.controller.v1;

import java.io.IOException;
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

import gr.cyberstream.workflow.engine.model.FBLoginResponse;
import gr.cyberstream.workflow.engine.model.Registry;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.ErrorResponse;
import gr.cyberstream.workflow.engine.model.api.WfPublicForm;
import gr.cyberstream.workflow.engine.model.api.WfPublicService;
import gr.cyberstream.workflow.engine.model.api.WfPublicGroup;
import gr.cyberstream.workflow.engine.model.api.WfPublicWrapper;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfProcessMetadata;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfSettings;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;
import gr.cyberstream.workflow.engine.model.api.WfUser;
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
	@Deprecated
	@RequestMapping(value = "/process", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin','User')")
	public List<WfProcess> getProcessDefinitions() {
		
		return processService.getAll();
	}
	
	/**
	 * Returns all active process definitions
	 * 
	 * @param request
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/process/active", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin','User')")
	public List<WfProcess> getAtiveProcessDefinitions() throws InvalidRequestException {
		
		return processService.getActiveProcessDefinitions();
	}

	/**
	 * Returns process definitions by owners
	 * 
	 * @param request
	 * @return definitionsByOwner
	 */
	@Deprecated
	@RequestMapping(value = "/process/filter", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcess> getProcessDefinitionsByOwner(@RequestParam("owners") List<String> owners) {

		if (owners.size() > 0 ) {
			return processService.getDefinitionsByOwners(owners);
		} else {
			return processService.getAll();
		}
	}

	/**
	 * Return instances by supervisor
	 * 
	 * @param request
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/process/supervised", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getSupervisedProcesses() {
		
		return processService.getSupervisedInstances();
	}


	/**
	 * Returns specific process definition by id
	 * 
	 * @param request
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/process/{id}", method = RequestMethod.GET)
	@ResponseBody
	public WfProcess getProcessDefinition(@PathVariable int id) 
			throws InvalidRequestException{
		return processService.getProcessById(id);
	}

	/**
	 * 
	 */
	@Deprecated
	@RequestMapping(value = "/process/version/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTaskDetails> getVersionsTaskDetails(@PathVariable int id) {
		return processService.getVersionTaskDetails(id);
	}

	/**
	 * Returns instances of process specified by its id.
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/process/{id}/instance", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getProcessInstances(@PathVariable int id) {
		return processService.getActiveProcessInstances(id);
	}

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
	@Deprecated
	@RequestMapping(value = "/process/instance/ended/search:{title:.+},{after:\\d+},{before:\\d+},{anonymous:.+}", method = RequestMethod.GET)
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
	 * Request all tasks for the specified user
	 * 
	 * @param userId
	 * @return
	 * @throws InvalidRequestException 
	 */
	@Deprecated
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
	 * Update the UserTaskDetails entity with the specified id 
	 * 
	 * @param id
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/process/taskdetails", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfTaskDetails updateTaskDetails(@RequestBody WfTaskDetails wfTaskDetails) throws InvalidRequestException{
		
		return processService.updateTaskDetails(wfTaskDetails);
	}

	/**
	 * Return the current system settings
	 * 
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	@ResponseBody
	public WfSettings getSettings()
			throws InvalidRequestException {
		
		WorkflowSettings settings = processService.getSettings();		
		return new WfSettings(settings);
	}
	
	/**
	 * Updates the settings
	 * 
	 * @param settings
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/settings", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public WfSettings updateSettings(@RequestBody WfSettings wfSettings) throws InvalidRequestException {

		logger.info("Auto Assignment::" + wfSettings.isAutoAssignment());
		logger.info("Duedate Alert Period::" + wfSettings.getDuedateAlertPeriod());
		logger.info("Assignment Notification::" + wfSettings.isAssignmentNotification());
		
		WorkflowSettings settings = processService.updateSettings(wfSettings);
		return new WfSettings(settings);
	}
	
	/**
	 * Return external forms for specific process
	 * 
	 * @param id
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/process/{id}/externalform", method = RequestMethod.GET)
	@ResponseBody
	public List<WfPublicForm> getProcessExternalForms(@PathVariable int id)
			throws InvalidRequestException {
		
		List<WfPublicForm> wfForms = processService.getProcessExternalForms(id);
		
		return wfForms;
	}
	
	/**
	 * Create external form
	 * 
	 * @param xform
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/externalform", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfPublicForm createExternalForm(@RequestBody WfPublicForm wfXform) throws InvalidRequestException {

		return processService.createExternalForm(wfXform);
	}
	
	/**
	 * Update external form
	 * 
	 * @param wfXform
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/externalform/update", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfPublicForm updateExternalForm(@RequestBody WfPublicForm wfXform) throws InvalidRequestException {

		return processService.updateExternalForm(wfXform);
	}
	
	/**
	 * Delete external form
	 * 
	 * @param id
	 * @param request
	 * @throws InvalidRequestException 
	 */
	@Deprecated
	@RequestMapping(value = "/externalform/{id}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public void deleteExternalForm(@PathVariable String id) throws InvalidRequestException{
		
		logger.info("Delete external form " + id);

		processService.deleteExternalForm(id);
	}
	
	/**
	 * Return available registries
	 * 
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/registry", method = RequestMethod.GET)
	@ResponseBody
	public List<Registry> getRegistries()
			throws InvalidRequestException {
		
		List<Registry> registries = processService.getRegistries();		
		return registries;
	}
	
	
	/**
	 * Obtain the facebook permanent token for a page
	 * 
	 * @param fbResponse
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/facebook", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public boolean claimFacebookToken(@RequestBody FBLoginResponse fbResponse) throws InvalidRequestException {
		logger.info("accessToken:: " + fbResponse.getAccessToken() + ", userID:: " + fbResponse.getUserID());
		return processService.claimPermanentAccessToken(fbResponse);
		//return fbResponse;
	}
	
	
	/**
	 * Updates registry
	 * 
	 * @param registryId
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/registry", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public void updateRegistry(@RequestBody Registry registry) 
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
	@Deprecated
	@RequestMapping(value = "/registry", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public void createRegistry(@RequestBody Registry registry) 
			throws InvalidRequestException{
		
		processService.createRegistry(registry);
	}
	
	/**
	 * 
	 * @param registry
	 * @param request
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/registry/{registryId}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public void deleteRegistry(@PathVariable String registryId) 
			throws InvalidRequestException{
		
		processService.deleteRegistry(registryId);
	}
	
	/**
	 * Cancel a process instance...equivalent to deleting the instance.
	 * 
	 * @param id
	 * @throws InvalidRequestException 
	 */
	@Deprecated
	@RequestMapping(value = "/process/instance/{id}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public void cancelProcessInstance(@PathVariable String id) throws InvalidRequestException{
		logger.info("Cancel (ie delete) process instance.");
		processService.cancelProcessInstance(id);
	}
	
	/**
	 * Either suspend or resume a process instance.
	 * 
	 * @param id
	 * @param action
	 * @return
	 * @throws InvalidRequestException 
	 */
	@Deprecated
	@RequestMapping(value = "/process/instance/{id}/{action}", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfProcessInstance modifyProcessInstanceStatus(@PathVariable String id, @PathVariable String action) throws InvalidRequestException{
		
		logger.info(action + " action on process instance.");
		
		WfProcessInstance instance;
		
		if(action.equals("suspend"))
			instance = processService.suspendProcessInstance(id);
		else if(action.equals("resume"))
			instance = processService.resumeProcessInstance(id);
		else 
			throw new InvalidRequestException("Non valid action on process instance");
				
		return instance;
	}
	
	/**
	 * Suspend / Resume an external form
	 * 
	 * @param id
	 * @param action
	 * @return
	 * @throws InvalidRequestException 
	 */
	@Deprecated
	@RequestMapping(value = "/externalform/{id}/{action}", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfPublicForm modifyExternalFormStatus(@PathVariable String id, @PathVariable String action) 
			throws InvalidRequestException{
		
		logger.info(action + " action on external form.");
		
		WfPublicForm wfXForm;
		
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
	@Deprecated
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
	@Deprecated
	@RequestMapping(value = "/public/process/form/{id}", method = RequestMethod.GET)
	public WfProcessMetadata getPublicProcessMetadata(@PathVariable String id)
			throws InvalidRequestException {
		
		logger.info("Get Process external form " + id);
		
		return processService.getPublicProcessMetadata(id);
	}
	
	/**
	 * Returns all available external services
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/public/process/external", method = RequestMethod.GET)
	@ResponseBody
	public List<WfPublicService> getExternalServices() {
		
		return processService.getExternalServices();
	}

	/**
	 * Deletes a Process Definition and all related versions
	 * 
	 * @param id
	 *            the process id
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@RequestMapping(value = "/process/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public ErrorResponse deleteProcessDefinition(@PathVariable int id) throws InvalidRequestException {

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
	@Deprecated
	@RequestMapping(value = "/process/{processId}/{deploymentId}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcess deleteProcessDefinitionVersion(@PathVariable int processId,
			@PathVariable String deploymentId) throws InvalidRequestException {

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
	@Deprecated
	@RequestMapping(value = "/process/{processId}/version/active/{versionId}", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcess setActiveVersion(@PathVariable int processId, @PathVariable int versionId) throws InvalidRequestException {

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
	@Deprecated
	@RequestMapping(value = "/process/{processId}/version/inactive/{versionId}", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessVersion deactivateVersion(@PathVariable int processId, @PathVariable int versionId) throws InvalidRequestException {

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
	@Deprecated
	@RequestMapping(value = "/process", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfProcess updateProcessDefinition(@RequestBody WfProcess process) throws InvalidRequestException {

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
	@Deprecated
	@RequestMapping(value = "/process/{processId}/version", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessVersion updateProcessDefinitionVersion(@PathVariable int processId,
			@RequestBody WfProcessVersion definitionVersion)
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
	@RequestMapping(value = "/process/{id}", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
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
	@Deprecated
	@RequestMapping(value = "/process/{processId}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public Resource getProcessDiagram(@PathVariable int processId) {

		return processService.getProcessDiagram(processId);
	}
	
	/**
	 * Returns all available external forms ordered by group
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/external/groups/forms/wrapped", method = RequestMethod.GET)
	@ResponseBody
	public List<WfPublicWrapper> getWrappedGroupsForms() {
		
		return WfPublicWrapper.fromExternalWrappers(processService.getExternalWrapper());
	}
	
	/**
	 * Returns external groups
	 * 
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/external/groups", method = RequestMethod.GET)
	@ResponseBody
	public List<WfPublicGroup> getExternalGroups() {
		
		return processService.getExternalGroups();
	}
	
	
	/**
	 * Creates a new external group
	 * 
	 * @param wfExternalGroup
	 */
	@Deprecated
	@RequestMapping(value = "/external/group/create", method = RequestMethod.PUT)
	public void createExternalGroup(@RequestBody WfPublicGroup wfExternalGroup) {
		
		processService.createExternalGroup(wfExternalGroup);
	}
	
	/**
	 * Exists in api v2 too
	 * 
	 * Deletes a group
	 * 
	 * @param groupId
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/form/delete/group/{groupId}", method = RequestMethod.DELETE)
	public void deletePublicGroup(@PathVariable int groupId) throws InvalidRequestException {
		
		processService.deletePublicGroup(groupId);
	}
	
	/**
	 * Exists in api v2 too
	 * 
	 * Updates a group
	 * 
	 * @param publicGroup
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/form/update/group", method = RequestMethod.PUT)
	public void updatePublicGroup(@RequestBody WfPublicGroup publicGroup) throws InvalidRequestException {
		
		processService.updatePublicGroup(publicGroup);
	}
	
	/**
	 * Gets supervisors by process
	 * 
	 * @param processId
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/supervisors/process/{processId}", method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public List<WfUser> getSupervirosByProcess(@PathVariable int processId) throws InvalidRequestException {
		
		
		return processService.getSupervisorsByProcess(processId);
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
