package gr.cyberstream.workflow.engine.controller;

import java.util.List;
import java.util.Map;

import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.ProcessService;

import javax.servlet.annotation.MultipartConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
	 * @throws InvalidRequestException
	 */
	@RequestMapping(value = "/process/{processId}/start", method = RequestMethod.POST)
	@ResponseBody
	public void createProcessDefinition(@PathVariable int processId, @RequestBody Map<String, String> formData)
			throws InvalidRequestException {
		processService.startProcess(processId, formData);
	}

	/**
	 * Returns the unassigned tasks according t a list of workflow definition IDs
	 * 
	 * @param processIds
	 * @return
	 */
	@RequestMapping(value = "/task/unassigned", method = RequestMethod.GET)
	@ResponseBody
	public List<WfTask> getUnassingedTasksByProcessIds(@RequestParam("p") List<Integer> processIds)
			throws InvalidRequestException {
		
		logger.info("request unassigned tasks for processes: " + processIds);
		
		return processService.getUnassingedTasksByProcessIds(processIds);
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
}
