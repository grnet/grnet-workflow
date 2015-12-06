/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.service;

import gr.cyberstream.util.string.StringUtil;

import java.io.InputStream;
import java.util.List;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * Helper functions for simplifying common tasks for Activiti processes etc.
 * 
 * @author nlyk
 *
 */
public class ActivitiHelper {

	/**
	 * Get a process definition using the deployment id
	 * 
	 * @param activitiRepository
	 * @param deploymentId
	 * @return
	 */
	public static ProcessDefinition getProcessDefinitionByDeploymentId(RepositoryService activitiRepository,
			String deploymentId) throws ActivitiException {

		List<ProcessDefinition> processDefs = activitiRepository.createProcessDefinitionQuery()
				.deploymentId(deploymentId).list();

		if (processDefs == null || processDefs.size() == 0) {
			throw new ActivitiException("Invalid process definition");
		}

		return processDefs.get(0);
	}

	/**
	 * Create a new deployment from a BPMN file
	 * 
	 * @param inputStream
	 *            the input stream for the BPMN file
	 * @param filename
	 *            the original filename
	 * @return the new deployment
	 */
	public static Deployment createDeployment(RepositoryService activitiRepository, InputStream inputStream,
			String filename) throws ActivitiException {

		Deployment deployment;

		// 1. Deploy the BPMN file to Activiti repository service ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		filename = StringUtil.isEmpty(filename) ? "noname.bpmn20.xml" : filename;

		try {
			deployment = activitiRepository.createDeployment().addInputStream("input.bpmn20.xml", inputStream)
					.name(filename).deploy();
		} catch (XMLException ex) {
			String message = "The BPMN input is not valid. Error string: " + ex.getMessage();
			throw new ActivitiException(message);
		}

		// 2. Check deployment and get metadata from the deployed process definition ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if (deployment == null) {
			throw new ActivitiException("The BPMN input is not valid");
		}

		return deployment;
	}
}
