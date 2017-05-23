package gr.cyberstream.workflow.engine.controller.v2;

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
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfProcessStatus;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.ProcessService;

/**
 * Implements all RESTfull requests related to process execution
 */
@RestController("executionV2Controller")
@RequestMapping(value = "/api/v2")
@MultipartConfig(fileSizeThreshold = 20971520)
public class ExecutionController {

	final static Logger logger = LoggerFactory.getLogger(ExecutionController.class);

	@Autowired
	private ProcessService processService;

	/**
	 * <code>GET: /api/v2/execution/supervised</code>
	 * 
	 * Return process executions supervised by the current user
	 * 
	 * @return
	 */
	@RequestMapping(value = "/execution/supervised", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getSupervisedProcesses() {

		return processService.getSupervisedInstances();
	}

	/**
	 * <code>GET: /api/v2/execution/completed</code>
	 * 
	 * Get user's proces executions instances
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/execution/completed", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getUserCompletedExecutions() throws InvalidRequestException {

		return processService.getUserCompletedInstances();
	}

	/**
	 * <code>GET: /api/v2/execution/process/version/{id}</code>
	 * 
	 * Returns all running instances for the specified process definition
	 * version.
	 * 
	 * @param id
	 * 
	 * @return
	 */
	@RequestMapping(value = "/execution/process/version/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getExecutions(@PathVariable int id) {
		return processService.getActiveProcessInstances(id);
	}

	/**
	 * <code>GET: /api/v2/mobile/execution/{id}</code>
	 * 
	 * Returns execution by id, used by the mobile client.
	 * 
	 * @param id
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/mobile/execution/{id}", method = RequestMethod.GET)
	@ResponseBody
	public WfProcessInstance getExecutionById(@PathVariable String id) throws InvalidRequestException {

		return processService.getProcessInstanceById(id);
	}

	/**
	 * <code>POST: /api/v2/process/{id}/start</code>
	 * 
	 * Starts a new process execution using form data
	 * 
	 * @param processId
	 *            the workflow definition id
	 * @param instanceData
	 *            the form data in key-value pairs
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@RequestMapping(value = "/process/{id}/start", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('Admin','ProcessAdmin')")
	public WfProcessInstance startProcess(@PathVariable int id, @RequestBody WfProcessInstance instanceData)
			throws InvalidRequestException, InternalException {

		logger.info("Start process: " + id);

		return processService.startProcess(id, instanceData);
	}

	/**
	 * <code>POST: /api/v2/process/{id}/start</code>
	 * 
	 * Starts a new process instance using form data and files
	 * 
	 * @param processId
	 *            the workflow definition id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws InternalException
	 * @throws InvalidRequestException
	 * @throws Exception
	 */
	@RequestMapping(value = "/process/{id}/start", method = RequestMethod.POST, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseBody
	@PreAuthorize("hasAnyRole('Admin','ProcessAdmin')")
	public WfProcessInstance startProcessWithDocuments(@PathVariable int id, @RequestPart("json") String instanceData,
			@RequestParam("file") MultipartFile[] files) throws InvalidRequestException, InternalException {

		logger.info("Start process: " + id);

		ObjectMapper mapper = new ObjectMapper();

		WfProcessInstance wfProcessInstance;

		try {
			wfProcessInstance = mapper.readValue(instanceData, WfProcessInstance.class);

			return processService.startProcess(id, wfProcessInstance, files);

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
	 * <code>POST: /api/v2/public/form/{id}/start</code>
	 * 
	 * Starts a new process instance using form data
	 * 
	 * @param id
	 *            external form id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@RequestMapping(value = "/public/form/{id}/start", method = RequestMethod.POST)
	@ResponseBody
	public WfProcessInstance startPublicProcess(@PathVariable String id, @RequestBody WfProcessInstance instanceData)
			throws InvalidRequestException, InternalException {

		logger.info("Start process using form: " + id);

		if (instanceData.getCaptchaAnswer() == null || instanceData.getCaptchaAnswer().isEmpty()) {

			throw new InvalidRequestException("Captcha answer is null or empty.");
		}

		return processService.startPublicProcess(id, instanceData);
	}

	/**
	 * <code>POST: /api/v2/public/form/{id}/start</code>
	 * 
	 * Starts a new process instance using form data and files
	 * 
	 * @param id
	 *            external form id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws InvalidRequestException
	 * @throws InternalException
	 * @throws Exception
	 */
	@RequestMapping(value = "/public/form/{id}/start", method = RequestMethod.POST, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseBody
	public WfProcessInstance startPublicProcessWithDocuments(@PathVariable String id,
			@RequestPart("json") String instanceData, @RequestParam("file") MultipartFile[] files)
			throws InvalidRequestException, InternalException {

		logger.info("Start process using form: " + id);

		ObjectMapper mapper = new ObjectMapper();

		WfProcessInstance wfProcessInstance;

		try {
			wfProcessInstance = mapper.readValue(instanceData, WfProcessInstance.class);

			if (wfProcessInstance.getCaptchaAnswer() == null || wfProcessInstance.getCaptchaAnswer().isEmpty()) {

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
	 * <code>POST: /api/v2/mobile/form/{id}/start</code>
	 * 
	 * Starts a new process instance using form data used by mobile client. That
	 * means no captcha or any other security is used
	 * 
	 * @param id
	 *            external form id
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@RequestMapping(value = "/mobile/form/{id}/start", method = RequestMethod.POST)
	@ResponseBody
	public WfProcessInstance startPublicMobileProcess(@PathVariable String id,
			@RequestBody WfProcessInstance instanceData) throws InvalidRequestException, InternalException {

		logger.info("Start process from mobile client using form: " + id);

		return processService.startPublicMobileProcess(id, instanceData);
	}

	/**
	 * <code>POST: /api/v2/mobile/form/{id}/start</code>
	 * 
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
	@RequestMapping(value = "/mobile/form/{id}/start", method = RequestMethod.POST, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseBody
	public WfProcessInstance startPublicMobileProcessWithDocuments(@PathVariable String id,
			@RequestPart("json") String instanceData, @RequestParam("file") MultipartFile[] files)
			throws InvalidRequestException, InternalException {

		logger.info("Start process using form: " + id);

		ObjectMapper mapper = new ObjectMapper();

		WfProcessInstance wfProcessInstance;

		try {
			wfProcessInstance = mapper.readValue(instanceData, WfProcessInstance.class);

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
	 * <code>POST: /api/v2/public/execution/status</code>
	 * 
	 * Returns the status by the reference id
	 * 
	 * @param referenceId
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/public/execution/status", method = RequestMethod.GET)
	@ResponseBody
	public WfProcessStatus getProcessStatus(@RequestParam("referenceId") String referenceId)
			throws InvalidRequestException {

		return processService.getProcessStatusByReferenceId(referenceId);
	}

	/**
	 * <code>PUT: /api/v2/execution/{id}/cancel</code>
	 * 
	 * Cancel a process execution
	 */
	@RequestMapping(value = "/execution/{id}/cancel", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public void cancelProcessInstance(@PathVariable String id) throws InvalidRequestException {

		logger.info("Cancel process instance.");
		processService.cancelProcessInstance(id);
	}

	/**
	 * <code>PUT: /api/v2/execution/{id}/{action}</code>
	 * 
	 * Either suspend or resume a process instance.
	 */
	@RequestMapping(value = "/execution/{id}/{action}", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessInstance modifyProcessInstanceStatus(@PathVariable String id, @PathVariable String action)
			throws InvalidRequestException {

		logger.info(action + " action on process instance.");

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
	 * <code>PUT: /api/v2/execution/{id}/version/{version}</code>
	 * 
	 * Modify the process instance version
	 */
	@RequestMapping(value = "/execution/{id}/version/{version}", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')")
	public WfProcessInstance modifyProcessInstanceVersion(@PathVariable String id, @PathVariable int version)
			throws InvalidRequestException {

		logger.info("Modify process instance " + id + " version.");

		return processService.setInstanceVersion(id, version);
	}

	/**
	 * <code>GET: /api/v2/execution/{id}</code>
	 * 
	 * Deletes an execution
	 * 
	 * @param id
	 */
	@RequestMapping(value = "/execution/{id}", method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('Admin')")
	public void deleteCompletedExecution(@PathVariable String id) {

		processService.deleteProcessCompletedInstance(id);
	}

	/**
	 * <code>GET: /api/v2/execution/{id}/form</code>
	 * 
	 * Returns the start process form by id
	 * 
	 * @param id
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/execution/{id}/form", method = RequestMethod.GET)
	@ResponseBody
	public List<WfFormProperty> getExecutionForm(@PathVariable String id) throws InvalidRequestException {

		return processService.getStartFormByInstanceId(id);
	}

	/**
	 * <code>GET: /api/v2/execution/{id}/document</code>
	 * 
	 * Get process execution documents.
	 * 
	 * @param id
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/execution/{id}/document", method = RequestMethod.GET)
	@ResponseBody
	public List<WfDocument> getProcessInstanceDocuments(@PathVariable int id) throws InvalidRequestException {

		logger.info("Getting documents for process instance: " + id);

		return processService.getProcessInstanceDocuments(id);
	}

	/**
	 * <code>GET: /api/v2/execution/{id}/diagram</code>
	 * 
	 * Returns a diagram of a progress execution
	 * 
	 * @param id
	 *            the process execution id
	 * 
	 * @return
	 */
	@RequestMapping(value = "/execution/{id}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public Resource getProcessDiagram(@PathVariable String id) {

		return processService.getInstanceProgressDiagram(id);
	}

	/**
	 * <code>POST: /api/v2/instance/{instanceId}/supervisor</code>
	 * 
	 * Changes instance's supervisor
	 * 
	 * @param instanceId
	 * @param supervisor
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/instance/{instanceId}/supervisor", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public void changeInstanceSupervisor(@PathVariable String instanceId, @RequestParam("supervisor") String supervisor)
			throws InvalidRequestException {

		processService.changeInstanceSupervisor(instanceId, supervisor);
	}

	/**
	 * <code>GET: /api/v2/inprogress/instances</code>
	 * 
	 * A list of all in progress instances
	 * 
	 * @return a list of all in progress instances
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/inprogress/instances", method = RequestMethod.GET)
	@ResponseBody
	public List<WfProcessInstance> getInProgressInstances() throws InvalidRequestException {

		return processService.getInProgressInstances();
	}

	/**
	 * Returns instance's documents
	 * 
	 * @param instanceId
	 *            The instance's id
	 *            
	 * @return List of {@link WfDocument}
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/instance/{instanceId}/documents", method = RequestMethod.GET)
	@ResponseBody
	public List<WfDocument> getDocumentsByInstanceId(@PathVariable String instanceId) throws InvalidRequestException {

		return processService.getDocumentsByInstance(instanceId);
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
