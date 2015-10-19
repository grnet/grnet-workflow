/**
 * @author nlyk
 */

package gr.cyberstream.workflow.engine.controller;

import java.io.IOException;
import java.util.List;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.ErrorResponse;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.ProcessService;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

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
	private Processes processRepository;

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
	public List<WorkflowDefinition> getProcessDefinitions(HttpServletRequest request) {
		return processRepository.getAll();
	}

	/**
	 * Returns all process definitions for all processes supervised by the authenticated user
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/process/supervised", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcess> getSupervisedProcesses(HttpServletRequest request) {
		return processService.getSupervisedProcesses();
	}

	/**
	 * Returns specific process definition by id
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/process/{id}", method = RequestMethod.GET)
	@ResponseBody
	public WorkflowDefinition getProcessDefinition(@PathVariable int id, HttpServletRequest request) {
		return processRepository.getById(id);
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
	public WfProcess getProcessMetadata(@PathVariable int id, HttpServletRequest request)
			throws InvalidRequestException {
		return processService.getProcessMetadata(id);
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
	public WorkflowDefinition deleteProcessDefinitionVersion(@PathVariable int processId,
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
	public WorkflowDefinition setActiveVersion(@PathVariable int processId, @PathVariable int versionId,
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
	public DefinitionVersion deactivateVersion(@PathVariable int processId, @PathVariable int versionId,
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
	public WorkflowDefinition updateProcessDefinition(@RequestBody WorkflowDefinition definition,
			HttpServletRequest request) throws InvalidRequestException {

		return processRepository.save(definition);
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
	public DefinitionVersion updateProcessDefinitionVersion(@PathVariable int processId,
			@RequestBody DefinitionVersion definitionVersion, HttpServletRequest request)
			throws InvalidRequestException {

		return processRepository.saveVersion(processId, definitionVersion);
	}

	/**
	 * Processes an uploaded BPMN file and creates a new ProcessDefinition entry upon successful processing
	 * 
	 * @param uploadedFileRef
	 * @return the newly created process definition
	 * @throws InternalException
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/processbpmn", method = RequestMethod.POST)
	@ResponseBody
	public WorkflowDefinition createProcessDefinition(@RequestPart("file") MultipartFile uploadedFileRef)
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
	 * Creates a new process definition in the system. This requests is not of common use. Normally a new process
	 * definition is created uploading a new BPMN file to the system.
	 * 
	 * @param process
	 *            the new process definition to be added to the system
	 * @return the process definition updated
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process", method = RequestMethod.POST)
	@ResponseBody
	public WorkflowDefinition createProcessDefinition(@RequestBody WorkflowDefinition process)
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
	public DefinitionVersion createProcessVersion(@PathVariable int id,
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
