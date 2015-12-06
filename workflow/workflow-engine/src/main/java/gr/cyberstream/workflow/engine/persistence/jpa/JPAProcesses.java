/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.persistence.jpa;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.persistence.Processes;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

			for (DefinitionVersion version : process.getDefinitionVersions()) {
				version.setWorkflowDefinition(process);
				version.setProcessDefinitionId(process.getKey());
			}
			entityManager.merge(process);
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
		
		System.out.println(processIds);
		
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
				"select v from DefinitionVersion v where v.processDefinition.id = :id", DefinitionVersion.class);
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

}
