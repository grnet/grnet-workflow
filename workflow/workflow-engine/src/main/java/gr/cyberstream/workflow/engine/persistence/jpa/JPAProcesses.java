/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.persistence.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.ExternalForm;
import gr.cyberstream.workflow.engine.model.Registry;
import gr.cyberstream.workflow.engine.model.TaskPath;
import gr.cyberstream.workflow.engine.model.UserTaskDetails;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.persistence.Processes;

/**
 * Implements the Processes Repository
 * 
 * @author nlyk
 *
 */

@Repository
public class JPAProcesses implements Processes {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Returns all workflow definitions
	 */
	@Override
	@Transactional
	public List<WorkflowDefinition> getAll() {
		return entityManager.createQuery("select p from WorkflowDefinition p", WorkflowDefinition.class)
				.getResultList();
	}

	/**
	 * Saves a new workflow definition to the database
	 */
	@Override
	@Transactional
	public WorkflowDefinition save(WorkflowDefinition process) {
		
		if (entityManager.contains(process)) {
			entityManager.persist(process);
		} else {

			// if process is not managed update versions to reference the definition
			// (the source of the process is possibly a RESTful request)

			if (process.getDefinitionVersions() != null)
				for (DefinitionVersion version : process.getDefinitionVersions()) {
					version.setWorkflowDefinition(process);
					version.setProcessDefinitionId(process.getKey());
				}
			
			process = entityManager.merge(process);
		}
		return process;
	}
	
	/**
	 * Return the workflow definition for the given id
	 */
	@Override
	public WorkflowDefinition getById(int id) {
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery(
				"select p from WorkflowDefinition p where p.id = :id", WorkflowDefinition.class);
		return query.setParameter("id", id).getSingleResult();
	}
	
	/**
	 * Return the workflow definition for the given name
	 */
	@Override
	public WorkflowDefinition getByName(String name) {		
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery(
				"select p from WorkflowDefinition p where p.name = :name", WorkflowDefinition.class);
		
		return query.setParameter("name", name).getSingleResult();
	}

	/**
	 * Deletes the workflow definition with the given id
	 */
	@Override
	@Transactional
	public void delete(int processId) {
		WorkflowDefinition process = this.getById(processId);

		if (process != null) {
			entityManager.remove(process);
		}
	}

	/**
	 * Updates the workflow definition version
	 */
	@Override
	@Transactional
	public DefinitionVersion saveVersion(int processId, DefinitionVersion definitionVersion) {
		WorkflowDefinition definition = getById(processId);
		definitionVersion.setWorkflowDefinition(definition);
		
		if (definitionVersion.getProcessDefinitionId() == null) {
			definitionVersion.setProcessDefinitionId(definition.getKey());
		}

		entityManager.merge(definitionVersion);
		return definitionVersion;
	}

	/**
	 * Return the process definition IDs (Keys) for the workflow definition IDs
	 */
	@Override
	public List<String> getProcessDefinitionIDs(List<Integer> processIds) {
		TypedQuery<String> query = entityManager.createQuery(
				"select v.processDefinitionId from DefinitionVersion v where v.workflowDefinition.id in :ids", String.class);
		return query.setParameter("ids", processIds).getResultList();
	}

	/**
	 * Returns the process definition version using the deployment id
	*/
	@Override
	public DefinitionVersion getVersionByDeploymentId(String deploymentId) {
		TypedQuery<DefinitionVersion> query = entityManager.createQuery(
				"select v from DefinitionVersion v where v.deploymentId = :id", DefinitionVersion.class);
		return query.setParameter("id", deploymentId).getSingleResult();
	}

	/**
	 * Returns the process definition versions using the process id
	*/
	@Override
	public List<DefinitionVersion> getVersionsByProcessId(int processId) {
		TypedQuery<DefinitionVersion> query = entityManager.createQuery(
				"select v from DefinitionVersion v where v.workflowDefinition.id = :id", DefinitionVersion.class);
		return query.setParameter("id", processId).getResultList();
	}

	/**
	 * Returns the process definition versions using the process definition id
	 */
	@Override
	public DefinitionVersion getVersionByProcessDefinitionId(String processDefinitionId) {
		TypedQuery<DefinitionVersion> query = entityManager.createQuery(
				"select v from DefinitionVersion v where v.processDefinitionId = :id", DefinitionVersion.class);
		return query.setParameter("id", processDefinitionId).getSingleResult();
	}

	/**
	 * 
	 * @param ownerName
	 * @return A list of Workflow Definitions by owner
	 */
	@Override
	public List<WorkflowDefinition> getDefinitionsByOwner(String ownerName) {
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery(
				"select p from WorkflowDefinition p where p.owner = :processOwner", WorkflowDefinition.class);
		return query.setParameter("processOwner", ownerName).getResultList();
	}
	
	/**
	 * Get definitions by owners
	 * 
	 * @param ownerName
	 * @return A list of Workflow Definitions by owner
	 */
	@Override
	public List<WorkflowDefinition> getDefinitionsByOwners(List<String> owners) {
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery(
				"select p from WorkflowDefinition p where p.owner in :owners", WorkflowDefinition.class);
		return query.setParameter("owners", owners).getResultList();
	}
	
	/**
	 * Saves a new UserTaskDetails object 
	 */
	@Override
	public UserTaskDetails save(UserTaskDetails userTaskDetails) {
		if (entityManager.contains(userTaskDetails)) {
			entityManager.persist(userTaskDetails);
		} 
		else userTaskDetails = entityManager.merge(userTaskDetails);
		return userTaskDetails;
	}

	/**
	 * Saves a new process instance to the database
	 */
	@Override
	@Transactional
	public WorkflowInstance save(WorkflowInstance instance) {
		
		if (entityManager.contains(instance)) {
			
			entityManager.persist(instance);
			
		} else {

			instance = entityManager.merge(instance);
		}
		
		return instance;
	}
	
	public WorkflowInstance getInstanceById(String instanceId) {
		
		TypedQuery<WorkflowInstance> query = entityManager.createQuery(
				"select i from WorkflowInstance i where i.id = :id", WorkflowInstance.class);
		WorkflowInstance instance = query.setParameter("id", instanceId).getSingleResult();
		return instance;
	}

	/**
	 * 
	 */
	@Override
	public DefinitionVersion getVersionById(int versionId) {
		TypedQuery<DefinitionVersion> query = entityManager.createQuery(
				"select v from DefinitionVersion v where v.id = :id", DefinitionVersion.class);
		return query.setParameter("id", versionId).getSingleResult();
	}
	
	//TODO:vpap
	@Override
	@Transactional
	public List<UserTaskDetails> getVersionTaskDetails(int versionId) {
		TypedQuery<UserTaskDetails> query = entityManager.createQuery(
				"select d from UserTaskDetails d where d.definitionVersion.id = :versionId", UserTaskDetails.class);
		
		return query.setParameter("versionId", versionId).getResultList();
	}

	@Override
	public Long getCheckName(WorkflowDefinition definition) {
		TypedQuery<Long> query = entityManager.createQuery(
				"select count(*) from WorkflowDefinition p where p.name = :name and p.id <> :id", Long.class);
		
		query.setParameter("name", definition.getName());
		query.setParameter("id", definition.getId());
		
		return query.getSingleResult();
	}


	/**
	 * Returns supervised process by authenticated user
	 */
	@Override
	public List<WorkflowInstance> getSupervisedProcesses(String userId) {
		TypedQuery<WorkflowInstance> query = entityManager.createQuery(
				"select i from WorkflowInstance i where i.supervisor = :supervisorId", WorkflowInstance.class);
		return query.setParameter("supervisorId", userId).getResultList();
	}

	/**
	 * Returns all instances of a process
	 */
	@Override
	public List<WorkflowInstance> getActiveProcessInstances(int id) {
		/*select i.title from instance i join definitionversion dv 
		on i.definition_version_id = dv.id where dv.workflow_definition_id=? and i.status=?*/
		TypedQuery<WorkflowInstance> query = entityManager.
				createQuery("select i from WorkflowInstance i inner join i.definitionVersion v where "
						+ "v.workflowDefinition.id=:id and i.status!=:status", WorkflowInstance.class);
		
		query.setParameter("id", id);
		query.setParameter("status",WorkflowInstance.STATUS_ENDED);
		
		List<WorkflowInstance> workflowInstances = query.getResultList();
		
		return workflowInstances;
	}

	@Override
	public WorkflowDefinition getDefinitionByKey(String definitionKey) {
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery(
				"select d from WorkflowDefinition d where d.key = :key", WorkflowDefinition.class);
		return query.setParameter("key", definitionKey).getSingleResult();	
	}
	
	/**
	 * Returns the UserTaskDetails entity with the specified id
	 */
	@Override
	public UserTaskDetails getUserTaskDetailsById(int id) {
		TypedQuery<UserTaskDetails> query = entityManager.createQuery(
				"select t from UserTaskDetails t where t.id = :id", UserTaskDetails.class);
		return query.setParameter("id", id).getSingleResult();
	}

	/**
	 * Returns the process instance with the specified id
	 */
	@Override
	public WorkflowInstance getProcessInstance(String id) {
		TypedQuery<WorkflowInstance> query = entityManager.createQuery(
				"select i from WorkflowInstance i where i.id = :id", WorkflowInstance.class);
		return query.setParameter("id", id).getSingleResult();
	}
	
	@Override
	public Long getCheckInstanceName(String instanceTitle) {
		TypedQuery<Long> query = entityManager.createQuery(
				"select count(*) from WorkflowInstance i where i.title = :title", Long.class);
		query.setParameter("title", instanceTitle);
		return query.getSingleResult();
	}

	/**
	 * Deletes a process instance.
	 */
	@Override
	public void cancelProcessInstance(WorkflowInstance instance) {
		entityManager.remove(instance);
	}
	
	/**
	 * Returns task details for a task
	 * @param key
	 * @return
	 */
	@Override
	public List<UserTaskDetails> getUserTaskDetailsByDefinitionKey(String key, int definitionId) {
		
		TypedQuery <UserTaskDetails> query = entityManager.createQuery(
				"select d from UserTaskDetails d where d.taskId = :key and d.definitionVersion.id = :definitionId", UserTaskDetails.class);
		
		query.setParameter("key", key);
		query.setParameter("definitionId", definitionId);
		
		return query.getResultList();
	}
	
	/**
	 * Returns a WorkflwoDefinition entity with the specified processDefinitionId.
	 * 
	 */
	@Override
	public WorkflowDefinition getProcessByDefinitionId(String processDefinitionId) {
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery(
				"select wd from WorkflowDefinition wd where wd.key = :defid", WorkflowDefinition.class);
		return query.setParameter("defid", processDefinitionId).getSingleResult();
	}
	
	
	/**
	 * Get the path for task
	 */
	@Override
	public TaskPath getTaskPath(String instanceId, String taskId) {
		TypedQuery<TaskPath> query = entityManager.createQuery(
				"SELECT new gr.cyberstream.workflow.engine.model.TaskPath(det, inst, ver, def) "
				+ " from "
				+ " WorkflowInstance as inst join "
				+ " inst.definitionVersion as ver join "
				+ " ver.workflowDefinition as def, "
				+ " UserTaskDetails as det join det.definitionVersion as detVer"
				+ " where "
				+ " detVer.id = ver.id and "
				+ " inst.id = :instanceId "
				+ " and det.taskId = :taskId ", 
				TaskPath.class);
		
		query.setParameter("instanceId", instanceId);
		query.setParameter("taskId", taskId);
		
		return query.getSingleResult();
	}
	
	@Override
	public ExternalForm getFormById(String formId) {
		
		TypedQuery<ExternalForm> query = entityManager.createQuery(
				"select f from ExternalForm f where f.id = :formId", ExternalForm.class);
		return query.setParameter("formId", formId).getSingleResult();	
	}
	
	/**
	 * Update or create registry entry to the database
	 */
	@Override
	@Transactional
	public void update(Registry registry) {
		if(entityManager.contains(registry))
			entityManager.persist(registry);
		else
			entityManager.merge(registry);
	}

	/**
	 * Returns the settings values as they are stored in database
	 * @return
	 */
	@Override
	public WorkflowSettings getSettings() {
		TypedQuery<WorkflowSettings> query = entityManager.createQuery(
				"select s from WorkflowSettings s where s.id = :id", WorkflowSettings.class);
		return query.setParameter("id", 1).getSingleResult();
	}

	/**
	 * Updates the settings
	 * @param settings
	 * @return
	 */
	@Override
	@Transactional
	public WorkflowSettings updateSettings(WorkflowSettings settings) {
		return entityManager.merge(settings);
	}

	/**
	 * 	Fetch and return external forms of the process specified by the id.
	 */
	@Override
	public List<ExternalForm> getProcessExternalForms(int id) {
		TypedQuery<ExternalForm> query = entityManager.createQuery(
				"select e from ExternalForm e where e.workflowDefinition.id = :id", ExternalForm.class);
		
		return query.setParameter("id", id).getResultList();
	}

	/**
	 * Fetches all registries
	 */
	@Override
	public List<Registry> getRegistries() {
		return entityManager.createQuery("select r from Registry r", Registry.class)
				.getResultList();
	}

	@Override
	@Transactional
	public ExternalForm saveExternalForm(ExternalForm xform) {
		
		if (entityManager.contains(xform)) {
			
			entityManager.persist(xform);
			
		} else {

			xform = entityManager.merge(xform);
		}
		return xform;
	}

	/**
	 * Retrieve external form with the specific id
	 */
	@Override
	public ExternalForm getExternalForm(String id) {
		TypedQuery<ExternalForm> query = entityManager.createQuery(
				"select x from ExternalForm x where x.id = :id", ExternalForm.class);
		return query.setParameter("id", id).getSingleResult();
	}

	/**
	 * Delete an external form
	 */
	@Override
	@Transactional
	public void deleteExternalForm(ExternalForm xform) {
		// the check for null object has been done on the service layer 
		entityManager.remove(xform);		
	}
	

	/**
	 * Check whether an external form with the same id exists in the database
	 */
	@Override
	public Long checkForExternalForm(String id) {
		TypedQuery<Long> query = entityManager.createQuery(
				"select count(*) from ExternalForm x where x.id = :id", Long.class);
		query.setParameter("id", id);
		return query.getSingleResult();
	}

	/**
	 * Deletes a registry
	 */
	@Override
    @Transactional
	public void deleteRegistry(String registryId) {
		Query query = entityManager.createQuery(
			      "delete from Registry reg where reg.id = :id");
		query.setParameter("id", registryId);
		query.executeUpdate();
	}

	@Override
	public Long checkIfRegistryExists(String registryId) {
		TypedQuery<Long> query = entityManager.createQuery(
				"select count(*) from Registry reg where reg.id = :id", Long.class);
		query.setParameter("id", registryId);
		return query.getSingleResult();
	}

	@Override
	public Long checkIfDefinitionHasRegistry(String registryId) {
		TypedQuery<Long> query = entityManager.createQuery(
				"select count(*) from WorkflowDefinition def where def.registry.id = :registryId", Long.class);
		query.setParameter("registryId", registryId);
		return query.getSingleResult();
	}

	@Override
	public Registry getRegistryById(String registryId) {
		TypedQuery<Registry> query = entityManager.createQuery(
				"select reg from Registry reg where reg.id = :registryId", Registry.class);
		query.setParameter("registryId", registryId);
		return query.getSingleResult();
	}
	
}
