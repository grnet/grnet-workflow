package gr.cyberstream.workflow.engine.controller;

import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.*;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.util.List;

/**
 * Implements all RESTfull requests related to Process Definitions and Instances
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/api")
@MultipartConfig(fileSizeThreshold = 20971520)
public class ProcessController {

	private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private ProcessService processService;

	/**
	 * Returns all process definitions in the system
	 * 
	 * @return List of {@link WfProcess}
	 */
	@RequestMapping(value = "/process", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin','User')")
	public List<WfProcess> getProcessDefinitions() {

		return processService.getAll();
	}

	/**
	 * Returns all active process definitions
	 * 
	 * @return List of {@link WfProcess}
	 */
	@RequestMapping(value = "/process/active", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin','User')")
	public List<WfProcess> getAtiveProcessDefinitions() {

		return processService.getActiveProcessDefinitions();
	}

	/**
	 * Returns process definitions by owners
	 * 
	 * @param owners
	 *            List of owners to get Definitions from
	 * 
	 * @return List of {@link WfProcess}
	 */
	@RequestMapping(value = "/process/filter", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcess> getProcessDefinitionsByOwner(@RequestParam("owners") List<String> owners) {

		if (owners.size() > 0)
			return processService.getDefinitionsByOwners(owners);
		else
			return processService.getAll();
	}

	/**
	 * Return instances by supervisor
	 * 
	 * @return List of {@link WfProcessInstance}
	 */
	@RequestMapping(value = "/process/supervised", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getSupervisedProcesses() {

		return processService.getSupervisedInstances();
	}

	/**
	 * Returns specific process definition by id
	 * 
	 * @param id
	 *            The id of Definition
	 * 
	 * @return List of {@link WfProcessInstance}
	 */
	@RequestMapping(value = "/process/{id}", method = RequestMethod.GET)
	@ResponseBody
	public WfProcess getProcessDefinition(@PathVariable int id) {

		return processService.getProcessById(id);
	}

	/**
	 * Returns task details by version id
	 * 
	 * @param id
	 *            Version id
	 * 
	 * @return List of {@link WfTaskDetails}
	 */
	@RequestMapping(value = "/process/version/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTaskDetails> getVersionsTaskDetails(@PathVariable int id) {

		return processService.getVersionTaskDetails(id);
	}

	/**
	 * Returns active instances by definition id
	 * 
	 * @param id
	 *            Definition id
	 * 
	 * @return List of {@link WfProcessInstance}
	 */
	@RequestMapping(value = "/process/{id}/instance", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getProcessInstances(@PathVariable int id) {

		return processService.getActiveProcessInstances(id);
	}

	/**
	 * Search for ended instances by given criteria
	 * 
	 * @param title
	 *            The title of the instance
	 * 
	 * @param after
	 *            Date after which the queried instances should have ended
	 * 
	 * @param before
	 *            Date by which the queried instances should have ended
	 * 
	 * @param anonymous
	 *            Searches for user in context if true
	 * 
	 * @return List of {@link WfTask}
	 */
	@ResponseBody
	@RequestMapping(value = "/process/instance/ended/search:{title},{after},{before},{anonymous}", method = RequestMethod.GET)
	public List<WfTask> getEndedProcessInstancesTasks(@PathVariable String title, @PathVariable long after,
			@PathVariable long before, @PathVariable boolean anonymous) {

		return processService.getEndedProcessInstancesTasks(title, after, before, anonymous);
	}

	/**
	 * Get user's activity based on given criteria
	 * 
	 * @param after
	 *            Date after which the queried instances should have ended
	 * 
	 * @param before
	 *            Date by which the queried instances should have ended
	 * 
	 * @param userId
	 *            The selected user to retrieve tasks
	 * 
	 * @return List of {@link WfTask}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/task/user/search/{after},{before},{userId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUserActivity(@PathVariable long after, @PathVariable long before,
			@PathVariable String userId) throws InvalidRequestException {

		return processService.getUserActivity(after, before, userId);
	}

	/**
	 * Update task details
	 * 
	 * @param wfTaskDetails
	 *            The updated {@link WfTaskDetails} entity
	 * 
	 * @return {@link WfTaskDetails}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/taskdetails", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfTaskDetails updateTaskDetails(@RequestBody WfTaskDetails wfTaskDetails) throws InvalidRequestException {

		return processService.updateTaskDetails(wfTaskDetails);
	}

	/**
	 * Return the current system settings
	 * 
	 * @return {@link WfSettings}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	@ResponseBody
	public WfSettings getSettings() throws InvalidRequestException {

		return new WfSettings(processService.getSettings());
	}

	/**
	 * Updates the settings
	 * 
	 * @param wfSettings
	 *            The updated {@link WfSettings} entity
	 * 
	 * @return {@link WfSettings}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/settings", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public WfSettings updateSettings(@RequestBody WfSettings wfSettings) throws InvalidRequestException {
		logger.debug("Auto Assignment " + wfSettings.isAutoAssignment());
		logger.debug("Duedate Alert Period " + wfSettings.getDuedateAlertPeriod());
		logger.debug("Assignment Notification " + wfSettings.isAssignmentNotification());

		WorkflowSettings settings = processService.updateSettings(wfSettings);
		return new WfSettings(settings);
	}

	/**
	 * Sets process instance's status to "delete" and delete it from activiti
	 * 
	 * @param id
	 *            The instance's id
	 * 
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/instance/{id}", method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public void cancelProcessInstance(@PathVariable String id) throws InvalidRequestException {

		processService.cancelProcessInstance(id);
	}

	/**
	 * Deletes instance
	 * 
	 * @param id
	 *            The instance's id
	 * 
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/instance/{id}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public void deleteProcessInstance(@PathVariable String id) throws InvalidRequestException {

		processService.deleteInstance(id);
	}

	/**
	 * Either suspend or resume a process instance.
	 * 
	 * @param id
	 *            The instance's id
	 * 
	 * @param action
	 *            resume or suspend command
	 * 
	 * @return {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/instance/{id}/{action}", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessInstance modifyProcessInstanceStatus(@PathVariable String id, @PathVariable String action)
			throws InvalidRequestException {
		logger.debug(action + " action on process instance.");

		WfProcessInstance instance;

		if (action.equals("suspend"))
			instance = processService.suspendProcessInstance(id);

		else if (action.equals("resume"))
			instance = processService.resumeProcessInstance(id);

		else
			throw new InvalidRequestException("Non valid action on process instance");

		return instance;
	}

	/**
	 * Returns specific process definition by id
	 * 
	 * @param id
	 *            Definition's id
	 * 
	 * @param device
	 *            Device which metadata will be retrieved (browser/mobile)
	 * 
	 * @return {@link WfProcess}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}/form", method = RequestMethod.GET)
	@ResponseBody
	public WfProcess getProcessMetadata(@PathVariable int id,
			@RequestParam(required = false, name = "device", defaultValue = "browser") String device)
			throws InvalidRequestException {

		return processService.getProcessMetadata(id, device);
	}

	/**
	 * Deletes a Process Definition and all related versions
	 * 
	 * @param id
	 *            The definition id
	 * 
	 * @return {@link ErrorResponse}
	 * @throws InvalidRequestException
	 */
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
	 * @param processId
	 *            The process id
	 * 
	 * @param deploymentId
	 *            Deployment id of process
	 * 
	 * @return {@link WfProcess}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/{deploymentId}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcess deleteProcessDefinitionVersion(@PathVariable int processId, @PathVariable String deploymentId)
			throws InvalidRequestException {

		return processService.deleteProcessDefinitionVersion(processId, deploymentId);
	}

	/**
	 * Sets the active version for the workflow definition
	 * 
	 * @param processId
	 *            The process id
	 * 
	 * @param versionId
	 *            the version id to become active
	 * 
	 * @return {@link WfProcess}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/version/active/{versionId}", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcess setActiveVersion(@PathVariable int processId, @PathVariable int versionId)
			throws InvalidRequestException {

		return processService.setActiveVersion(processId, versionId);
	}

	/**
	 * Deactivate the version of the workflow definition
	 * 
	 * @param processId
	 *            The definition id
	 * 
	 * @param versionId
	 *            The version id to be deactivated
	 * 
	 * @return {@link WfProcessVersion}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/version/inactive/{versionId}", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessVersion deactivateVersion(@PathVariable int processId, @PathVariable int versionId)
			throws InvalidRequestException {

		return processService.deactivateVersion(processId, versionId);
	}

	/**
	 * Update process definition
	 * 
	 * @param process
	 *            The updated {@link WfProcess} entity
	 * 
	 * @return {@link WfProcess}
	 * @throws InvalidRequestException
	 */
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
	 *            The definition id
	 * 
	 * @param definitionVersion
	 *            the updated {@link WfProcessVersion} entity
	 * 
	 * @return {@link WfProcessVersion}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/version", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessVersion updateProcessDefinitionVersion(@PathVariable int processId,
			@RequestBody WfProcessVersion definitionVersion) throws InvalidRequestException {

		return processService.updateVersion(processId, definitionVersion);
	}

	/**
	 * Processes an uploaded BPMN file and creates a new ProcessDefinition entry
	 * upon successful processing
	 * 
	 * @param uploadedFileRef
	 *            The BPMN file
	 * 
	 * @return {@link WfProcess}
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
	 * Create a new version for the given process based on an uploaded BPMN file
	 * 
	 * @param id
	 *            The definition id
	 * 
	 * @param uploadedFileRef
	 *            The BPMN file
	 * 
	 * @return {@link WfProcessVersion}
	 * @throws InternalException
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessVersion createProcessVersion(@PathVariable int id,
			@RequestPart("file") MultipartFile uploadedFileRef, @RequestPart("justification") String justification)
			throws InternalException, InvalidRequestException {

		try {
			return processService.createNewProcessVersion(id, uploadedFileRef.getInputStream(),
					uploadedFileRef.getOriginalFilename(), justification);

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
	 *            The definition id
	 * 
	 * @return {@link Resource}
	 */
	@RequestMapping(value = "/process/{processId}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public Resource getProcessDiagram(@PathVariable int processId,
			@RequestParam(required = false, name = "task") String taskDefinition) {

		if (taskDefinition != null && !taskDefinition.isEmpty())
			return processService.getTaskProcessDiagram(processId, taskDefinition);
		else
			return processService.getProcessDiagram(processId);
	}

	/**
	 * Gets supervisors by process
	 * 
	 * @param processId
	 *            The definition id
	 * 
	 * @return List of {@link WfUser}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/supervisors/process/{processId}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfUser> getSupervirosByProcess(@PathVariable int processId) throws InvalidRequestException {

		return processService.getSupervisorsByProcess(processId);
	}
}