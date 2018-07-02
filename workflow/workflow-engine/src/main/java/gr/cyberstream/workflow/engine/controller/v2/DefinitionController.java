package gr.cyberstream.workflow.engine.controller.v2;

import gr.cyberstream.workflow.engine.model.FBLoginResponse;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.ErrorResponse;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfSettings;
import gr.cyberstream.workflow.engine.service.DefinitionService;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController("definitionV2Controller")
@RequestMapping(value = "/api/v2")
@MultipartConfig(fileSizeThreshold = 20971520)
public class DefinitionController {

	final static Logger logger = LoggerFactory.getLogger(DefinitionController.class);

	@Autowired
	private DefinitionService definitionService;

	/**
	 * <code>GET: /api/v2/process</code>
	 * 
	 * Returns all process definitions in the system
	 * 
	 * @return
	 */
	@RequestMapping(value = "/process", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public List<WfProcess> getProcessDefinitions(HttpServletRequest request) {

		return definitionService.getAllProcesses();
	}

	/**
	 * <code>GET: /api/v2/process/active</code>
	 * 
	 * Returns all active process definitions
	 * 
	 * @return
	 */
	@RequestMapping(value = "/process/active", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin','User')")
	public List<WfProcess> getActiveProcessDefinitions() throws InvalidRequestException {

		return definitionService.getActiveProcessDefinitions();
	}

	/**
	 * <code>GET: /api/v2/process/{id}</code>
	 * 
	 * Returns specific process definition by id
	 * 
	 * @return
	 */
	@RequestMapping(value = "/process/{id}", method = RequestMethod.GET)
	@ResponseBody
	public WfProcess getProcessDefinition(@PathVariable int id) throws InvalidRequestException {

		return definitionService.getProcessById(id);
	}

	/**
	 * <code>GET: /api/v2/process/{id}/form</code>
	 * 
	 * Returns specific process definition by id and device including the start
	 * form definition
	 * 
	 * @param id
	 *            the process id
	 * @param device
	 *            the device for which metadata will be retrieved
	 * @return {@link WfProcess}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}/form", method = RequestMethod.GET)
	@ResponseBody
	public WfProcess getProcessMetadata(@PathVariable int id,
			@RequestParam(required = false, name = "device", defaultValue = "browser") String device)
			throws InvalidRequestException {

		return definitionService.getProcessMetadata(id, device);
	}

	/**
	 * <code>GET: /api/v2/process/filter</code><br>
	 * 
	 * Get process definitions by owners.<br>
	 * Will return all available processes if param "owners" is empty
	 * 
	 * @param owners
	 * @return List of {@link WfProcess}
	 */
	@RequestMapping(value = "/process/filter", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcess> getProcessDefinitionsByOwner(@RequestParam("owners") List<String> owners) {

		if (owners.size() > 0) {
			return definitionService.getDefinitionsByOwners(owners);
		} else {
			return definitionService.getAllProcesses();
		}
	}

	/**
	 * <code>POST: /api/v2/process</code> <br>
	 * Creates a new process definition using the uploaded BPMN file
	 * 
	 * @param uploadedFile
	 *            The BPMN File used to create the new process definition
	 * 
	 * @return {@link WfProcess}
	 * 
	 * @throws InternalException
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process:{justification}", method = RequestMethod.POST)
	@ResponseBody
	public WfProcess createProcessDefinition(@RequestPart("file") MultipartFile uploadedFile,
											 @PathVariable String justification)
			throws InternalException, InvalidRequestException {

		try {
			return definitionService.createNewProcessDefinition(uploadedFile.getInputStream(),
					uploadedFile.getOriginalFilename(), justification);

		} catch (IOException e) {
			logger.error("Unable to read the BPMN input file " + uploadedFile.getOriginalFilename());
			logger.error(e.getMessage());
			throw new InternalException("There was a problem getting the input BPMN file");
		}
	}

	/**
	 * <code>PUT: /api/v2/process</code>
	 * 
	 * Update the process definition
	 * 
	 * @param process
	 *            the process definition to be updated
	 * 
	 * @return {@link WfProcess}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcess updateProcessDefinition(@RequestBody WfProcess process) throws InvalidRequestException {

		return definitionService.update(process);
	}

	/**
	 * <code>DELETE: /engine/api/v2/process/{id}</code>
	 * 
	 * Deletes a process definition and all related versions
	 * 
	 * @param id
	 *            the process id
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public ErrorResponse deleteProcessDefinition(@PathVariable int id) throws InvalidRequestException {

		ErrorResponse result = new ErrorResponse();
		result.setCode(ErrorResponse.noerror);
		result.setMessage("no error");

		definitionService.deleteProcessDefinition(id);

		return result;
	}

	/**
	 * <code>GET: /api/v2/process/{id}/diagram</code>
	 * 
	 * Return the process diagram as an image
	 * 
	 * @param id
	 *            the process id
	 * 
	 * @return
	 */
	@RequestMapping(value = "/process/{id}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public Resource getProcessDiagram(@PathVariable int id) {

		return definitionService.getProcessDiagram(id);
	}

	/**
	 * <code>POST: /api/v2/process/{id}/version</code>
	 * 
	 * Create a new version for the given process based on an uploaded BPMN file
	 * 
	 * @param id
	 *            The process id
	 * 
	 * @param uploadedFileRef
	 *            The BPMN File
	 * 
	 * @return {@link WfProcessVersion} The new process version
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{id}/version:{justification}", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessVersion createProcessVersion(@PathVariable int id,
			@RequestPart("file") MultipartFile uploadedFileRef, @PathVariable String justification)
			throws InternalException, InvalidRequestException {

		try {
			return definitionService.createNewProcessVersion(id, uploadedFileRef.getInputStream(),
					uploadedFileRef.getOriginalFilename(), justification);
		} catch (IOException e) {
			logger.error("Unable to read the BPMN input file " + uploadedFileRef.getOriginalFilename());
			logger.error(e.getMessage());

			throw new InternalException("There was a problem getting the input BPMN file");
		}
	}

	/**
	 * <code>POST: /api/v2/process/{processId}/version/{versionId}/active</code>
	 * 
	 * Activate the process definition version
	 * 
	 * @param processId
	 *            the process id
	 * @param versionId
	 *            the version id to become active
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/version/{versionId}/active", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcess setActiveVersion(@PathVariable int processId, @PathVariable int versionId)
			throws InvalidRequestException {

		return definitionService.setActiveVersion(processId, versionId);
	}

	/**
	 * <code>POST: /api/v2/process/{processId}/version/{versionId}/inactive</code>
	 * 
	 * Deactivate the process definition version
	 * 
	 * @param processId
	 *            the process id
	 * @param versionId
	 *            the version id to be deactivated
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/version/{versionId}/inactive", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessVersion deactivateVersion(@PathVariable int processId, @PathVariable int versionId)
			throws InvalidRequestException {

		return definitionService.deactivateVersion(processId, versionId);
	}

	/**
	 * <code>DELETE: /api/v2/process/version/{processId}/{deploymentId}</code>
	 * 
	 * Deletes a process definition version
	 *
	 * @param processId
	 *            the process id
	 *
	 * @param deploymentId
	 *            the deployment id
	 *
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/version/{processId}/{deploymentId}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcess deleteProcessDefinitionVersion(@PathVariable int processId, @PathVariable String deploymentId)
			throws InvalidRequestException {

		return definitionService.deleteProcessDefinitionVersion(processId, deploymentId);
	}

	/**
	 * <code>GET: /api/v2/settings</code>
	 * 
	 * Return the current system settings
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	@ResponseBody
	public WfSettings getSettings() throws InvalidRequestException {

		WorkflowSettings settings = definitionService.getSettings();
		return new WfSettings(settings);
	}

	/**
	 * <code>PUT: /api/v2/settings</code>
	 * 
	 * Updates the settings
	 * 
	 * @param wfSettings
	 *            Updated settings to be persisted
	 *            
	 * @return {@link WfSettings}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/settings", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public WfSettings updateSettings(@RequestBody WfSettings wfSettings) throws InvalidRequestException {

		WorkflowSettings settings = definitionService.updateSettings(wfSettings);
		return new WfSettings(settings);
	}

	/**
	 * <code>POST: /api/v2/facebook</code>
	 * 
	 * Obtain the facebook permanent token for a page
	 * 
	 * @param fbResponse
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/facebook", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public boolean claimFacebookToken(@RequestBody FBLoginResponse fbResponse) throws InvalidRequestException {

		logger.info("accessToken:: " + fbResponse.getAccessToken() + ", userID:: " + fbResponse.getUserID());
		return definitionService.claimPermanentAccessToken(fbResponse);
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
	public ErrorResponse handleInternalError(HttpServletRequest req, InternalException exception) {
		logger.error("Request: " + req.getRequestURL() + " raised " + exception);
		return exception.getError();
	}
}
