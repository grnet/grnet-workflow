package gr.cyberstream.workflow.engine.controller.v2;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gr.cyberstream.workflow.engine.model.Registry;
import gr.cyberstream.workflow.engine.model.api.ErrorResponse;
import gr.cyberstream.workflow.engine.model.api.WfProcessMetadata;
import gr.cyberstream.workflow.engine.model.api.WfPublicForm;
import gr.cyberstream.workflow.engine.model.api.WfPublicGroup;
import gr.cyberstream.workflow.engine.model.api.WfPublicService;
import gr.cyberstream.workflow.engine.model.api.WfPublicWrapper;
import gr.cyberstream.workflow.engine.model.api.WfRegistry;
import gr.cyberstream.workflow.engine.service.InternalException;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.ProcessService;

/**
* Implements all RESTfull requests related to public forms and registry
*/
@RestController
@RequestMapping(value = "/api/v2")
@MultipartConfig(fileSizeThreshold = 20971520)
public class PublicFormController {

	final static Logger logger = LoggerFactory.getLogger(DefinitionController.class);

	@Autowired
	private ProcessService processService;
	
	/**
	 * <code>GET: /api/v2/public/service</code>
	 * 
	 * Returns all available public services
	 * 
	 * @return
	 */
	@RequestMapping(value = "/public/service", method = RequestMethod.GET)
	@ResponseBody
	public List<WfPublicService> getPublicServices() {
		
		return processService.getExternalServices();
	}
	
	/**
	 * <code>GET: /api/v2/public/form/{id}</code>
	 * 
	 * Returns process start form for the 
	 * specfied public form id
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/public/form/{id}", method = RequestMethod.GET)
	public WfProcessMetadata getPublicProcessMetadata(@PathVariable String id)
			throws InvalidRequestException {
		
		logger.info("Get Process external form " + id);
		
		return processService.getPublicProcessMetadata(id);
	}
		
	/**
	 * <code>GET: /api/v2/form/process/{id}</code>
	 * 
	 * Return public forms for specific process
	 * 
	 * @param id
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/form/process/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<WfPublicForm> getProcessPublicForms(@PathVariable int id)
			throws InvalidRequestException {
		
		List<WfPublicForm> wfForms = processService.getProcessExternalForms(id);
		
		return wfForms;
	}
	
	/**
	 * <code>POST: /api/v2/form</code>
	 * 
	 * Create a new public form
	 * 
	 * @param form
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/form", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfPublicForm createPublicForm(@RequestBody WfPublicForm form) throws InvalidRequestException {

		return processService.createExternalForm(form);
	}
	
	/**
	 * <code>PUT: /api/v2/form</code>
	 * 
	 * Update a public form
	 * 
	 * @param form
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/form", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfPublicForm updatePublicForm(@RequestBody WfPublicForm form) throws InvalidRequestException {

		return processService.updateExternalForm(form);
	}
	
	/**
	 * <code>DELETE: /api/v2/form/{id}</code>
	 * 
	 * Delete a public form
	 * 
	 * @param id
	 * 
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/form/{id}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public void deletePublicForm(@PathVariable String id) throws InvalidRequestException{
		
		logger.info("Delete public form " + id);

		processService.deleteExternalForm(id);
	}
	
	/**
	 * <code>DELETE: /api/v2/form/{id}/{action}</code>
	 * 
	 * Suspend / Resume an external form
	 * 
	 * @param id
	 * @param action
	 * @return
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/form/{id}/{action}", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ProcessAdmin','Admin')") 
	public WfPublicForm modifyExternalFormStatus(@PathVariable String id, @PathVariable String action) 
			throws InvalidRequestException{
		
		logger.info(action + " action on external form.");
		
		WfPublicForm form;
		
		if(action.equals("suspend"))	form = processService.modifyExternalFormStatus(id, false);
		else if(action.equals("resume"))	form = processService.modifyExternalFormStatus(id, true);
		else throw new InvalidRequestException("Non valid action on external form");
				
		return form;
	}
	
	/**
	 * <code>GET: /api/v2/registry</code>
	 * 
	 * Returns all available registries
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/registry", method = RequestMethod.GET)
	@ResponseBody
	public List<WfRegistry> getRegistries()
			throws InvalidRequestException {
		
		List<WfRegistry> wfRegistries = new ArrayList<WfRegistry>();
		List<Registry> registries = processService.getRegistries();
		
		for (Registry registry : registries) {
			
			wfRegistries.add(new WfRegistry(registry));
		}
		
		return wfRegistries;
	}
	
	/**
	 * <code>POST: /api/v2/registry</code>
	 * 
	 * Creates a registry
	 * 
	 * @param registryId
	 * 
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/registry", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public void createRegistry(@RequestBody WfRegistry wfRegistry) 
			throws InvalidRequestException{
		
		Registry registry = new Registry(wfRegistry);
		processService.createRegistry(registry);
	}
	
	/**
	 * <code>PUT: /api/v2/registry</code>
	 * 
	 * Updates a registry
	 * 
	 * @param registryId
	 * @param request
	 * @return
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/registry", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public void updateRegistry(@RequestBody WfRegistry wfRegistry) 
			throws InvalidRequestException{
		
		Registry registry = new Registry(wfRegistry);
		processService.updateRegistry(registry);
	}
	
	/**
	 * <code>DELETE: /api/v2/registry/{id}</code>
	 * 
	 * Deletes a registry
	 * 
	 * @param registry
	 * @param request
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/registry/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasRole('Admin')")
	public void deleteRegistry(@PathVariable String id) 
			throws InvalidRequestException{
		
		processService.deleteRegistry(id);
	}
	
	/**
	 * <code>GET: /api/v2/form/group/wrapped</code>
	 * 
	 * Returns all available public forms ordered by group
	 * 
	 * @return
	 */
	@RequestMapping(value = "/form/group/wrapped", method = RequestMethod.GET)
	@ResponseBody
	public List<WfPublicWrapper> getWrappedGroupsForms() {
		
		return WfPublicWrapper.fromExternalWrappers(processService.getExternalWrapper());
	}
	
	/**
	 * <code>GET: /api/v2/form/group</code>
	 * 
	 * Returns public groups
	 * 
	 * @return
	 */
	@RequestMapping(value = "/form/group", method = RequestMethod.GET)
	@ResponseBody
	public List<WfPublicGroup> getPublicGroups() {
		
		return processService.getExternalGroups();
	}
	
	
	/**
	 * <code>POST: /api/v2/form/group</code>
	 * 
	 * Creates a new public group
	 * 
	 * @param wfPublicGroup
	 */
	@RequestMapping(value = "/form/group", method = RequestMethod.POST)
	public void createPublicGroup(@RequestBody WfPublicGroup wfPublicGroup) {
		
		processService.createExternalGroup(wfPublicGroup);
	}
	
	/**
	 * <code>POST: /api/v2/form/group/{groupId}</code>
	 * 
	 * Deletes a group
	 * 
	 * @param groupId
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/form/group/{groupId}", method = RequestMethod.DELETE)
	public void deletePublicGroup(@RequestParam int groupId) throws InvalidRequestException {
		
		processService.deletePublicGroup(groupId);
	}
	
	/**
	 * <code>POST: /api/v2/form/group</code>
	 * 
	 * Updates a group
	 * 
	 * @param publicGroup
	 * @throws InvalidRequestException 
	 */
	@RequestMapping(value = "/form/group", method = RequestMethod.PUT)
	public void updatePublicGroup(@RequestBody WfPublicGroup publicGroup) throws InvalidRequestException {
		
		processService.updatePublicGroup(publicGroup);
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
