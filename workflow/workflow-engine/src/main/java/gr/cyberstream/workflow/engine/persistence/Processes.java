/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.persistence;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.ExternalForm;
import gr.cyberstream.workflow.engine.model.ExternalGroup;
import gr.cyberstream.workflow.engine.model.ExternalWrapper;
import gr.cyberstream.workflow.engine.model.Registry;
import gr.cyberstream.workflow.engine.model.TaskPath;
import gr.cyberstream.workflow.engine.model.UserTaskDetails;
import gr.cyberstream.workflow.engine.model.UserTaskFormElement;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;

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
	 * @return
	 */
	public List<WorkflowDefinition> getActiveProcessDefintions();

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
	 * Return the process definition for the given name
	 * 
	 * @param name
	 * @return
	 */
	public WorkflowDefinition getByName(String name);

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
	 * Return process definition version by id
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
	 * @param instance
	 * @return
	 */
	public WorkflowInstance save(WorkflowInstance instance);
	
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
	 * Returns a list of user task form elements by element id
	 * 
	 * @param taskDefinitionKey
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
	 * @param userId
	 * @return
	 */
	public List<WorkflowInstance> getSupervisedProcesses(String userId);
	
	/**
	 * Get all instances of process with the specified id
	 * 
	 * @param id
	 * @return
	 */
	public List<WorkflowInstance> getActiveProcessInstances(int id);
	

	/**
	 * Returns a workflow definition by key
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
	 * Delete process instance with the specified id.
	 * 
	 * @param id
	 */
	public void cancelProcessInstance(WorkflowInstance instance);

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
	 * @param definitionId
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
	 * Returns the external form with the specified id
	 * 
	 * @param formId
	 * @return
	 */
	public ExternalForm getFormById(String formId);
	
	/**
	 * Updates a registry entry to the database
	 * 
	 * @param registry
	 * @return
	 */
	public void update(Registry registry);
	
	/**
	 * Checks if registry id exists
	 * 
	 * @param registryId
	 */
	public Long checkIfRegistryExists(String registryId);
	
	/**
	 * Get registry by id
	 * 
	 * @param registryId
	 * @return
	 */
	public Registry getRegistryById(String registryId);
	
	/**
	 * Check if definitions has registry with the given registry id
	 * 
	 * @param registryId
	 * @return
	 */
	public Long checkIfDefinitionHasRegistry(String registryId);
	
	/**
	 * Returns the settings.
	 * 
	 * @return
	 */
	public WorkflowSettings getSettings();
	
	/**
	 * Update settings
	 */
	public WorkflowSettings updateSettings(WorkflowSettings settings);
	
	/**
	 * Returns all external forms of a specified process
	 * 
	 * @param id
	 * @return
	 */
	public List<ExternalForm> getProcessExternalForms(int id);
	
	/**
	 * Returns all registries
	 * 
	 * @return
	 */
	public List<Registry> getRegistries();
	
	/**
	 * Deletes a registry
	 * 
	 * @param registryId
	 */
	public void deleteRegistry(String registryId);

	/**
	 * Saves an external form
	 * 
	 * @param xform
	 * @return
	 */
	public ExternalForm saveExternalForm(ExternalForm xform);
	
	/**
	 * Returns the external form with the specified id
	 * 
	 * @param id
	 * @return
	 */
	public ExternalForm getExternalForm(String id);
	
	/**
	 * Delete the external form with the specified id
	 * 
	 * @param id
	 */
	public void deleteExternalForm(ExternalForm xform);
	
	/**
	 * Check whether an external form with the same id exists in the database
	 * 
	 * @param id
	 * @return
	 */
	public Long checkForExternalForm(String id);

	/**
	 * Get definitions by owners 
	 * 
	 * @param owners
	 * @return
	 */
	public List<WorkflowDefinition> getDefinitionsByOwners(List<String> owners); 
	
	/**
	 * Return process instance by reference id
	 * 
	 * @param referenceId
	 * @return
	 */
	public WorkflowInstance getInstanceByReferenceId(String referenceId);

	/**
	 * Return a list of task form elements by definition version and task detail
	 * 
	 * @param definitionVersion
	 * @param elementId
	 * @return
	 */
	public List<UserTaskFormElement> getUserTaskFromElements(String definitionVersion, int taskDetailId);
	
	/**
	 * 
	 * @param definitionVersion
	 * @param taskDetailId
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
	 * Returns a list with all available external forms
	 * 
	 * @return
	 */
	public List<ExternalForm> getExternalForms();
	
	/**
	 * Deletes an instance by instance id
	 * @param instanceId
	 */
	public void deleteProcessInstance(String instanceId);
	
	/**
	 * Get external groups ordered by order
	 * 
	 * @return
	 */
	public List<ExternalWrapper> getExternalFormsGroupsWrapped();
	
	/**
	 * Get external forms by group id 
	 * 
	 * @param groupId
	 * @return
	 */
	public List<ExternalForm> getExternalFormsByGroup(int groupId);
	
	/**
	 * Creates new external group
	 * 
	 * @param externalGroup
	 * @return
	 */
	public ExternalGroup createExternalGroup(ExternalGroup externalGroup);

	/**
	 * Checks if group name already exists
	 * @param groupName
	 * @return
	 */
	public Long checkIfExternalGroup(String groupName);
	
	/**
	 * Get all available groups
	 * 
	 * @return
	 */
	public List<ExternalGroup> getExternalGroups();
	
	/**
	 * Deletes a public group
	 * 
	 * @param groupId
	 */
	public void deletePublicGroup(int groupId);
	
	/**
	 * Returns an external group by id
	 * 
	 * @param groupId
	 * @return
	 */
	public ExternalGroup getExternalGroupById(int groupId);
	
	/**
	 * Checks if public group has public forms
	 * 
	 * @param groupId
	 * @return
	 */
	public Long checkIfPublicGroupHasForms(int groupId);
	
	/**
	 * Updates an external group
	 * 
	 * @param externalGroup
	 * @return
	 */
	public ExternalGroup updatePublicGroup(ExternalGroup externalGroup);
}
