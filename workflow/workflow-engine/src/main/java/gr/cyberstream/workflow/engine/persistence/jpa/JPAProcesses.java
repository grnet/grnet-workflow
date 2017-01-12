package gr.cyberstream.workflow.engine.persistence.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.TaskPath;
import gr.cyberstream.workflow.engine.model.UserTaskDetails;
import gr.cyberstream.workflow.engine.model.UserTaskFormElement;
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

	@Override
	public List<WorkflowDefinition> getAll() {
		
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery("select p from WorkflowDefinition p", WorkflowDefinition.class);
		
		return query.getResultList();
	}

	@Override
	public List<WorkflowDefinition> getActiveProcessDefintions() {
		return entityManager.createQuery("select p from WorkflowDefinition p " + "where p.activeDeploymentId != ''", WorkflowDefinition.class).getResultList();
	}

	@Override
	public WorkflowDefinition save(WorkflowDefinition workflowDefinition) {

		if (entityManager.contains(workflowDefinition)) {
			entityManager.persist(workflowDefinition);
		} else {

			// if process is not managed update versions to reference the
			// definition
			// (the source of the process is possibly a RESTful request)

			if (workflowDefinition.getDefinitionVersions() != null)
				for (DefinitionVersion version : workflowDefinition.getDefinitionVersions()) {
					version.setWorkflowDefinition(workflowDefinition);
					version.setProcessDefinitionId(workflowDefinition.getKey());
				}

			workflowDefinition = entityManager.merge(workflowDefinition);
		}
		return workflowDefinition;
	}

	/**
	 * Return the workflow definition for the given id
	 */
	@Override
	public WorkflowDefinition getById(int id) {
		
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery("select p from WorkflowDefinition p where p.id = :id", WorkflowDefinition.class);
		
		return query.setParameter("id", id).getSingleResult();
	}

	/**
	 * Return the workflow definition for the given name
	 */
	@Override
	public WorkflowDefinition getByName(String name) {
		
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery("select p from WorkflowDefinition p where p.name = :name", WorkflowDefinition.class);

		return query.setParameter("name", name).getSingleResult();
	}

	@Override
	public void delete(int processId) {
		WorkflowDefinition process = getById(processId);

		if (process != null)
			entityManager.remove(process);
	}

	@Override
	public DefinitionVersion saveVersion(int processId, DefinitionVersion definitionVersion) {
		WorkflowDefinition definition = getById(processId);
		definitionVersion.setWorkflowDefinition(definition);

		if (definitionVersion.getProcessDefinitionId() == null) {
			definitionVersion.setProcessDefinitionId(definition.getKey());
		}

		entityManager.merge(definitionVersion);
		return definitionVersion;
	}

	@Override
	public List<String> getProcessDefinitionIDs(List<Integer> processIds) {
		
		TypedQuery<String> query = entityManager.createQuery("select v.processDefinitionId from DefinitionVersion v where v.workflowDefinition.id in :ids", String.class);
		query.setParameter("ids", processIds);
		
		return query.getResultList();
	}

	@Override
	public DefinitionVersion getVersionByDeploymentId(String deploymentId) {
		
		TypedQuery<DefinitionVersion> query = entityManager.createQuery("select v from DefinitionVersion v where v.deploymentId = :id", DefinitionVersion.class);
		query.setParameter("id", deploymentId);
		
		return query.getSingleResult();
	}

	@Override
	public List<DefinitionVersion> getVersionsByProcessId(int processId) {
		
		TypedQuery<DefinitionVersion> query = entityManager.createQuery("select v from DefinitionVersion v where v.workflowDefinition.id = :id", DefinitionVersion.class);
		query.setParameter("id", processId);
		
		return query.getResultList();
	}

	@Override
	public DefinitionVersion getVersionByProcessDefinitionId(String processDefinitionId) {
		
		TypedQuery<DefinitionVersion> query = entityManager.createQuery("select v from DefinitionVersion v where v.processDefinitionId = :id", DefinitionVersion.class);
		query.setParameter("id", processDefinitionId);
		
		return query.getSingleResult();
	}

	@Override
	public List<WorkflowDefinition> getDefinitionsByOwner(String ownerName) {
		
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery("select p from WorkflowDefinition p where p.owner = :processOwner", WorkflowDefinition.class);
		query.setParameter("processOwner", ownerName);
		
		return query.getResultList();
	}

	@Override
	public List<WorkflowDefinition> getDefinitionsByOwners(List<String> owners) {
		
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery("select p from WorkflowDefinition p where p.owner in :owners", WorkflowDefinition.class);
		query.setParameter("owners", owners);
		
		return query.getResultList();
	}

	@Override
	public UserTaskDetails save(UserTaskDetails userTaskDetails) {
		
		if (entityManager.contains(userTaskDetails))
			entityManager.persist(userTaskDetails);
		else
			userTaskDetails = entityManager.merge(userTaskDetails);
		
		return userTaskDetails;
	}

	@Override
	public WorkflowInstance save(WorkflowInstance instance) {

		if (entityManager.contains(instance))
			entityManager.persist(instance);
		else
			instance = entityManager.merge(instance);

		return instance;
	}

	@Override
	public WorkflowInstance getInstanceById(String instanceId) {
		
		TypedQuery<WorkflowInstance> query = entityManager.createQuery("select inst from WorkflowInstance inst where inst.id = :id", WorkflowInstance.class);
		query.setParameter("id", instanceId);

		return query.getSingleResult();
	}

	@Override
	public DefinitionVersion getVersionById(int versionId) {
		
		TypedQuery<DefinitionVersion> query = entityManager.createQuery("select v from DefinitionVersion v where v.id = :id", DefinitionVersion.class);
		query.setParameter("id", versionId);
		
		return query.getSingleResult();
	}

	@Override
	public List<UserTaskDetails> getVersionTaskDetails(int versionId) {
		
		TypedQuery<UserTaskDetails> query = entityManager.createQuery("select d from UserTaskDetails d where d.definitionVersion.id = :versionId", UserTaskDetails.class);
		query.setParameter("versionId", versionId);

		return query.getResultList();
	}

	@Override
	public Long getCheckName(WorkflowDefinition definition) {
		
		TypedQuery<Long> query = entityManager.createQuery("select count(*) from WorkflowDefinition p where p.name = :name and p.id <> :id", Long.class);
		query.setParameter("name", definition.getName());
		query.setParameter("id", definition.getId());

		return query.getSingleResult();
	}

	@Override
	public List<WorkflowInstance> getSupervisedProcesses(String userId) {
		
		TypedQuery<WorkflowInstance> query = entityManager.createQuery("select i from WorkflowInstance i where i.supervisor = :supervisorId", WorkflowInstance.class);
		query.setParameter("supervisorId", userId);
		
		return query.getResultList();
	}

	@Override
	public List<WorkflowInstance> getActiveProcessInstances(int id) {
		
		TypedQuery<WorkflowInstance> query = entityManager.createQuery("select i from WorkflowInstance i inner join i.definitionVersion v where " 
				+ "v.workflowDefinition.id=:id and i.status!=:status", WorkflowInstance.class);

		query.setParameter("id", id);
		query.setParameter("status", WorkflowInstance.STATUS_ENDED);

		return query.getResultList();
	}

	@Override
	public WorkflowDefinition getDefinitionByKey(String definitionKey) {
		
		TypedQuery<WorkflowDefinition> query = entityManager.createQuery("select d from WorkflowDefinition d where d.key = :key", WorkflowDefinition.class);
		query.setParameter("key", definitionKey);
		
		return query.getSingleResult();
	}

	@Override
	public UserTaskDetails getUserTaskDetailsById(int id) {
		
		TypedQuery<UserTaskDetails> query = entityManager.createQuery("select t from UserTaskDetails t where t.id = :id", UserTaskDetails.class);
		query.setParameter("id", id);
		
		return query.getSingleResult();
	}

	@Override
	public WorkflowInstance getProcessInstance(String id) {
		
		TypedQuery<WorkflowInstance> query = entityManager.createQuery("select i from WorkflowInstance i where i.id = :id", WorkflowInstance.class);
		query.setParameter("id", id);
		
		return query.getSingleResult();
	}

	@Override
	public Long getCheckInstanceName(String instanceTitle) {
		
		TypedQuery<Long> query = entityManager.createQuery("select count(*) from WorkflowInstance i where i.title = :title", Long.class);
		query.setParameter("title", instanceTitle);
		
		return query.getSingleResult();
	}

	@Override
	public void cancelProcessInstance(WorkflowInstance instance) {
		
		entityManager.remove(instance);
	}

	@Override
	public List<UserTaskDetails> getUserTaskDetailsByDefinitionKey(String key, int definitionId) {

		TypedQuery<UserTaskDetails> query = entityManager.createQuery("select d from UserTaskDetails d where d.taskId = :key and d.definitionVersion.id = :definitionId", UserTaskDetails.class);

		query.setParameter("key", key);
		query.setParameter("definitionId", definitionId);

		return query.getResultList();
	}

	@Override
	public WorkflowDefinition getProcessByDefinitionId(String processDefinitionId) {
		TypedQuery<WorkflowDefinition> query = entityManager .createQuery("select def from DefinitionVersion ver join ver.workflowDefinition as def "
				+ "where ver.processDefinitionId = :defid", WorkflowDefinition.class);
		
		query.setParameter("defid", processDefinitionId);

		return query.getSingleResult();
	}

	@Override
	public TaskPath getTaskPath(String instanceId, String taskId) {
		TypedQuery<TaskPath> query = entityManager.createQuery(
				"SELECT new gr.cyberstream.workflow.engine.model.TaskPath(det, inst, ver, def) " + " from "
						+ " WorkflowInstance as inst join " + " inst.definitionVersion as ver join "
						+ " ver.workflowDefinition as def, "
						+ " UserTaskDetails as det join det.definitionVersion as detVer" + " where "
						+ " detVer.id = ver.id and " + " inst.id = :instanceId " + " and det.taskId = :taskId ",
				TaskPath.class);

		query.setParameter("instanceId", instanceId);
		query.setParameter("taskId", taskId);

		return query.getSingleResult();
	}

	@Override
	public WorkflowSettings getSettings() {
		
		TypedQuery<WorkflowSettings> query = entityManager.createQuery("select s from WorkflowSettings s where s.id = :id", WorkflowSettings.class);
		query.setParameter("id", 1);
		
		return query.getSingleResult();
	}

	@Override
	public WorkflowSettings updateSettings(WorkflowSettings settings) {
		
		return entityManager.merge(settings);
	}

	@Override
	public UserTaskFormElement save(UserTaskFormElement taskFormElement) {

		if (entityManager.contains(taskFormElement))
			entityManager.persist(taskFormElement);
		else
			taskFormElement = entityManager.merge(taskFormElement);

		return taskFormElement;
	}

	@Override
	public List<UserTaskFormElement> getUserTaskFormElements(String elementId, int userTaskDetailId) {
		TypedQuery<UserTaskFormElement> query = entityManager
				.createQuery("select elem from UserTaskFormElement elem where elem.elementId = :elementId "
						+ "and elem.userTaskDetail.id = :userTaskDetailId", UserTaskFormElement.class);

		query.setParameter("elementId", elementId);
		query.setParameter("userTaskDetailId", userTaskDetailId);

		return query.getResultList();
	}

	@Override
	public List<UserTaskFormElement> getUserTaskFromElements(String definitionVersion, int taskDetailId) {
		TypedQuery<UserTaskFormElement> query = entityManager.createQuery("select elem from UserTaskFormElement elem "
				+ "inner join elem.userTaskDetail as det " + "inner join det.definitionVersion as ver "
				+ "where ver.processDefinitionId = :definitionVersion " + "and elem.userTaskDetail.id = :taskDetailId",
				UserTaskFormElement.class);

		query.setParameter("definitionVersion", definitionVersion);
		query.setParameter("taskDetailId", taskDetailId);
		return query.getResultList();
	}

	@Override
	public UserTaskFormElement getUserTaskFromElement(String definitionVersion, String taskDefintionKey, String elementId) {

		TypedQuery<UserTaskFormElement> query = entityManager.createQuery("select elem from UserTaskFormElement elem "
				+ "inner join elem.userTaskDetail as det " + "inner join det.definitionVersion as ver "
				+ "where ver.processDefinitionId = :definitionVersion " + "and det.taskId = :taskId "
				+ "and elem.elementId = :elementId", UserTaskFormElement.class);

		query.setParameter("definitionVersion", definitionVersion);
		query.setParameter("elementId", elementId);
		query.setParameter("taskId", taskDefintionKey);
		return query.getSingleResult();
	}

	@Override
	public UserTaskDetails getUserTaskDetailByDefinitionKey(String key, String definitionVersionKey) {

		TypedQuery<UserTaskDetails> query = entityManager.createQuery(
				"select det from UserTaskDetails det where det.taskId = :key "
						+ "and det.definitionVersion.processDefinitionId = :definitionVersionKey", UserTaskDetails.class);

		query.setParameter("key", key);
		query.setParameter("definitionVersionKey", definitionVersionKey);

		return query.getSingleResult();
	}

	@Override
	public List<WorkflowInstance> getInstancesByDefinitionVersionId(int id) {
		
		TypedQuery<WorkflowInstance> query = entityManager.createQuery("select inst from WorkflowInstance inst where inst.definitionVersion.id = :definitionVersionId", WorkflowInstance.class);
		query.setParameter("definitionVersionId", id);
		
		return query.getResultList();
	}

	@Override
	public void deleteProcessInstance(String instanceId) {
		
		Query query = entityManager.createQuery("delete from WorkflowInstance inst where inst.id = :instanceId");
		query.setParameter("instanceId", instanceId);
		
		query.executeUpdate();
	}

	@Override
	public List<WorkflowInstance> getInProgressInstances() {

		TypedQuery<WorkflowInstance> query = entityManager.createQuery("select inst from WorkflowInstance inst where inst.status='running' ", WorkflowInstance.class);

		return query.getResultList();
	}

	@Override
	public List<WorkflowInstance> getEndedProgressInstances() {

		TypedQuery<WorkflowInstance> query = entityManager.createQuery("select inst from WorkflowInstance inst where inst.status='ended' or inst.status='deleted'", WorkflowInstance.class);

		return query.getResultList();
	}

	@Override
	public DefinitionVersion getDefinitionVersionById(Integer definitionVersionId) {
		
		TypedQuery<DefinitionVersion> query = entityManager.createQuery("select defVer from DefinitionVersion defVer where defVer.id = :definitionVersionId", DefinitionVersion.class);

		query.setParameter("definitionVersionId", definitionVersionId);
		return query.getSingleResult();
	}

}
