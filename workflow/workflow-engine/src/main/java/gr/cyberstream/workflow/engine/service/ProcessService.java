/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gr.cyberstream.util.string.StringUtil;
import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowDefinitionStatus;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.persistence.Processes;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements all the business rules related to process definitions and process instances
 * 
 * @author nlyk
 *
 */
@Service
public class ProcessService {

	final static Logger logger = LoggerFactory.getLogger(ProcessService.class);

	@Autowired
	Processes processRepository;

	@Autowired
	RepositoryService activitiRepositorySrv;

	@Autowired
	HistoryService activitiHistorySrv;

	@Autowired
	FormService activitiFormSrv;

	@Autowired
	TaskService activitiTaskSrv;

	/**
	 * Creates a new process definition from just its metadata. No BPMN definition is attached yet.
	 * 
	 * @param process
	 *            the metadata of the process
	 * @return the saved process definition
	 * @throws InvalidRequestException
	 */
	public WorkflowDefinition createNewProcessDefinition(WorkflowDefinition process) throws InvalidRequestException {

		// 1. apply some rules
		if (StringUtil.isEmpty(process.getName())) {
			throw new InvalidRequestException("the name is required for the new process definition");
		}

		// 2. Initialize process definition
		process.setActiveDeploymentId(null);

		// 3. ask repository to save the new process definition
		return processRepository.save(process);
	}

	/**
	 * Creates a new process definition based on an uploaded BPMN file. If the BPMN definition deploys successfully to
	 * Activiti repository service, a new ProcessDefinition object is created and saved in Process definitions
	 * repository.
	 * 
	 * @param inputStream
	 *            the input BPMN XML definition
	 * @param filename
	 * @return the newly created process definition
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WorkflowDefinition createNewProcessDefinition(InputStream inputStream, String filename)
			throws InvalidRequestException {

		Deployment deployment;

		// 1. Deploy the BPMN file to Activiti repository service ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		filename = StringUtil.isEmpty(filename) ? "noname.bpmn20.xml" : filename;

		try {
			deployment = activitiRepositorySrv.createDeployment().addInputStream("input.bpmn20.xml", inputStream)
					.name(filename).deploy();
		} catch (XMLException | ActivitiIllegalArgumentException ex) {
			String message = "The BPMN input is not valid. Error string: " + ex.getMessage();
			logger.error(message);
			throw new InvalidRequestException(message);
		}

		// 2. Check deployment and get metadata from the deployed process definition ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if (deployment == null) {
			logger.error("BPMN file error");
			throw new InvalidRequestException("The BPMN input is not valid");
		}

		logger.info("New BPMN deployment: " + deployment.getName());

		ProcessDefinition processDef = ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositorySrv,
				deployment.getId());

		if (processDef == null) {
			logger.error("BPMN file error");
			throw new InvalidRequestException("The BPMN input is not valid");
		}

		WorkflowDefinition workflow = new WorkflowDefinition();
		workflow.setKey(processDef.getId());
		workflow.setDescription(processDef.getDescription());

		String definitionName = processDef.getName();
		workflow.setName((definitionName == null) ? "you must name it" : definitionName);

		DefinitionVersion definitionVersion = new DefinitionVersion();
		definitionVersion.setDeploymentId(deployment.getId());
		definitionVersion.setStatus(WorkflowDefinitionStatus.NEW.toString());
		definitionVersion.setVersion(processDef.getVersion());
		definitionVersion.setDeploymentdate(deployment.getDeploymentTime());
		definitionVersion.setProcessDefinitionId(processDef.getKey());

		workflow.addDefinitionVersion(definitionVersion);

		// 3. save the new process definition ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		return processRepository.save(workflow);
	}

	/**
	 * 
	 * @param id
	 * @param inputStream
	 * @param originalFilename
	 * @return
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public DefinitionVersion createNewProcessVersion(int id, InputStream inputStream, String filename)
			throws InvalidRequestException {

		Deployment deployment;
		ProcessDefinition processDef;

		try {
			deployment = ActivitiHelper.createDeployment(activitiRepositorySrv, inputStream, filename);
		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}

		logger.info("New BPMN deployment: " + deployment.getName());

		try {
			processDef = ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositorySrv, deployment.getId());
		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}

		WorkflowDefinition workflow = processRepository.getById(id);

		DefinitionVersion definitionVersion = new DefinitionVersion();
		definitionVersion.setDeploymentId(deployment.getId());
		definitionVersion.setStatus(WorkflowDefinitionStatus.NEW.toString());
		definitionVersion.setVersion(processDef.getVersion());
		definitionVersion.setDeploymentdate(deployment.getDeploymentTime());
		definitionVersion.setProcessDefinitionId(
				ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositorySrv, deployment.getId()).getId());

		workflow.addDefinitionVersion(definitionVersion);

		processRepository.save(workflow);

		return definitionVersion;
	}

	/**
	 * Create an image with the diagram of the process definition
	 * 
	 * @param processId
	 *            the id of process
	 * @return
	 */
	public InputStreamResource getProcessDiagram(int processId) {

		WorkflowDefinition process = processRepository.getById(processId);

		ProcessDefinition processDefinition = activitiRepositorySrv.createProcessDefinitionQuery()
				.processDefinitionId(process.getKey()).singleResult();

		ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
		BpmnModel bpmnModel = activitiRepositorySrv.getBpmnModel(processDefinition.getId());

		if (bpmnModel.getLocationMap().size() == 0) {
			BpmnAutoLayout autoLayout = new BpmnAutoLayout(bpmnModel);
			autoLayout.execute();
		}

		InputStream is = processDiagramGenerator.generateJpgDiagram(bpmnModel);
		return new InputStreamResource(is);
	}

	/**
	 * Deletes all versions of the process. Throw exception if there are instances (active or old ones)
	 * 
	 * @param processId
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteProcessDefinition(int processId) throws InvalidRequestException {

		// get workflow definition
		WorkflowDefinition definition = processRepository.getById(processId);

		if (definition == null) {
			throw new InvalidRequestException("no process with id: " + processId + " found.");
		}

		// check if any of the process deployments have instances
		boolean found = false;
		for (DefinitionVersion version : definition.getDefinitionVersions()) {
			if (activitiHistorySrv.createHistoricProcessInstanceQuery().notDeleted()
					.deploymentId(version.getDeploymentId()).count() > 0) {
				found = true;
			}
		}

		if (found) {
			throw new InvalidRequestException("The process definition with id: " + processId
					+ "could not be deleted. There are associated entries");
		}

		// delete all process definitions (all versions)
		String activeDeploymentId = definition.getActiveDeploymentId();

		boolean activeDeleted = false;
		for (DefinitionVersion version : definition.getDefinitionVersions()) {

			if (version.getDeploymentId().isEmpty()) {
				continue;
			}

			activitiRepositorySrv.deleteDeployment(version.getDeploymentId());
			if (version.getDeploymentId().equals(activeDeploymentId)) {
				activeDeleted = true;
			}
		}

		// delete active deployment if not already deleted
		if (!activeDeleted && activeDeploymentId != null && !activeDeploymentId.isEmpty()) {
			activitiRepositorySrv.deleteDeployment(activeDeploymentId);
		}

		// delete workflow definition entry
		processRepository.delete(processId);
	}

	/**
	 * Deletes the specific version of the process definition. Fail if instances are found
	 * 
	 * @param processId
	 * @param deploymentId
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WorkflowDefinition deleteProcessDefinitionVersion(int processId, String deploymentId)
			throws InvalidRequestException {

		// get workflow definition
		WorkflowDefinition definition = processRepository.getById(processId);

		if (definition == null) {
			throw new InvalidRequestException("no process with id: " + processId + " found.");
		}

		// check if the version id the last one
		if (definition.getDefinitionVersions().size() < 2) {
			throw new InvalidRequestException(
					"Trying to delete the last version. Delete the process definition instead.");
		}

		// check the existence of the deploymentId
		boolean found = false;
		boolean used = false;
		List<DefinitionVersion> versions = definition.getDefinitionVersions();
		for (DefinitionVersion version : versions) {

			if (!version.getDeploymentId().equals(deploymentId)) {
				continue;
			}

			found = true;
			if (activitiHistorySrv.createHistoricProcessInstanceQuery().notDeleted().deploymentId(deploymentId)
					.count() > 0) {
				used = true;
				break;
			}

			versions.remove(version);
			break;
		}

		if (!found) {
			throw new InvalidRequestException("The process definition version with id: " + deploymentId
					+ " does not exist in process " + processId);
		}

		if (used) {
			throw new InvalidRequestException("The process definition version with id: " + deploymentId
					+ "could not be deleted. There are associated entries");
		}

		// delete the deployment
		activitiRepositorySrv.deleteDeployment(deploymentId);

		// remove the version for the process definition
		definition.setDefinitionVersions(versions);

		// update the process definition
		// if the deleted version was the active one, set the active deployment to most recent one
		if (definition.getActiveDeploymentId().equals(deploymentId)) {
			definition.setActiveDeploymentId(definition.getDefinitionVersions().get(0).getDeploymentId());
		}

		return processRepository.save(definition);
	}

	/**
	 * Sets the active version for the workflow definition
	 * 
	 * @param processId
	 *            the workflow definition object
	 * @param versionId
	 *            the id of the version to become active
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WorkflowDefinition setActiveVersion(int processId, int versionId) throws InvalidRequestException {

		WorkflowDefinition definition = processRepository.getById(processId);

		boolean found = false;
		for (DefinitionVersion version : definition.getDefinitionVersions()) {
			if (version.getId() == versionId) {
				version.setStatus(WorkflowDefinitionStatus.ACTIVE.toString());

				definition.setActiveDeploymentId(version.getDeploymentId());
				definition.setKey(ActivitiHelper
						.getProcessDefinitionByDeploymentId(activitiRepositorySrv, version.getDeploymentId()).getId());

				found = true;
			} else {
				if (version.getStatus().equals(WorkflowDefinitionStatus.ACTIVE.toString())) {
					version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());
				}
			}
		}

		if (!found) {
			throw new InvalidRequestException("The process definition version with id: " + versionId
					+ " does not exist in process " + definition.getId());
		}

		processRepository.save(definition);
		return definition;
	}

	/**
	 * Deactivate the version of the workflow definition
	 * 
	 * @param processId
	 *            the process id
	 * @param versionId
	 *            the of the id to be deactivated
	 * @return the modified workflow definition
	 */
	@Transactional(rollbackFor = Exception.class)
	public DefinitionVersion deactivateVersion(int processId, int versionId) throws InvalidRequestException {

		WorkflowDefinition definition = processRepository.getById(processId);
		DefinitionVersion version = definition.getVersion(versionId);
		version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());

		if (definition.getActiveDeploymentId().equals(version.getDeploymentId())) {
			definition.setActiveDeploymentId(null);
			processRepository.save(definition);
		}

		processRepository.saveVersion(processId, version);
		return version;
	}

	/**
	 * Return the full metadata set for the workflow definition
	 * 
	 * @param id
	 *            the id of the workflow definition
	 * @return
	 */
	public WfProcess getProcessMetadata(int id) throws InvalidRequestException {

		WorkflowDefinition definition = processRepository.getById(id);
		WfProcess process = new WfProcess(definition);

		StartFormData startForm = activitiFormSrv.getStartFormData(definition.getKey());
		process.setProcessForm(startForm.getFormProperties());

		return process;
	}

	/**
	 * Start a new process instance with form data
	 * 
	 * @param processId
	 *            the id of the workflow definition
	 * @param formData
	 *            the form data in key-value pairs
	 */
	public ProcessInstance startProcess(int processId, Map<String, String> formData) {

		WorkflowDefinition definition = processRepository.getById(processId);

		return activitiFormSrv.submitStartFormData(definition.getKey(), formData);
	}

	/**
	 * Return all process definitions for the processes supervised by the authenticated user
	 * 
	 * @return
	 */
	public List<WfProcess> getSupervisedProcesses() {

		// TODO: add supervisor and supervisor deputy to workflow definition
		// TODO: get authenticated user and filter workflow definitions accordingly
		// TODO: administrator role gets all processes

		List<WfProcess> result = new ArrayList<WfProcess>();
		List<WorkflowDefinition> definitions = processRepository.getAll();

		for (WorkflowDefinition definition : definitions) {
			result.add(new WfProcess(definition));
		}

		return result;
	}

	/**
	 * Return all unassigned tasks for the provided process IDs
	 * 
	 * @param processIds
	 * @return
	 */
	public List<WfTask> getUnassingedTasksByProcessIds(List<Integer> processIds) {

		// Each process definition may have multiple versions with active instances.
		// Get all process definition IDs (process keys) corresponding to these processes.
		List<String> processKeys = this.getProcessDefinitionKeys(processIds);

		List<Task> tasks = activitiTaskSrv.createTaskQuery().processDefinitionKeyIn(processKeys).active()
				.taskUnassigned().orderByDueDateNullsLast().asc().list();

		return hydrateTasks(tasks);
	}

	/**
	 * Return the process definition keys for a list of process definition IDs
	 * 
	 * @param processIds
	 * @return
	 */
	public List<String> getProcessDefinitionKeys(List<Integer> processIds) {

		List<String> processDefinitionIDs = processRepository.getProcessDefinitionIDs(processIds);
		List<String> processKeys = new ArrayList<String>();

		for (String defId : processDefinitionIDs) {
			processKeys.add(activitiRepositorySrv.getProcessDefinition(defId).getKey());
		}

		return processKeys;
	}

	/**
	 * Return the task
	 * 
	 * @param taskId
	 * @return
	 */
	public WfTask getTask(String taskId) {

		Task task = activitiTaskSrv.createTaskQuery().taskId(taskId).singleResult();
		WfTask wfTask = hydrateTask(task);

		return wfTask;
	}

	/**
	 * Create a new WfTask hydrate an Activiti Task with extra information
	 * 
	 * @param task
	 * @return
	 */
	private WfTask hydrateTask(Task task) {
		WfTask wfTask = new WfTask(task);

		TaskFormData taskForm = activitiFormSrv.getTaskFormData(task.getId());
		wfTask.setTaskForm(taskForm.getFormProperties());

		DefinitionVersion definitionVersion =
				processRepository.getVersionByProcessDefinitionId(task.getProcessDefinitionId());
		wfTask.initFromDefinitionVersion(definitionVersion);
		
		return wfTask;
	}

	/**
	 * Return a hydrated list of WfTask
	 * 
	 * @param task
	 * @return
	 */
	private List<WfTask> hydrateTasks(List<Task> tasks) {
		List<WfTask> wfTasks = new ArrayList<WfTask>();
				
		for (Task task : tasks) {
			wfTasks.add(hydrateTask(task));
		}
		
		return wfTasks;
	}

}
