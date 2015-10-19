package gr.cyberstream.workflow.engine.persistence;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 * Process repository interface. Access to process entities.
 * 
 * @author nlyk
 *
 */
public interface Processes {

	/**
	 * Return all defined processes
	 * 
	 * @return list containing the processes
	 */
	public List<WorkflowDefinition> getAll();

	/**
	 * Saves the new process definition
	 * 
	 * @param process
	 * @return
	 */
	@Transactional
	public WorkflowDefinition save(WorkflowDefinition process);

	/**
	 * Return the process definition for the given id
	 * 
	 * @param id
	 * @return
	 */
	public WorkflowDefinition getById(int id);

	/**
	 * Deleted the process definition and all versions
	 * 
	 * @param processId
	 */
	public void delete(int processId);

	/**
	 * Updates the Definition Version
	 * 
	 * @param processId
	 * @param definitionVersion
	 * @return
	 */
	public DefinitionVersion saveVersion(int processId, DefinitionVersion definitionVersion);

	/**
	 * Get the process definitions IDs (keys) for the provided workflow definition IDs
	 * 
	 * @param processIds
	 * @return
	 */
	public List<String> getProcessDefinitionIDs(List<Integer> processIds);

	/**
	 * Get a process definition version using the deployment id
	 * 
	 * @param deploymentId
	 * @return
	 */
	public DefinitionVersion getVersionByDeploymentId(String deploymentId);

	/**
	 * Get a process definition versions using the process id
	 * 
	 * @param processId
	 * @return
	 */
	public List<DefinitionVersion> getVersionsByProcessId(int processId);

	/**
	 * Returns the process definition versions using the process definition id
	 */
	public DefinitionVersion getVersionByProcessDefinitionId(String processDefinitionId);

}
