/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.persistence;

import java.util.List;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.ExternalForm;
import gr.cyberstream.workflow.engine.model.ExternalGroup;
import gr.cyberstream.workflow.engine.model.ExternalUser;
import gr.cyberstream.workflow.engine.model.ExternalWrapper;
import gr.cyberstream.workflow.engine.model.Owner;
import gr.cyberstream.workflow.engine.model.Registry;
import gr.cyberstream.workflow.engine.model.Role;
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
	 * @return A list of {@link WorkflowDefinition}
	 */
	public List<WorkflowDefinition> getAll();

	/**
	 * Returns all active process definitions
	 * 
	 * @return A list of {@link WorkflowDefinition}
	 */
	public List<WorkflowDefinition> getActiveProcessDefintions();

	/**
	 * Saves the new process definition
	 * 
	 * @param workflowDefinition
	 *            The definition to be saved
	 * 
	 * @return {@link WorkflowDefinition}
	 */
	public WorkflowDefinition save(WorkflowDefinition workflowDefinition);

	/**
	 * Return process definition by id
	 * 
	 * @param id
	 *            The process definition id
	 * 
	 * @return {@link WorkflowDefinition}
	 */
	public WorkflowDefinition getById(int id);

	/**
	 * Return the process definition for the given name
	 * 
	 * @param name
	 *            The process definition name
	 * 
	 * @return {@link WorkflowDefinition}
	 */
	public WorkflowDefinition getByName(String name);

	/**
	 * Deletes the process definition by its id and all version of it
	 * 
	 * @param processId
	 *            The process definition id
	 */
	public void delete(int processId);

	/**
	 * Updates a process definition's version
	 * 
	 * @param processId
	 *            The new process version
	 * 
	 * @param definitionVersion
	 *            The process definition id
	 * 
	 * @return {@link DefinitionVersion}
	 */
	public DefinitionVersion saveVersion(int processId, DefinitionVersion definitionVersion);

	/**
	 * Return the process definition IDs (Keys) for the workflow definition IDs
	 * 
	 * @param processIds
	 *            Process definitions ids
	 * 
	 * @return A list of {@link String}
	 */
	public List<String> getProcessDefinitionIDs(List<Integer> processIds);

	/**
	 * Get a process definition version using the deployment id
	 * 
	 * @param deploymentId
	 * 
	 * @return {@link DefinitionVersion}
	 */
	public DefinitionVersion getVersionByDeploymentId(String deploymentId);
	
	/**
	 * Get a process definition version using the process definition
	 * and the version number
	 * 
	 * @param definition
	 * @param version
	 * 
	 * @return {@link DefinitionVersion}
	 */
	public DefinitionVersion getDefinitionVersion(WorkflowDefinition definition, int version);

	/**
	 * Returns definition version by id
	 * 
	 * @param versionId
	 *            The version's id
	 * 
	 * @return {@link DefinitionVersion}
	 */
	public DefinitionVersion getVersionById(int versionId);

	/**
	 * Returns definitions versions by process definition id (workflowDefinition
	 * id)
	 * 
	 * @param processId
	 *            The process definition id
	 * 
	 * @return List of {@link DefinitionVersion}
	 */
	public List<DefinitionVersion> getVersionsByProcessId(int processId);

	/**
	 * Returns the process definition versions using the process definition id
	 * (processDefinitionId)
	 * 
	 * @param processDefinitionId
	 * 
	 * @return {@link DefinitionVersion}
	 */
	public DefinitionVersion getVersionByProcessDefinitionId(String processDefinitionId);

	/**
	 * Saves a new process instance
	 * 
	 * @param instance
	 *            The workflow instance to be saved
	 * 
	 * @return {@link WorkflowInstance}
	 */
	public WorkflowInstance save(WorkflowInstance instance);

	/**
	 * Returns a workflow instance by its id
	 * 
	 * @param instanceId
	 *            The workflow instance's id
	 * 
	 * @return {@link WorkflowInstance}
	 */
	public WorkflowInstance getInstanceById(String instanceId);

	/**
	 * Returns process definitions by owner
	 * 
	 * @param ownerName
	 *            The owner of the process definitions
	 * 
	 * @return List of {@link WorkflowDefinition}
	 */
	public List<WorkflowDefinition> getDefinitionsByOwner(String ownerName);

	/**
	 * Saves user task details
	 * 
	 * @param userTaskDetails
	 *            The task details to be saved
	 * 
	 * @return {@link UserTaskDetails}
	 */
	public UserTaskDetails save(UserTaskDetails userTaskDetails);

	/**
	 * Returns user task details by process definition version id
	 * 
	 * @param versionId
	 *            The process definition version id
	 * 
	 * @return List of {@link UserTaskDetails}
	 */
	public List<UserTaskDetails> getVersionTaskDetails(int versionId);

	/**
	 * Saves a new user task form element
	 * 
	 * @param taskFormElement
	 *            The task form element to be saved
	 * 
	 * @return {@link UserTaskFormElement}
	 */
	public UserTaskFormElement save(UserTaskFormElement taskFormElement);

	/**
	 * Returns user task form elements by the process definition version and the
	 * id of the element
	 * 
	 * @param elementId
	 *            The user task form element id
	 * 
	 * @param definitionVersionId
	 *            The process version id
	 * 
	 * @return List of {@link UserTaskFormElement}
	 */
	public List<UserTaskFormElement> getUserTaskFormElements(String elementId, int definitionVersionId);

	/**
	 * Checks for the same process definition name. Returns a long based on how
	 * many definitions with the same name found
	 * 
	 * @param definition
	 *            The process definition's to test its uniqueness
	 * 
	 * @return {@link Long}
	 */
	public Long getCheckName(WorkflowDefinition definition);

	/**
	 * Returns process instances supervised by given user id
	 * 
	 * @param userId
	 *            The supervisor's user id (The email address)
	 * 
	 * @return List of {@link WorkflowInstance}
	 */
	public List<WorkflowInstance> getSupervisedProcesses(String userId);

	/**
	 * Returns active process instances by process definition id
	 * 
	 * @param workflowDefinitionId
	 *            The process definition id which instances will be selected
	 * 
	 * @return List of {@link WorkflowInstance}
	 */
	public List<WorkflowInstance> getActiveProcessInstances(int workflowDefinitionId);

	/**
	 * Returns a workflow definition by key
	 * 
	 * @param definitionKey
	 *            The process definition key
	 * 
	 * @return {@link WorkflowDefinition}
	 */
	public WorkflowDefinition getDefinitionByKey(String definitionKey);

	/**
	 * Returns user task details by id
	 * 
	 * @param id
	 *            The user's task details id
	 * 
	 * @return {@link UserTaskDetails}
	 */
	public UserTaskDetails getUserTaskDetailsById(int id);

	/**
	 * Return the process instance by id
	 * 
	 * @param id
	 *            The process instace's id
	 * 
	 * @return {@link WorkflowInstance}
	 */
	public WorkflowInstance getProcessInstance(String id);

	/**
	 * Checks all instances for the same name. Returns a long number based on
	 * how many instances with the same name found.
	 * 
	 * @param instanceName
	 *            The instance's name to check its uniqueness
	 * 
	 * @return {@link Long}
	 */
	public Long getCheckInstanceName(String instanceName);

	/**
	 * Deletes process instance by id
	 * 
	 * @param instance
	 *            The process instance to be deleted
	 */
	public void cancelProcessInstance(WorkflowInstance instance);

	/**
	 * Returns user task details by process definition id
	 * 
	 * @param key
	 *            The task's key
	 * 
	 * @param definitionId
	 *            The process definition id
	 * 
	 * @return List of {@link UserTaskDetails}
	 */
	public List<UserTaskDetails> getUserTaskDetailsByDefinitionKey(String key, int definitionId);

	/**
	 * Returns user task details by process definition key
	 * 
	 * @param key
	 *            The task's key
	 * 
	 * @param definitionVersionKey
	 *            The process definition key
	 * 
	 * @return {@link UserTaskDetails}
	 */
	public UserTaskDetails getUserTaskDetailByDefinitionKey(String key, String definitionVersionKey);

	/**
	 * Returns a WorkfloDefinition entity with the specified processDefinitionId
	 * 
	 * @param processDefinitionId
	 *            The process definition's id
	 * 
	 * @return {@link WorkflowDefinition}
	 */
	public WorkflowDefinition getProcessByDefinitionId(String processDefinitionId);

	/**
	 * Selects all the required path for the task
	 * 
	 * @param instanceId
	 *            The instance's id
	 * 
	 * @param taskId
	 *            The task's id
	 * 
	 * @return {@link TaskPath}
	 */
	public TaskPath getTaskPath(String instanceId, String taskId);

	/**
	 * Returns external form by id
	 * 
	 * @param formId
	 *            The external form's id
	 * 
	 * @return {@link ExternalForm}
	 */
	public ExternalForm getFormById(String formId);

	/**
	 * Updates registry entry
	 * 
	 * @param registry
	 *            The registry to be updated
	 */
	public void saveRegistry(Registry registry);

	/**
	 * Checks if registry id exists. Returns a long number based on how many
	 * same registries found by id
	 * 
	 * @param registryId
	 *            The registry's id
	 * 
	 * @return {@link Long}
	 */
	public Long checkIfRegistryExists(String registryId);

	/**
	 * Get registry by id
	 * 
	 * @param registryId
	 *            Registry's id
	 * 
	 * @return {@link Registry}
	 */
	public Registry getRegistryById(String registryId);

	/**
	 * Check if definitions has registry with the given registry id. Return a
	 * long number based on how many process definition found with the given
	 * registry
	 * 
	 * @param registryId
	 *            Registry's id
	 * 
	 * @return {@link Long}
	 */
	public Long checkIfDefinitionHasRegistry(String registryId);

	/**
	 * Returns settings
	 * 
	 * @return {@link WorkflowSettings}
	 */
	public WorkflowSettings getSettings();

	/**
	 * Updates settings
	 * 
	 * @param settings
	 *            Settings object to be updated
	 * 
	 * @return {@link WorkflowSettings}
	 */
	public WorkflowSettings updateSettings(WorkflowSettings settings);

	/**
	 * Returns all external forms by process definition id
	 * 
	 * @param id
	 *            The process definition id
	 * 
	 * @return List of {@link ExternalForm}
	 */
	public List<ExternalForm> getProcessExternalForms(int id);

	/**
	 * Returns all registries
	 * 
	 * @return List of {@link Registry}
	 */
	public List<Registry> getRegistries();

	/**
	 * Deletes a registry by registry id
	 * 
	 * @param registryId
	 *            The registry's id
	 */
	public void deleteRegistry(String registryId);

	/**
	 * Saves an external form
	 * 
	 * @param xform
	 *            The external form to be saved
	 * 
	 * @return {@link ExternalForm}
	 */
	public ExternalForm saveExternalForm(ExternalForm xform);

	/**
	 * Returns external form by id
	 * 
	 * @param id
	 *            The external form's id
	 * 
	 * @return {@link ExternalForm}
	 */
	public ExternalForm getExternalForm(String id);

	/**
	 * Deletes external form by id
	 * 
	 * @param xform
	 *            The external form to be deleted
	 */
	public void deleteExternalForm(String externalFormId);

	/**
	 * Check whether an external form with the same id exists in the database
	 * 
	 * @param id
	 *            The external form's id
	 * 
	 * @return {@link Long}
	 */
	public Long checkForExternalForm(String id);

	/**
	 * Get definitions by a list of owners
	 * 
	 * @param owners
	 *            List of owners
	 * 
	 * @return List of {@link WorkflowDefinition}
	 */
	public List<WorkflowDefinition> getDefinitionsByOwners(List<String> owners);

	/**
	 * Return process instance by reference id
	 * 
	 * @param referenceId
	 *            The process instace's reference id
	 * 
	 * @return {@link WorkflowInstance}
	 */
	public WorkflowInstance getInstanceByReferenceId(String referenceId);

	/**
	 * Return a list of task form elements by definition version and task detail
	 * 
	 * @param definitionVersion
	 *            The process definition version
	 * 
	 * @param taskDetailId
	 *            The task's details id
	 * 
	 * @return List of {@link UserTaskFormElement}
	 */
	public List<UserTaskFormElement> getUserTaskFromElements(String definitionVersion, int taskDetailId);

	/**
	 * Returns user task form element
	 * 
	 * @param definitionVersion
	 *            The process definition version
	 * 
	 * @param taskDefintionKey
	 *            The task's definition key
	 * 
	 * @param elementId
	 *            The form element's id
	 * 
	 * @return {@link UserTaskFormElement}
	 */
	public UserTaskFormElement getUserTaskFromElement(String definitionVersion, String taskDefintionKey,
			String elementId);

	/**
	 * Returns process instances based on process definition version id
	 * 
	 * @param id
	 *            The process definition version id
	 * 
	 * @return List of {@link WorkflowInstance}
	 */
	public List<WorkflowInstance> getInstancesByDefinitionVersionId(int id);

	/**
	 * Returns a list with all available external forms
	 * 
	 * @return List of {@link ExternalForm}
	 */
	public List<ExternalForm> getExternalForms();

	/**
	 * Deletes an instance by instance id
	 * 
	 * @param instanceId
	 *            The process instace's id
	 */
	public void deleteProcessInstance(String instanceId);

	/**
	 * Get external groups ordered by order
	 * 
	 * @return List of {@link ExternalWrapper}
	 */
	public List<ExternalWrapper> getExternalFormsGroupsWrapped();

	/**
	 * Get external forms by group id
	 * 
	 * @param groupId
	 *            The group's id
	 * 
	 * @return List of {@link ExternalForm}
	 */
	public List<ExternalForm> getExternalFormsByGroup(int groupId);

	/**
	 * Creates new external group
	 * 
	 * @param externalGroup
	 *            The external group to be created
	 * 
	 * @return {@link ExternalGroup}
	 */
	public ExternalGroup createExternalGroup(ExternalGroup externalGroup);

	/**
	 * Checks if group name already exists
	 * 
	 * @param groupName
	 *            The group's name to check if alreay exists
	 * 
	 * @return {@link Long}
	 */
	public Long checkIfExternalGroup(String groupName);

	/**
	 * Get all available groups
	 * 
	 * @return List of {@link ExternalGroup}
	 */
	public List<ExternalGroup> getExternalGroups();

	/**
	 * Deletes an external group by id
	 * 
	 * @param groupId
	 *            Group's id to be deleted
	 */
	public void deletePublicGroup(int groupId);

	/**
	 * Returns an external group by id
	 * 
	 * @param groupId
	 *            The group's id
	 * 
	 * @return {@link ExternalGroup}
	 */
	public ExternalGroup getExternalGroupById(int groupId);

	/**
	 * Checks if public group has public forms
	 * 
	 * @param groupId
	 *            The group's id to check if it has forms
	 * 
	 * @return {@link Long}
	 */
	public Long checkIfPublicGroupHasForms(int groupId);

	/**
	 * Updates an external group
	 * 
	 * @param externalGroup
	 *            The external group to be updated
	 * 
	 * @return {@link ExternalGroup}
	 */
	public ExternalGroup updatePublicGroup(ExternalGroup externalGroup);

	/**
	 * Returns a list of in progress instances (status = running)
	 * 
	 * @return List of {@link WorkflowInstance}
	 */
	public List<WorkflowInstance> getInProgressInstances();

	/**
	 * Save or update an external user
	 * 
	 * @param externalUser
	 *            The external user to be saved/updated
	 */
	public void saveExternalUser(ExternalUser externalUser);

	/**
	 * Returns an external user by device id
	 * 
	 * @param deviceId
	 *            The external user's device id
	 * 
	 * @return {@link ExternalUser}
	 */
	public ExternalUser getExternalUserByDeviceId(String deviceId);

	/**
	 * Returns definition version by id
	 * 
	 * @param definitionVersionId
	 *            Definition version's id
	 * 
	 * @return {@link DefinitionVersion}
	 */
	public DefinitionVersion getDefinitionVersionById(Integer definitionVersionId);

	/**
	 * 
	 * @param groupId
	 * @return
	 */
	public Owner getOwnerById(String ownerId);

	/**
	 * Returns all available owners
	 * 
	 * @return List of {@link Owner}
	 */
	public List<Owner> getOwners();

	/**
	 * 
	 * @param ownerId
	 */
	public void deleteOwnerByOwnerId(String ownerId);

	/**
	 * 
	 * @param owner
	 * @return
	 */
	public Owner saveOwner(Owner owner);
	
	/**
	 * Check if owner exists
	 * 
	 * @param ownerId
	 * @return
	 */
	public boolean isOWnerExist(String ownerId);

	/**
	 * 
	 * @return
	 */
	public List<Role> getRoles();
	
	/**
	 * Check if role exists
	 * 
	 * @param roleId
	 * @return
	 */
	public boolean isRoleExist(String roleId);

	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public Role getRoleByRoleId(String roleId);

	/**
	 * 
	 * @param role
	 * @return
	 */
	public Role saveRole(Role role);

	/**
	 * 
	 * @param roleId
	 */
	public void deleteRoleByRoleId(String roleId);
	
}
