/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.persistence;

import gr.cyberstream.workflow.engine.model.*;

import java.util.List;

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
	 * Returns all active process definitions
	 * 
	 * @return A list of {@link WorkflowDefinition} containing the active
	 *         definitions
	 */
	public List<WorkflowDefinition> getActiveProcessDefintions();

	/**
	 * Creates or updates a definition
	 * 
	 * @param workflowDefinition
	 *            The definition to be created or updated
	 * 
	 * @return The persisted {@link WorkflowDefinition}
	 */
	public WorkflowDefinition save(WorkflowDefinition workflowDefinition);

	/**
	 * Return the definition for the given id
	 * 
	 * @param definitionId
	 *            Definition's id to be returned
	 * 
	 * @return {@link WorkflowDefinition}
	 */
	public WorkflowDefinition getById(int definitionId);

	/**
	 * Returns definition by a given name
	 * 
	 * @param name
	 *            Definition's name to be returned
	 * 
	 * @return {@link WorkflowDefinition}
	 */
	public WorkflowDefinition getByName(String name);

	/**
	 * Deletes a definition and all versions by a given id
	 * 
	 * @param processId
	 *            Definition's id to be deleted
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
	 * Get the process definitions IDs (keys) for the provided workflow
	 * definition IDs
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
	 * Return process definition version by id
	 * 
	 * @param versionId
	 * @return
	 */
	public DefinitionVersion getVersionById(int versionId);

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

	/**
	 * Saves a new process instance
	 * 
	 * @param workflowInstance
	 * @return
	 */
	public WorkflowInstance save(WorkflowInstance workflowInstance);

	/**
	 * Get the process instance by id
	 * 
	 * @param instanceId
	 * @return
	 */
	public WorkflowInstance getInstanceById(String instanceId);

	/**
	 * 
	 * @param ownerName
	 * @return A list of Workflow Definitions by owner
	 */
	public List<WorkflowDefinition> getDefinitionsByOwner(String ownerName);

	/**
	 * Saves a new UserTaskDetails object
	 * 
	 * @param userTaskDetails
	 * @return
	 */
	public UserTaskDetails save(UserTaskDetails userTaskDetails);

	/**
	 * Return the UserTaskDetails entities of a DefinitionVersion object
	 * 
	 * @param versionId
	 * @return
	 */
	public List<UserTaskDetails> getVersionTaskDetails(int versionId);

	/**
	 * Saves a new UserTaskFormElement
	 * 
	 * @param taskFormElement
	 * @return
	 */
	public UserTaskFormElement save(UserTaskFormElement taskFormElement);

	/**
	 * Returns user task form elements by element id
	 * 
	 * @param elementId
	 * @param userTaskDetailId
	 * @return
	 */
	public List<UserTaskFormElement> getUserTaskFormElements(String elementId, int userTaskDetailId);

	/**
	 * 
	 * @param definition
	 * @return
	 */
	public Long getCheckName(WorkflowDefinition definition);

	/**
	 * Return List of workflow instances by user id
	 * 
	 * @param userId
	 * @return
	 */
	public List<WorkflowInstance> getSupervisedProcesses(String userId);

	/**
	 * Get all running instances by process definition id
	 * 
	 * @param processDefinitionId
	 *            The definition's id to get running instances from
	 * 
	 * @return
	 */
	public List<WorkflowInstance> getActiveProcessInstances(int processDefinitionId);

	/**
	 * Returns a workflow definition by key
	 * 
	 * @param definitionKey
	 * @return
	 */
	public WorkflowDefinition getDefinitionByKey(String definitionKey);

	/**
	 * Returns the UserTaskDetails object with the specified id
	 * 
	 * @param id
	 * @return
	 */
	public UserTaskDetails getUserTaskDetailsById(int id);

	/**
	 * Return the process instance with the specified id
	 * 
	 * @param id
	 */
	public WorkflowInstance getProcessInstance(String id);

	/**
	 * Return all defined instances
	 * 
	 * @return list containing instances
	 */
	public Long getCheckInstanceName(String instanceName);

	/**
	 * Deletes a given instance
	 * 
	 * @param workflowInstance
	 *            Instance to be deleted
	 */
	public void cancelProcessInstance(WorkflowInstance workflowInstance);

	/**
	 * Returns task details for a task
	 * 
	 * @param key
	 * @param definitionId
	 * @return
	 */
	public List<UserTaskDetails> getUserTaskDetailsByDefinitionKey(String key, int definitionId);

	/**
	 * Returns task details for a task
	 * 
	 * @param key
	 * @param definitionVersionKey
	 * @return
	 */
	public UserTaskDetails getUserTaskDetailByDefinitionKey(String key, String definitionVersionKey);

	/**
	 * Returns a WorkfloDefinition entity with the specified processDefinitionId
	 * 
	 * @param processDefinitionId
	 * @return
	 */
	public WorkflowDefinition getProcessByDefinitionId(String processDefinitionId);

	/**
	 * Selects all the required path for the task
	 * 
	 * @param instanceId
	 * @param taskId
	 * @return
	 */
	public TaskPath getTaskPath(String instanceId, String taskId);

	/**
	 * Returns system's settings
	 * 
	 * @return {@link WorkflowSettings}
	 */
	public WorkflowSettings getSettings();

	/**
	 * Update settings
	 */
	public WorkflowSettings updateSettings(WorkflowSettings settings);

	/**
	 * Get definitions by owners
	 * 
	 * @param owners
	 * @return
	 */
	public List<WorkflowDefinition> getDefinitionsByOwners(List<String> owners);

	/**
	 * Return a list of task form elements by definition version and task detail
	 * 
	 * @param definitionVersion
	 * @param taskDetailId
	 * @return
	 */
	public List<UserTaskFormElement> getUserTaskFromElements(String definitionVersion, int taskDetailId);
	
	/**
	 * 
	 * @param definitionVersion
	 * @param taskDefintionKey
	 * @param elementId
	 * @return
	 */
	public UserTaskFormElement getUserTaskFromElement(String definitionVersion, String taskDefintionKey, String elementId);

	/**
	 * Returns a workflow instance by defintion version id
	 * 
	 * @param id
	 */
	public List<WorkflowInstance> getInstancesByDefinitionVersionId(int id);

	/**
	 * Deletes an instance by instance id
	 * 
	 * @param instanceId
	 */
	public void deleteProcessInstance(String instanceId);

	/**
	 * Returns a list of in progress instances
	 *
	 * @return A list of {@link WorkflowInstance} containing in progress
	 *         instances
	 */
	public List<WorkflowInstance> getInProgressInstances();

	/**
	 * Returns a list of ended progress instances
	 *
	 * @return A list of {@link WorkflowInstance} containing ended
	 *         instances
	 */
	public List<WorkflowInstance> getEndedProgressInstances();

	/**
	 * 
	 * @param definitionVersionId
	 * @return
	 */
	public DefinitionVersion getDefinitionVersionById(Integer definitionVersionId);
}
