package gr.cyberstream.workflow.engine.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.persistence.entity.HistoricFormPropertyEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cyberstream.workflow.engine.cmis.CMISDocument;
import gr.cyberstream.workflow.engine.cmis.CMISFolder;
import gr.cyberstream.workflow.engine.config.SettingsStatus;
import gr.cyberstream.workflow.engine.customtypes.ConversationType;
import gr.cyberstream.workflow.engine.customtypes.DocumentType;
import gr.cyberstream.workflow.engine.customtypes.MessageType;
import gr.cyberstream.workflow.engine.listeners.CustomTaskFormFields;
import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.TaskPath;
import gr.cyberstream.workflow.engine.model.UserTaskDetails;
import gr.cyberstream.workflow.engine.model.UserTaskFormElement;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowDefinitionStatus;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.WfDocument;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfSettings;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.util.string.StringUtil;

/**
 * Implements all the business rules related to process definitions and process
 * instances
 * 
 * @author nlyk
 *
 */
@Service
public class ProcessService {

	private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

	@Autowired
	private Environment environment;

	@Autowired
	private Processes processRepository;

	@Autowired
	private CMISFolder cmisFolder;

	@Autowired
	private CMISDocument cmisDocument;

	@Autowired
	private RepositoryService activitiRepositorySrv;

	@Autowired
	private HistoryService activitiHistorySrv;

	@Autowired
	private FormService activitiFormSrv;

	@Autowired
	private TaskService activitiTaskSrv;

	@Autowired
	private RuntimeService activitiRuntimeSrv;

	@Autowired
	private RealmService realmService;

	@Autowired
	private MailService mailService;

	@Autowired
	private SettingsStatus settingsStatus;

	// user roles
	private static final String ROLE_ADMIN = "ROLE_Admin";
	private static final String ROLE_PROCESS_ADMIN = "ROLE_ProcessAdmin";
	private static final String ROLE_SUPERVISOR = "ROLE_Supervisor";

	/**
	 * Get a process by its id
	 * 
	 * @param definitionId
	 *            The process id
	 * 
	 * @return {@link WfProcess}
	 */
	public WfProcess getProcessById(int definitionId) {
		WorkflowDefinition workflow = processRepository.getById(definitionId);

		return new WfProcess(workflow);
	}

	/**
	 * Returns a list of all WfProcess depending on user.<br>
	 * If user has role admin all available processes will be returned,<br>
	 * else will be returned processes which user's group equals to process.
	 * 
	 * @return List of {@link WfProcess}
	 */
	public List<WfProcess> getAll() {

		List<WfProcess> returnList = new ArrayList<WfProcess>();
		List<WorkflowDefinition> workflows = processRepository.getAll();

		if (hasRole(ROLE_ADMIN)) {
			returnList = WfProcess.fromWorkflowDefinitions(workflows);

		} else {
			for (WorkflowDefinition workflowDefinition : workflows) {

				if (hasGroup(workflowDefinition.getOwner())) {
					WfProcess wfProcess = new WfProcess(workflowDefinition);
					returnList.add(wfProcess);
				}
			}
		}

		return returnList;
	}

	/**
	 * Returns a list of active WfProcess depending on user.<br>
	 * If user has role admin all active processes will be returned,<br>
	 * else will be returned active processes which user's group equals to
	 * process.
	 * 
	 * @return List of {@link WfProcess}
	 */
	public List<WfProcess> getActiveProcessDefinitions() {
		List<WfProcess> returnList = new ArrayList<WfProcess>();
		List<WorkflowDefinition> workflows = processRepository.getActiveProcessDefintions();

		if (hasRole(ROLE_ADMIN)) {
			returnList = WfProcess.fromWorkflowDefinitions(workflows);
		} else {
			for (WorkflowDefinition workflowDefinition : workflows) {
				if (hasGroup(workflowDefinition.getOwner())) {
					WfProcess wfProcess = new WfProcess(workflowDefinition);
					returnList.add(wfProcess);
				}
			}
		}
		return returnList;
	}

	/**
	 * Returns Tasks details by version id
	 * 
	 * @param versionId
	 *            Version id to get tasks details from.(Version corresponds to
	 *            definition's version id)
	 * 
	 * @return A list of {@link WfTaskDetails}
	 */
	public List<WfTaskDetails> getVersionTaskDetails(int versionId) {
		List<UserTaskDetails> taskDetails = processRepository.getVersionTaskDetails(versionId);

		return WfTaskDetails.fromUserTaskDetails(taskDetails);
	}

	/**
	 * Returns all running instances by process definition id
	 * 
	 * @param processDefinitionId
	 *            The definition's id to get running instances from
	 * 
	 * @return A list of {@link WfProcessInstance}
	 */
	public List<WfProcessInstance> getActiveProcessInstances(int processDefinitionId) {

		List<WorkflowInstance> workflowInstances = processRepository.getActiveProcessInstances(processDefinitionId);
		List<WfProcessInstance> returnList = new ArrayList<WfProcessInstance>();

		if (workflowInstances.size() > 0) {
			for (WorkflowInstance instance : workflowInstances) {
				returnList.add(new WfProcessInstance(instance));
			}
		}

		return returnList;
	}

	/**
	 * Deletes an instance from activiti and changes the status of the instance
	 * in our table to "DELETED"
	 * 
	 * @param instanceId
	 *            Instance's id to be canceled
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void cancelProcessInstance(String instanceId) throws InvalidRequestException {
		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(instanceId);

		} catch (NoResultException | EmptyResultDataAccessException e) {
			throw new InvalidRequestException("Process instance with id " + instanceId + " not found");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(instance.getDefinitionVersion().getWorkflowDefinition().getOwner())) {

			// delete it from activiti
			activitiRuntimeSrv.deleteProcessInstance(instanceId, null);

			// update instance's status to deleted
			instance.setStatus(WorkflowInstance.STATUS_DELETED);

			// save the instance after status changed to deleted
			processRepository.save(instance);

		} else
			throw new InvalidRequestException("You are not authorized to cancel that instance");
	}

	/**
	 * Delete an instance from activiti also removes it from our table too
	 * 
	 * @param instanceId
	 *            Instance's id to be deleted
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteInstance(String instanceId) throws InvalidRequestException {

		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(instanceId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("Process instance with id " + instanceId + " not found");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(instance.getDefinitionVersion().getWorkflowDefinition().getOwner())) {
			// delete it from activiti if instance is not marked as deleted
			// which means it is already deleted from activiti
			if (!instance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
				activitiRuntimeSrv.deleteProcessInstance(instanceId, null);

			// delete instance from WorkflowInstance table
			processRepository.cancelProcessInstance(instance);

		} else
			throw new InvalidRequestException("You are not authorized to delete that instance");
	}

	/**
	 * Suspends an instance
	 * 
	 * @param instanceId
	 *            Instance's id to be suspended
	 * 
	 * @return The updated {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance suspendProcessInstance(String instanceId) throws InvalidRequestException {

		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(instanceId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("Process instance with id " + instanceId + " not found");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(instance.getDefinitionVersion().getWorkflowDefinition().getOwner())) {

			try {
				activitiRuntimeSrv.suspendProcessInstanceById(instanceId);

			} catch (ActivitiException e) {
				throw new InvalidRequestException("Suspending the instance has failed. " + e.getMessage());
			}

			instance.setStatus(WorkflowInstance.STATUS_SUSPENDED);
			instance = processRepository.save(instance);

		} else
			throw new InvalidRequestException("You are not authorized to suspend instance");

		return new WfProcessInstance(instance);
	}

	/**
	 * Resume a suspended process instance
	 * 
	 * @param instanceId
	 *            Instance's id to be resumed
	 * 
	 * @return The updated {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance resumeProcessInstance(String instanceId) throws InvalidRequestException {
		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(instanceId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("Process instance with id " + instanceId + " not found");
		}

		if (instance.getStatus().equals(WorkflowInstance.STATUS_RUNNING))
			throw new InvalidRequestException("Instance is already running");

		if (hasRole(ROLE_ADMIN) || hasGroup(instance.getDefinitionVersion().getWorkflowDefinition().getOwner())) {

			try {
				activitiRuntimeSrv.activateProcessInstanceById(instanceId);

			} catch (ActivitiException e) {
				throw new InvalidRequestException("Resuming the instance has failed. " + e.getMessage());
			}

			instance.setStatus(WorkflowInstance.STATUS_RUNNING);
			instance = processRepository.save(instance);

			return new WfProcessInstance(instance);

		} else
			throw new InvalidRequestException("You are not authorized to resume the instance");
	}

	/**
	 * Returns all available definitions by owner
	 * 
	 * @param owners
	 *            List of owners
	 * 
	 * @return List of {@link WfProcess}
	 */
	public List<WfProcess> getDefinitionsByOwners(List<String> owners) {
		List<WorkflowDefinition> definitions = processRepository.getDefinitionsByOwners(owners);

		return WfProcess.fromWorkflowDefinitions(definitions);
	}

	/**
	 * UPdate a process definition from just its metadata. No BPMN definition is
	 * attached.
	 * 
	 * @param process
	 *            the metadata of the process
	 * 
	 * @return the saved process definition
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcess update(WfProcess process) throws InvalidRequestException {
		WorkflowDefinition definition;

		try {
			definition = processRepository.getById(process.getId());

		} catch (NoResultException | EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process found with the given id");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner())) {

			// 1. apply some rules
			if (StringUtil.isEmpty(definition.getName()))
				throw new InvalidRequestException("the name is required for the process definition");

			try {
				Long nameCount = processRepository.getCheckName(definition);

				if (nameCount > 0)
					process.setName(definition.getName() + " - "
							+ DateFormatUtils.format(Calendar.getInstance(), "d-M-yyyy HH.mm.ss"));

			} catch (EmptyResultDataAccessException e) {

			}

			// 3. Update Process Definition Folder
			cmisFolder.updateFolderName(definition.getFolderId(), definition.getName());

			definition.updateFrom(process);
			processRepository.save(definition);

		} else
			throw new InvalidRequestException("Seems you are not authorized to update the definition");

		return new WfProcess(definition);
	}

	/**
	 * Creates a new process definition based on an uploaded BPMN file. If the
	 * BPMN definition deploys successfully to Activiti repository service, a
	 * new ProcessDefinition object is created and saved in Process definitions
	 * repository.
	 * 
	 * @param inputStream
	 *            the input BPMN XML definition
	 * @param filename
	 * @return the newly created process definition
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcess createNewProcessDefinition(InputStream inputStream, String filename)
			throws InvalidRequestException {
		Deployment deployment;
		String defaultIcon = environment.getProperty("defaultIcon");
		String bpmn;

		try {
			bpmn = IOUtils.toString(inputStream, "UTF-8");

		} catch (IOException e) {
			logger.error("Unable to read BPMN Input Stream. " + e.getMessage());
			throw new InvalidRequestException("Unable to read BPMN Input Stream.");
		}

		// parse the id of the process from the bpmn file
		String processId = parseProcessId(bpmn);

		// check whether another process with the same process id in its bpmn
		// file exists
		if (processId == null) {
			logger.error("Process Definition Key is null");
			throw new InvalidRequestException("Process key is null.");

		} else if (definitionExistenceCheck(processId)) {
			logger.error("Process Definition Key " + processId + " already exists");
			throw new InvalidRequestException("Process with key " + processId + " already exists.");
		}

		// 1. Deploy the BPMN file to Activiti repository service
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		filename = StringUtil.isEmpty(filename) ? "noname.bpmn20.xml" : filename;

		try {
			deployment = activitiRepositorySrv.createDeployment().addString("input.bpmn20.xml", bpmn).name(filename)
					.deploy();
		} catch (XMLException | ActivitiIllegalArgumentException ex) {
			String message = "The BPMN input is not valid. Error string: " + ex.getMessage();
			logger.error(message);
			throw new InvalidRequestException(message);
		}

		// 2. Check deployment and get metadata from the deployed process
		// definition ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
		workflow.setAssignBySupervisor(false);
		workflow.setIcon(defaultIcon);

		String definitionName = processDef.getName();
		workflow.setName((definitionName == null) ? "you must name it" : definitionName);

		try {
			WorkflowDefinition sameNameWorkflowDefinition = null;

			try {
				sameNameWorkflowDefinition = processRepository.getByName(workflow.getName());
			} catch (NonUniqueResultException e) {
			}

			if (sameNameWorkflowDefinition != null) {

				workflow.setName(workflow.getName() + " - "
						+ DateFormatUtils.format(Calendar.getInstance(), "d-M-yyyy HH.mm.ss"));
			}

		} catch (EmptyResultDataAccessException e) {
		}

		DefinitionVersion definitionVersion = new DefinitionVersion();
		definitionVersion.setDeploymentId(deployment.getId());
		definitionVersion.setStatus(WorkflowDefinitionStatus.NEW.toString());
		definitionVersion.setVersion(processDef.getVersion());
		definitionVersion.setDeploymentdate(deployment.getDeploymentTime());
		definitionVersion.setProcessDefinitionId(processDef.getKey());

		workflow.addDefinitionVersion(definitionVersion);

		// 3. Create Process Definition Folder
		Folder folder = cmisFolder.createFolder(null, workflow.getName());
		workflow.setFolderId(folder.getId());

		// 4. save the new process definition
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		workflow = processRepository.save(workflow);

		// 5. Get task information from the bpmn model and create task details
		// entities
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		createTaskDetails(workflow);

		return new WfProcess(workflow);
	}

	/**
	 * 
	 * @param definitionId
	 * @param inputStream
	 * @param filename
	 * @return
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessVersion createNewProcessVersion(int definitionId, InputStream inputStream, String filename)
			throws InvalidRequestException {
		Deployment deployment;
		ProcessDefinition processDef;
		String bpmn;
		WorkflowDefinition workflowDefinition;

		// get the workflow definition
		try {
			workflowDefinition = processRepository.getById(definitionId);

		} catch (NoResultException | EmptyResultDataAccessException noRes) {
			throw new InvalidRequestException("No process definition found with that id");
		}

		// check user's roles/groups
		if (hasRole(ROLE_ADMIN) || hasGroup(workflowDefinition.getOwner())) {

			// parse the bpmn file
			try {
				bpmn = IOUtils.toString(inputStream, "UTF-8");

			} catch (IOException e) {
				logger.error("Unable to read BPMN Input Stream. " + e.getMessage());
				throw new InvalidRequestException("Unable to read BPMN Input Stream.");
			}

			// parse the id of the process from the bpmn file
			String processId = parseProcessId(bpmn);

			// verify that the latest version has a bpmn file with the same
			// process id
			if (!definitionVersionExistenceCheck(definitionId, processId)) {
				logger.error("Successive process versions should have the same key");
				throw new InvalidRequestException("Successive process versions should have the same key");
			}

			try {
				deployment = ActivitiHelper.createDeployment(activitiRepositorySrv, bpmn, filename);

			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}

			logger.debug("New BPMN deployment: " + deployment.getName());

			try {
				processDef = ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositorySrv,
						deployment.getId());

			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}

			DefinitionVersion definitionVersion = new DefinitionVersion();
			definitionVersion.setDeploymentId(deployment.getId());
			definitionVersion.setStatus(WorkflowDefinitionStatus.NEW.toString());
			definitionVersion.setVersion(processDef.getVersion());
			definitionVersion.setDeploymentdate(deployment.getDeploymentTime());
			definitionVersion.setProcessDefinitionId(ActivitiHelper
					.getProcessDefinitionByDeploymentId(activitiRepositorySrv, deployment.getId()).getId());

			workflowDefinition.addDefinitionVersion(definitionVersion);

			processRepository.save(workflowDefinition);
			createTaskDetails(workflowDefinition);

			return new WfProcessVersion(definitionVersion);

		} else
			throw new InvalidRequestException("You are not authorized to update that process definition");
	}

	/**
	 * Updates a process version
	 * 
	 * @param processId
	 *            The id of the process to be updated
	 * 
	 * @param wfProcessVersion
	 *            The process version entity
	 * 
	 * @return {@link WfProcessVersion} The updated entity
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessVersion updateVersion(int processId, WfProcessVersion wfProcessVersion)
			throws InvalidRequestException {
		DefinitionVersion definitionVersion;

		try {
			definitionVersion = processRepository.getVersionById(wfProcessVersion.getId());

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process version found with the given id");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(definitionVersion.getWorkflowDefinition().getOwner())) {
			definitionVersion.updateFrom(wfProcessVersion);
			processRepository.saveVersion(processId, definitionVersion);

			return new WfProcessVersion(definitionVersion);

		} else
			throw new InvalidRequestException("The definition you are trying to edit doesn't belong to your group");
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
	 * Generates an image of the bpmn model based on the definition and the
	 * given task to be highlighted
	 * 
	 * @param definitionId
	 *            The definition id
	 * 
	 * @param taskDefinition
	 *            The task's definition to be highlighted
	 * 
	 * @return
	 */
	public InputStreamResource getTaskProcessDiagram(int definitionId, String taskDefinition) {
		WorkflowDefinition definition = processRepository.getById(definitionId);

		ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
		BpmnModel bpmnModel = activitiRepositorySrv.getBpmnModel(definition.getKey());
		List<String> taskId = new ArrayList<>();

		if (bpmnModel.getLocationMap().size() == 0) {
			BpmnAutoLayout autoLayout = new BpmnAutoLayout(bpmnModel);
			autoLayout.execute();
		}

		// the task's definition id
		taskId.add(taskDefinition);
		InputStream resource = processDiagramGenerator.generateDiagram(bpmnModel, "jpeg", taskId);
		return new InputStreamResource(resource);
	}

	/**
	 * Creates an image based on the instance progress
	 * 
	 * @param instanceId
	 * @return
	 */
	public InputStreamResource getInstanceProgressDiagram(String instanceId) {
		WorkflowInstance instance = processRepository.getInstanceById(instanceId);
		WorkflowDefinition definition = instance.getDefinitionVersion().getWorkflowDefinition();

		ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();

		BpmnModel bpmnModel = activitiRepositorySrv.getBpmnModel(definition.getKey());

		if (bpmnModel.getLocationMap().size() == 0) {
			BpmnAutoLayout autoLayout = new BpmnAutoLayout(bpmnModel);
			autoLayout.execute();
		}

		InputStream resource = processDiagramGenerator.generateDiagram(bpmnModel, "jpeg",
				activitiRuntimeSrv.getActiveActivityIds(instanceId));

		return new InputStreamResource(resource);
	}

	/**
	 * Deletes all versions of the process. Throw exception if there are
	 * instances (active or old ones)
	 * 
	 * @param processId
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteProcessDefinition(int processId) throws InvalidRequestException {
		WorkflowDefinition definition;

		// get workflow definition
		try {
			definition = processRepository.getById(processId);

		} catch (NoResultException | EmptyResultDataAccessException noResult) {
			throw new InvalidRequestException("No process with that id found");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner())) {

			// check if any of the process deployments have instances
			boolean found = false;

			for (DefinitionVersion version : definition.getDefinitionVersions()) {
				if (activitiHistorySrv.createHistoricProcessInstanceQuery().notDeleted()
						.deploymentId(version.getDeploymentId()).count() > 0)
					found = true;
			}

			if (found)
				throw new InvalidRequestException("The process definition with id: " + processId
						+ " could not be deleted. There are associated entries");

			// delete all process definitions (all versions)
			String activeDeploymentId = definition.getActiveDeploymentId();

			boolean activeDeleted = false;

			// delete all version from definition
			for (DefinitionVersion version : definition.getDefinitionVersions()) {

				if (version.getDeploymentId().isEmpty())
					continue;

				// get definition's instance
				List<WorkflowInstance> instances = processRepository.getInstancesByDefinitionVersionId(version.getId());

				for (WorkflowInstance instance : instances) {

					// delete from our table
					processRepository.deleteProcessInstance(instance.getId());

					// delete from activiti
					activitiRuntimeSrv.deleteProcessInstance(instance.getId(), null);
				}

				activitiRepositorySrv.deleteDeployment(version.getDeploymentId());

				if (version.getDeploymentId().equals(activeDeploymentId))
					activeDeleted = true;
			}

			// delete active deployment if not already deleted
			if (!activeDeleted && activeDeploymentId != null && !activeDeploymentId.isEmpty())
				activitiRepositorySrv.deleteDeployment(activeDeploymentId);

			// delete workflow definition entry
			processRepository.delete(processId);

			// delete definition's cmis folder
			try {
				cmisFolder.deleteFolderById(definition.getFolderId());

			} catch (Exception e) {
				throw new InvalidRequestException("Error deleting definition's folder");
			}

		} else
			throw new InvalidRequestException("You are not authorized to delete the definition");
	}

	/**
	 * Deletes the specific version of the process definition. Fail if instances
	 * are found
	 * 
	 * @param processId
	 * @param deploymentId
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcess deleteProcessDefinitionVersion(int processId, String deploymentId) throws InvalidRequestException {

		// get workflow definition
		WorkflowDefinition workflowDefinition;

		try {
			workflowDefinition = processRepository.getById(processId);

		} catch (EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("No process found");
		}

		// check if the version id the last one
		if (workflowDefinition.getDefinitionVersions().size() < 2)
			throw new InvalidRequestException(
					"Trying to delete the last version. Delete the process definition instead.");

		// no need to check anything
		if (hasRole(ROLE_ADMIN) || hasGroup(workflowDefinition.getOwner())) {

			// check the existence of the deploymentId
			boolean found = false;
			boolean used = false;
			List<DefinitionVersion> versions = workflowDefinition.getDefinitionVersions();

			for (DefinitionVersion version : versions) {

				if (!version.getDeploymentId().equals(deploymentId)) {
					continue;
				}

				found = true;

				// check if the version is ever used
				if (activitiHistorySrv.createHistoricProcessInstanceQuery().notDeleted().deploymentId(deploymentId)
						.count() > 0) {
					used = true;
					break;
				}

				// remove version
				versions.remove(version);
				break;
			}
			// definition version not found
			if (!found)
				throw new InvalidRequestException("The process definition version with id: " + deploymentId
						+ " does not exist in process " + processId);

			// definition with the specific version is used
			if (used)
				throw new InvalidRequestException("The process definition version with id: " + deploymentId
						+ " could not be deleted. There are associated entries");

			// delete the deployment
			activitiRepositorySrv.deleteDeployment(deploymentId);

			// remove the version for the process definition
			workflowDefinition.setDefinitionVersions(versions);

			// update the process definition
			// if the deleted version was the active one, set the active
			// deployment to most recent one
			if (workflowDefinition.getActiveDeploymentId().equals(deploymentId))
				workflowDefinition
						.setActiveDeploymentId(workflowDefinition.getDefinitionVersions().get(0).getDeploymentId());

			return new WfProcess(processRepository.save(workflowDefinition));

		} else
			throw new InvalidRequestException("The definition you are trying to edit doesn't belong to your group");
	}

	/**
	 * Sets active version to a process definition
	 * 
	 * @param processId
	 *            The definition's to set active version
	 * 
	 * @param versionId
	 *            The version's id to set
	 * 
	 * @return {@link WfProcess} The updated definition
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcess setActiveVersion(int processId, int versionId) throws InvalidRequestException {

		WorkflowDefinition workflowDefinition;

		try {
			workflowDefinition = processRepository.getById(processId);

		} catch (EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("No process found");
		}

		// check user's roles/groups
		if (hasRole(ROLE_ADMIN) || hasGroup(workflowDefinition.getOwner())) {

			boolean found = false;

			for (DefinitionVersion version : workflowDefinition.getDefinitionVersions()) {
				if (version.getId() == versionId) {

					version.setStatus(WorkflowDefinitionStatus.ACTIVE.toString());

					workflowDefinition.setActiveDeploymentId(version.getDeploymentId());
					workflowDefinition.setKey(ActivitiHelper
							.getProcessDefinitionByDeploymentId(activitiRepositorySrv, version.getDeploymentId())
							.getId());

					found = true;
				} else {
					if (version.getStatus().equals(WorkflowDefinitionStatus.ACTIVE.toString()))
						version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());
				}
			}

			if (!found)
				throw new InvalidRequestException("The process definition version with id: " + versionId
						+ " does not exist in process " + workflowDefinition.getId());

			return new WfProcess(processRepository.save(workflowDefinition));

		} else
			throw new InvalidRequestException("The definition you are trying to edit doesn't belong to your group");
	}

	/**
	 * Deactivate the version of the workflow definition
	 * 
	 * @param processId
	 *            the process id
	 * 
	 * @param versionId
	 *            the of the id to be deactivated
	 * 
	 * @return the modified workflow definition
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessVersion deactivateVersion(int processId, int versionId) throws InvalidRequestException {

		WorkflowDefinition workflowDefinition;

		try {
			workflowDefinition = processRepository.getById(processId);

		} catch (EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("No process found");
		}

		DefinitionVersion definitionVersion = workflowDefinition.getVersion(versionId);

		if (hasRole(ROLE_ADMIN) || hasGroup(workflowDefinition.getOwner())) {
			definitionVersion.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());

			if (workflowDefinition.getActiveDeploymentId() != null
					&& workflowDefinition.getActiveDeploymentId().equals(definitionVersion.getDeploymentId())) {
				workflowDefinition.setActiveDeploymentId(null);
				processRepository.save(workflowDefinition);
			}

			return new WfProcessVersion(processRepository.saveVersion(processId, definitionVersion));

		} else
			throw new InvalidRequestException("The definition you are trying to edit doesn't belong to your group");
	}

	/**
	 * Return the full metadata set for the workflow definition
	 * 
	 * @param processId
	 *            the id of the workflow definition
	 * 
	 * @param device
	 *            The device which metadata will be retrieved
	 * 
	 * @return {@link WfProcess} with metadata
	 * @throws InvalidRequestException
	 */
	public WfProcess getProcessMetadata(int processId, String device) throws InvalidRequestException {
		WorkflowDefinition workflowDefinition;
		List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
		Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
		UserTaskDetails taskDetail = new UserTaskDetails();
		List<WfFormProperty> formProperties = new ArrayList<WfFormProperty>();

		try {
			workflowDefinition = processRepository.getById(processId);

		} catch (EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("No process found");
		}

		WfProcess process = new WfProcess(workflowDefinition);

		// get the start form for the definition
		StartFormData startForm = activitiFormSrv.getStartFormData(workflowDefinition.getKey());

		BpmnModel bpmnModel = activitiRepositorySrv.getBpmnModel(process.getProcessDefinitionId());
		List<org.activiti.bpmn.model.Process> processes = bpmnModel.getProcesses();

		for (org.activiti.bpmn.model.Process p : processes) {
			List<StartEvent> startEvents = p.findFlowElementsOfType(StartEvent.class);
			for (StartEvent startEvent : startEvents) {

				// task detail
				taskDetail = processRepository.getUserTaskDetailByDefinitionKey(startEvent.getId(),
						process.getProcessDefinitionId());

				// get the task form elements
				taskFormElements = processRepository.getUserTaskFromElements(process.getProcessDefinitionId(),
						taskDetail.getId());

				// fill the usertaskform element map using as key the element id
				// and as value the user taskform element
				for (UserTaskFormElement userTaskFormElement : taskFormElements) {
					mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
				}
			}
		}

		for (WfFormProperty formProperty : getWfFormProperties(startForm.getFormProperties())) {
			UserTaskFormElement userTaskFormElement = mappedUserTaskFormElements.get(formProperty.getId());

			if (userTaskFormElement.getDevice().equalsIgnoreCase(device)
					|| userTaskFormElement.getDevice().equalsIgnoreCase(UserTaskFormElement.ALL_DEVICES)) {
				formProperty.setDescription(userTaskFormElement.getDescription());
				formProperties.add(formProperty);
			}
		}

		process.setProcessForm(formProperties);

		return process;
	}

	/**
	 * Starts a new instance using its form data (no multipart files present)
	 * 
	 * @param processId
	 *            The process id of the instance to be started
	 * 
	 * @param instanceData
	 *            The instance's form data
	 * 
	 * @return The started {@link WfProcessInstance}
	 * 
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance startProcess(int processId, WfProcessInstance instanceData)
			throws InvalidRequestException, InternalException {

		AccessToken token = getAccessToken();
		WorkflowDefinition workflowDefinition;

		try {
			workflowDefinition = processRepository.getById(processId);

		} catch (NoResultException | EmptyResultDataAccessException noRes) {
			throw new InvalidRequestException("No process found to start that instance");
		}

		if (hasGroup(workflowDefinition.getOwner()) || hasRole(ROLE_ADMIN))
			return startProcess(workflowDefinition, instanceData, token.getEmail());
		else
			throw new InvalidRequestException("You are not authorized to start the instance");
	}

	/**
	 * Starts a new instance using its form data (multipart files are present)
	 * 
	 * @param processId
	 *            The process id of the instance to be started
	 * 
	 * @param instanceData
	 *            The instance's form data
	 * 
	 * @param files
	 *            The multipart files used to start the instance
	 * 
	 * @return The started {@link WfProcessInstance}
	 * 
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance startProcess(int processId, WfProcessInstance instanceData, MultipartFile[] files)
			throws InvalidRequestException, InternalException {

		AccessToken token = getAccessToken();
		WorkflowDefinition workflowDefinition;

		try {
			workflowDefinition = processRepository.getById(processId);

		} catch (EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("No process found to start the instance");
		}

		// check if title is unique
		if (processRepository.getCheckInstanceName(instanceData.getTitle()) > 0)
			throw new InvalidRequestException("instanceTitleUnique");

		// check user's roles/groups
		if (hasGroup(workflowDefinition.getOwner()) || hasRole(ROLE_ADMIN))
			return startProcess(workflowDefinition, instanceData, token.getEmail(), token.getName(), files);
		else
			throw new InvalidRequestException("You are not authorized to start the instance");
	}

	/**
	 * Start a new instance with no multipart files
	 * 
	 * @param definition
	 *            The workflow definition of the instance to be started
	 * 
	 * @param wfProcessInstance
	 *            The process instance entity
	 * 
	 * @param userId
	 * @return The started {@link WfProcessInstance}
	 * @throws InternalException
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public WfProcessInstance startProcess(WorkflowDefinition definition, WfProcessInstance wfProcessInstance,
			String userId) throws InternalException, InvalidRequestException {

		DefinitionVersion activeVersion = definition.getActiveVersion();

		// check if definition has an active version
		if (activeVersion == null)
			throw new InvalidRequestException("the process definition version is not active");

		// check if instance has title
		if (wfProcessInstance.getTitle() == null || wfProcessInstance.getTitle().length() == 0)
			throw new InvalidRequestException("the process title is not set");

		// check if title is unique
		if (processRepository.getCheckInstanceName(wfProcessInstance.getTitle()) > 0)
			throw new InvalidRequestException("instanceTitleUnique");

		try {
			if (wfProcessInstance.getProcessForm() != null) {
				for (WfFormProperty property : wfProcessInstance.getProcessForm()) {

					if (property.getType().equals("conversation"))
						property.setValue(fixConversationMessage(property.getValue(), userId));
				}
			}

			Map<String, String> variableValues = new HashMap<>();

			// get the start form properties if exists
			if (wfProcessInstance.getVariableValues() != null)
				variableValues.putAll(wfProcessInstance.getVariableValues());

			Folder processFolder = cmisFolder.getFolderById(definition.getFolderId());
			Folder folder = cmisFolder.createInstanceFolder(processFolder, wfProcessInstance.getTitle());

			/*
			 * since we put instance's properties such as title, reference, etc
			 * as variables values on start form, we should always start the
			 * instance as it had a start form in that way, when the start event
			 * listener will "hit", it will always have that variables since we
			 * used them to save/create the new instance
			 */

			// set instance's properties besides the start form properties
			variableValues.put("instanceTitle", wfProcessInstance.getTitle());
			variableValues.put("instanceSupervisor", wfProcessInstance.getSupervisor());
			variableValues.put("folderId", folder.getId());
			variableValues.put("definitionVersionId", "" + activeVersion.getId());

			activitiFormSrv.submitStartFormData(definition.getKey(), variableValues);

			/*
			 * if (variableValues != null) { variableValues.put("instanceTitle",
			 * instanceData.getTitle()); variableValues.put("instanceReference",
			 * instanceData.getReference());
			 * variableValues.put("instanceSupervisor",
			 * instanceData.getSupervisor()); variableValues.put("folderId",
			 * folder.getId()); variableValues.put("definitionVersionId", "" +
			 * activeVersion.getId());
			 * 
			 * activitiFormSrv.submitStartFormData(definition.getKey(),
			 * variableValues); } else
			 * activitiRuntimeSrv.startProcessInstanceById(definition.getKey());
			 */

			return wfProcessInstance;

		} catch (ActivitiException e) {
			logger.error("Failed to start instance " + e.getMessage() + " / " + e.getCause().getMessage());

			if (e.getCause() instanceof ServiceException) {
				ServiceException serviceException = (ServiceException) e.getCause();
				throw new InternalException(serviceException.getCode() + ". " + serviceException.getMessage());
			} else
				throw new InvalidRequestException(e.getMessage());

		} catch (CmisStorageException e) {
			logger.error(e.getMessage());
			throw new InternalException(e.getMessage());
		}
	}

	/**
	 * Starts an instance with files
	 * 
	 * @param definition
	 *            The process definition which the instance will be started
	 * 
	 * @param wfProcessInstance
	 *            Properties which user set before started the instance
	 * 
	 * @param userId
	 *            The email of the user who starts the instance (used as author
	 *            in documents's properties)
	 * 
	 * @param user
	 *            The name of the user who starts the instance (used as author
	 *            in documents's properties)
	 * 
	 * @param files
	 *            {@link MultipartFile} in order to start the instance
	 * 
	 * @return A started {@link WfProcessInstance}
	 * 
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public WfProcessInstance startProcess(WorkflowDefinition definition, WfProcessInstance wfProcessInstance,
			String userId, String user, MultipartFile[] files) throws InvalidRequestException, InternalException {

		// get definition version
		DefinitionVersion activeVersion = definition.getActiveVersion();

		// check if definition has version
		if (activeVersion == null)
			throw new InvalidRequestException("the process definition version is not active");

		// check if instance has title
		if (wfProcessInstance.getTitle() == null || wfProcessInstance.getTitle().length() == 0)
			throw new InvalidRequestException("the process title is not set");

		// check if title is unique
		if (processRepository.getCheckInstanceName(wfProcessInstance.getTitle()) > 0)
			throw new InvalidRequestException("instanceTitleUnique");

		// create a map used to hold the files
		Map<String, MultipartFile> filesMap = new HashMap<String, MultipartFile>();

		for (MultipartFile file : files) {
			filesMap.put(file.getOriginalFilename(), file);
		}

		// create instance's folder
		Folder processFolder = cmisFolder.getFolderById(definition.getFolderId());
		Folder folder = cmisFolder.createInstanceFolder(processFolder, wfProcessInstance.getTitle());

		try {
			if (wfProcessInstance.getProcessForm() != null) {
				for (WfFormProperty property : wfProcessInstance.getProcessForm()) {
					if (property.getType().equals("document")) {

						ObjectMapper mapper = new ObjectMapper();
						WfDocument wfDocument;

						try {
							wfDocument = mapper.readValue(property.getValue(), WfDocument.class);

							MultipartFile file = filesMap.get(property.getId());

							Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(),
									file.getContentType());
							document.refresh();
							wfDocument.setDocumentId(document.getId());
							wfDocument.setVersion(document.getVersionLabel());

							Calendar now = Calendar.getInstance();

							// DocumentType documentType = new
							// DocumentType(wfDocument.getTitle(),
							// wfDocument.getVersion(),
							// wfDocument.getDocumentId(), user, userId,
							// now.getTime(), wfDocument.getRefNo());

							DocumentType documentType = new DocumentType();
							documentType.setTitle(wfDocument.getTitle());
							documentType.setVersion(wfDocument.getVersion());
							documentType.setDocumentId(wfDocument.getDocumentId());
							documentType.setAuthor(user);
							documentType.setAuthorId(userId);
							documentType.setSubmittedDate(now.getTime());
							documentType.setRefNo(wfDocument.getRefNo());

							property.setValue(mapper.writeValueAsString(documentType));

						} catch (JsonParseException e) {
							e.printStackTrace();

						} catch (JsonMappingException e) {
							e.printStackTrace();

						} catch (IOException e) {
							e.printStackTrace();
						}

					} else if (property.getType().equals("conversation"))
						property.setValue(fixConversationMessage(property.getValue(), userId));
				}
			}

			Map<String, String> variableValues = new HashMap<>();

			// get the start form properties if exists
			if (wfProcessInstance.getVariableValues() != null)
				variableValues.putAll(wfProcessInstance.getVariableValues());

			/*
			 * since we put instance's properties such as title, reference, etc
			 * as variables values on start form, we should always start the
			 * instance as it had a start form in that way, when the start event
			 * listener will "hit", it will always have that variables since we
			 * used them to save/create the new instance
			 */

			// set instance's properties besides the start form properties
			variableValues.put("instanceTitle", wfProcessInstance.getTitle());
			variableValues.put("instanceSupervisor", wfProcessInstance.getSupervisor());
			variableValues.put("folderId", folder.getId());
			variableValues.put("definitionVersionId", "" + activeVersion.getId());

			activitiFormSrv.submitStartFormData(definition.getKey(), variableValues);

			return wfProcessInstance;

		} catch (ActivitiException e) {
			logger.error("Failed to start instance " + e.getMessage() + " / " + e.getCause().getMessage());

			if (e.getCause() instanceof ServiceException) {
				ServiceException serviceException = (ServiceException) e.getCause();
				throw new InternalException(serviceException.getCode() + ". " + serviceException.getMessage());
			} else
				throw new InvalidRequestException(e.getMessage());

		} catch (CmisStorageException e) {
			logger.error(e.getMessage());
			throw new InternalException(e.getMessage());
		}
	}

	private String fixConversationMessage(String conversationValue, String userId) {

		Calendar now = Calendar.getInstance();
		ObjectMapper mapper = new ObjectMapper();
		ConversationType conversation;

		try {
			conversation = mapper.readValue(conversationValue, ConversationType.class);

			if (conversation.getComment() != null && !conversation.getComment().isEmpty()) {

				MessageType message = new MessageType(conversation.getComment(), now.getTime(), userId);

				if (conversation.getMessages() == null)
					conversation.setMessages(new ArrayList<MessageType>());

				conversation.getMessages().add(message);
				conversation.setComment("");

				return mapper.writeValueAsString(conversation);
			}

		} catch (JsonParseException e) {
			e.printStackTrace();

		} catch (JsonMappingException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return conversationValue;
	}

	/**
	 * Return all process definitions for the processes supervised by the
	 * authenticated user
	 * 
	 * @return
	 */
	public List<WfProcessInstance> getSupervisedInstances() {

		// get intsances supervised by user
		return WfProcessInstance
				.fromWorkflowInstances(processRepository.getSupervisedProcesses(getAccessToken().getEmail()));
	}

	/**
	 * Save a document
	 * 
	 * @param instanceId
	 *            is the process instance id the document will be added to
	 * @param wfDocument
	 *            is the document metadata
	 * @param inputStream
	 *            is the document file InputStream
	 * @param contentType
	 *            is the document file content type
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfDocument saveDocument(String instanceId, String variableName, WfDocument wfDocument,
			InputStream inputStream, String contentType) throws InvalidRequestException {

		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(instanceId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("The process instance ID is not valid.");
		}

		Folder folder = cmisFolder.getFolderById(instance.getFolderId());

		Document document = saveOrUpdateDocument(folder, wfDocument, inputStream, contentType);
		document.refresh();

		wfDocument.setDocumentId(document.getId());
		wfDocument.setVersion(document.getVersionLabel());

		updateDocumentType(instanceId, variableName, wfDocument);

		return wfDocument;
	}

	/**
	 * Update a document
	 * 
	 * @param instanceId
	 *            is the process instance id the document will be added to
	 * @param wfDocument
	 *            is the document metadata
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfDocument updateDocument(String instanceId, String variableName, WfDocument wfDocument)
			throws InvalidRequestException {

		if (wfDocument.getDocumentId() == null)
			throw new InvalidRequestException("the document ID is null.");

		Document document = cmisDocument.updateDocumentById(wfDocument.getDocumentId(), wfDocument.getTitle());
		document.refresh();

		wfDocument.setDocumentId(document.getId());
		wfDocument.setVersion(document.getVersionLabel());

		updateDocumentType(instanceId, variableName, wfDocument);

		return wfDocument;
	}

	private void updateDocumentType(String instanceId, String variableName, WfDocument wfDocument) {

		Calendar now = Calendar.getInstance();

		AccessToken token = getAccessToken();

		String userId = token.getEmail();
		String user = token.getName();

		DocumentType documentType = (DocumentType) activitiRuntimeSrv.getVariable(instanceId, variableName);

		if (documentType == null) {

			documentType = new DocumentType(wfDocument.getTitle(), wfDocument.getVersion(), wfDocument.getDocumentId(),
					user, userId, now.getTime(), wfDocument.getRefNo());

		} else {

			documentType.setTitle(wfDocument.getTitle());
			documentType.setVersion(wfDocument.getVersion());
			documentType.setDocumentId(wfDocument.getDocumentId());
			documentType.setRefNo(wfDocument.getRefNo());
			documentType.setAuthor(user);
			documentType.setAuthorId(userId);
			documentType.setSubmittedDate(now.getTime());
		}

		activitiRuntimeSrv.setVariable(instanceId, variableName, documentType);
	}

	private Document saveOrUpdateDocument(Folder folder, WfDocument wfDocument, InputStream inputStream,
			String contentType) throws InvalidRequestException {

		logger.debug("Saving document " + wfDocument.getTitle() + " document.");

		Document document = null;

		if (wfDocument.getDocumentId() != null) {
			document = cmisDocument.updateDocumentById(wfDocument.getDocumentId(), wfDocument.getTitle(), contentType,
					inputStream);
			document.refresh();
		} else {
			try {
				document = cmisDocument.createDocument(folder, wfDocument.getTitle(), contentType, inputStream);

			} catch (CmisStorageException e) {
				throw new InvalidRequestException("Duplicate document title.");
			}
		}

		return document;
	}

	/**
	 * Returns instance's documents by a task id
	 * 
	 * @param taskId
	 *            Instance's task id
	 * 
	 * @return A list of {@link WfDocument}
	 * @throws InvalidRequestException
	 */
	public List<WfDocument> getProcessInstanceDocumentsByTask(int taskId) throws InvalidRequestException {

		Task task = activitiTaskSrv.createTaskQuery().taskId("" + taskId).singleResult();

		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(task.getProcessInstanceId());

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("The process instance ID is not valid.");
		}

		Folder folder = cmisFolder.getFolderById(instance.getFolderId());

		List<WfDocument> wfDocuments = new ArrayList<WfDocument>();
		List<Document> documents = cmisFolder.getFolderDocuments(folder);

		for (Document document : documents) {

			WfDocument wfDocument = new WfDocument();
			wfDocument.setTitle(document.getName());
			wfDocument.setDocumentId(document.getId());
			wfDocument.setVersion(document.getVersionLabel());
			wfDocument.setSubmittedDate(document.getLastModificationDate().getTime());

			List<WfDocument> wfDocumentVersions = new ArrayList<WfDocument>();
			List<Document> documentVersions = cmisDocument.getDocumentVersions(document);

			// First version is the current document
			documentVersions.remove(0);

			for (Document documentVersion : documentVersions) {

				WfDocument wfDocumentVersion = new WfDocument();
				wfDocumentVersion.setTitle(documentVersion.getName());
				wfDocumentVersion.setDocumentId(documentVersion.getId());
				wfDocumentVersion.setVersion(documentVersion.getVersionLabel());
				wfDocumentVersion.setSubmittedDate(documentVersion.getLastModificationDate().getTime());

				wfDocumentVersions.add(wfDocumentVersion);
			}

			wfDocument.setVersions(wfDocumentVersions);
			wfDocuments.add(wfDocument);
		}

		return wfDocuments;
	}

	/**
	 * Returns instance's documents
	 * 
	 * @param instanceId
	 *            Instance's id
	 * 
	 * @return A list of {@link WfDocument}
	 * @throws InvalidRequestException
	 */
	public List<WfDocument> getDocumentsByInstance(String instanceId) throws InvalidRequestException {

		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(instanceId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("The process instance ID is not valid.");
		}

		Folder folder = cmisFolder.getFolderById(instance.getFolderId());

		List<WfDocument> wfDocuments = new ArrayList<WfDocument>();
		List<Document> documents = cmisFolder.getFolderDocuments(folder);

		for (Document document : documents) {

			WfDocument wfDocument = new WfDocument();
			wfDocument.setTitle(document.getName());
			wfDocument.setDocumentId(document.getId());
			wfDocument.setVersion(document.getVersionLabel());
			wfDocument.setSubmittedDate(document.getLastModificationDate().getTime());

			List<WfDocument> wfDocumentVersions = new ArrayList<WfDocument>();
			List<Document> documentVersions = cmisDocument.getDocumentVersions(document);

			// First version is the current document
			documentVersions.remove(0);

			for (Document documentVersion : documentVersions) {

				WfDocument wfDocumentVersion = new WfDocument();
				wfDocumentVersion.setTitle(documentVersion.getName());
				wfDocumentVersion.setDocumentId(documentVersion.getId());
				wfDocumentVersion.setVersion(documentVersion.getVersionLabel());
				wfDocumentVersion.setSubmittedDate(documentVersion.getLastModificationDate().getTime());

				wfDocumentVersions.add(wfDocumentVersion);
			}

			wfDocument.setVersions(wfDocumentVersions);
			wfDocuments.add(wfDocument);
		}

		return wfDocuments;
	}

	/**
	 * Get supervised tasks if user has role admin then all tasks returned
	 * 
	 * @return List of {@link WfTask}
	 */
	public List<WfTask> getSupervisedTasks() throws InvalidRequestException {

		List<Task> tasks = new ArrayList<Task>();
		List<WfTask> returnList = new ArrayList<WfTask>();

		if (hasRole(ROLE_ADMIN)) {
			tasks = activitiTaskSrv.createTaskQuery().active().orderByDueDateNullsLast().asc().list();

			for (Task task : tasks) {
				TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(),
						task.getTaskDefinitionKey());
				WfTask wfTask = new WfTask(task);
				wfTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));
				wfTask.setStartForm(taskPath.getDefinition().hasStartForm());
				hydrateTask(wfTask);
				returnList.add(wfTask);
			}
		} else {
			tasks = activitiTaskSrv.createTaskQuery().active().orderByDueDateNullsLast().asc().list();
			for (Task task : tasks) {
				TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(),
						task.getTaskDefinitionKey());
				if (taskPath.getInstance().getSupervisor().equals(getAccessToken().getEmail())) {
					WfTask wfTask = new WfTask(task);
					wfTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));
					wfTask.setStartForm(taskPath.getDefinition().hasStartForm());
					hydrateTask(wfTask);

					returnList.add(wfTask);
				}

			}
		}
		return returnList;
	}

	/**
	 * Returns a list of tasks by instance id
	 * 
	 * @param instanceId
	 * @return
	 */
	public List<WfTask> getTasksByInstanceId(String instanceId) throws InvalidRequestException {
		List<WfTask> returnList = new ArrayList<WfTask>();
		List<HistoricTaskInstance> historicTasks = new ArrayList<HistoricTaskInstance>();

		historicTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().processInstanceId(instanceId).list();
		WorkflowInstance instance = processRepository.getInstanceById(instanceId);

		// loop through completed tasks
		for (HistoricTaskInstance task : historicTasks) {

			WfTask wfTask = new WfTask(task);
			wfTask.setProcessInstance(new WfProcessInstance(instance));
			hydrateTask(wfTask);

			returnList.add(wfTask);
		}
		return returnList;
	}

	/**
	 * Get the user's completed instances
	 * 
	 * @return
	 */
	public List<WfProcessInstance> getUserCompletedInstances() {
		List<WfProcessInstance> returnList = new ArrayList<WfProcessInstance>();

		AccessToken token = this.getAccessToken();

		String assignee = (String) token.getEmail();

		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();

		completedUserTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().taskAssignee(assignee)
				.orderByHistoricTaskInstanceEndTime().desc().list();

		for (HistoricTaskInstance completedUserTask : completedUserTasks) {
			WorkflowInstance instance = processRepository.getInstanceById(completedUserTask.getProcessInstanceId());

			returnList.add(new WfProcessInstance(instance));
		}
		return returnList;
	}

	/**
	 * Get the user's completed tasks
	 * 
	 * @return
	 */

	public List<WfTask> getUserCompletedTasks() {
		List<WfTask> returnList = new ArrayList<WfTask>();

		AccessToken token = this.getAccessToken();

		String assignee = (String) token.getEmail();

		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();

		completedUserTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().taskAssignee(assignee)
				.orderByHistoricTaskInstanceEndTime().desc().list();

		for (HistoricTaskInstance completedUserTask : completedUserTasks) {
			WorkflowInstance instance = processRepository.getInstanceById(completedUserTask.getProcessInstanceId());
			DefinitionVersion definitionVersion = processRepository
					.getVersionByProcessDefinitionId(completedUserTask.getProcessDefinitionId());

			WfTask wfTask = new WfTask(completedUserTask);
			wfTask.setIcon(definitionVersion.getWorkflowDefinition().getIcon());
			wfTask.setProcessInstance(new WfProcessInstance(instance));

			returnList.add(wfTask);
		}

		return returnList;
	}

	/**
	 * Returns completed tasks for the user in context (Logged in user)
	 * 
	 * @return
	 */
	public List<WfTask> getCompletedTasksForUser() {

		List<WfTask> returnList = new ArrayList<WfTask>();
		List<HistoricTaskInstance> taskList = new ArrayList<HistoricTaskInstance>();

		AccessToken token = this.getAccessToken();
		String assignee = (String) token.getEmail();

		taskList = activitiHistorySrv.createHistoricTaskInstanceQuery().taskAssignee(assignee).orderByTaskCreateTime()
				.asc().list();

		for (HistoricTaskInstance task : taskList) {
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());

			WfTask wfTask = new WfTask(task);
			wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
			wfTask.setDefinitionName(instance.getDefinitionVersion().getWorkflowDefinition().getName());
			wfTask.setProcessInstance(new WfProcessInstance(instance));

			returnList.add(wfTask);
		}

		return returnList;
	}

	/**
	 * Searches for completed tasks based on given criteria
	 * 
	 * @param definitionKey
	 *            The process definition key(not the id)
	 * 
	 * @param instanceTitle
	 *            The instance's title
	 * 
	 * @param after
	 *            Task's completed date after
	 * 
	 * @param before
	 *            Task's completed date after
	 * 
	 * @param isSupervisor
	 *            If the user is supervisor
	 * 
	 * @return A list of found {@link WfTask}
	 * 
	 * @throws InvalidRequestException
	 */
	public List<WfTask> getSearchedCompletedTasks(String definitionKey, String instanceTitle, long after, long before,
			String isSupervisor) throws InvalidRequestException {

		List<WfTask> returnList = new ArrayList<WfTask>();
		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);
		List<HistoricTaskInstance> taskList = new ArrayList<HistoricTaskInstance>();
		WorkflowInstance instance = new WorkflowInstance();

		// show tasks for user
		if (isSupervisor.equals("false")) {
			// Process defintion id == process definition key

			HistoricTaskInstanceQuery query = activitiHistorySrv.createHistoricTaskInstanceQuery();

			// Process defintion id == process definition key
			if (!definitionKey.equals("all") && !definitionKey.isEmpty()) {
				WorkflowDefinition definition = processRepository.getDefinitionByKey(definitionKey);
				List<String> processInstanceIds = new ArrayList<>();

				if (definition.getDefinitionVersions().size() > 0) {
					for (DefinitionVersion version : definition.getDefinitionVersions()) {

						try {
							for (WorkflowInstance processInstance : processRepository
									.getInstancesByDefinitionVersionId(version.getId())) {
								processInstanceIds.add(processInstance.getId());
							}
						} catch (EmptyResultDataAccessException e) {
							throw new InvalidRequestException("No instance found for the selected process");
						}

					}
					if (processInstanceIds.size() > 0)
						query.processInstanceIdIn(processInstanceIds);
					else
						query.processInstanceId(" ");

				} else
					query.processDefinitionId(definitionKey);
			}

			if (dateAfter != null) {
				query = query.taskCompletedAfter(dateAfter);
			}

			if (dateBefore.getTime() == 0) {
				dateBefore = new Date();
				query = query.taskCompletedBefore(dateBefore);
			}

			if (dateBefore != null) {
				query = query.taskCompletedBefore(dateBefore);
			}

			taskList = query.taskAssignee(getAccessToken().getEmail()).list();

			for (HistoricTaskInstance task : taskList) {
				try {
					instance = processRepository.getInstanceById(task.getProcessInstanceId());
				} catch (EmptyResultDataAccessException e) {
					instance = null;
				}
				if (instance != null) {
					if (StringUtils.containsIgnoreCase(instance.getTitle(), instanceTitle)) {

						WfTask wfTask = new WfTask(task);
						wfTask.setProcessInstance(new WfProcessInstance(instance));
						wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
						returnList.add(wfTask);
					}
				}
			}

			// show supervised tasks
		} else if (isSupervisor.equals("true")) {

			HistoricTaskInstanceQuery query = activitiHistorySrv.createHistoricTaskInstanceQuery();

			// Process defintion id == process definition key
			if (!definitionKey.equals("all") && !definitionKey.isEmpty()) {
				WorkflowDefinition definition = processRepository.getDefinitionByKey(definitionKey);
				List<String> processInstanceIds = new ArrayList<>();

				if (definition.getDefinitionVersions().size() > 0) {
					for (DefinitionVersion version : definition.getDefinitionVersions()) {

						try {
							for (WorkflowInstance processInstance : processRepository
									.getInstancesByDefinitionVersionId(version.getId())) {
								processInstanceIds.add(processInstance.getId());
							}
						} catch (EmptyResultDataAccessException e) {
							throw new InvalidRequestException("No instance found for the selected process");
						}
					}

					if (processInstanceIds.size() > 0)
						query.processInstanceIdIn(processInstanceIds);
					else
						query.processInstanceId(" ");

				} else
					query.processDefinitionId(definitionKey);

			}

			if (dateAfter != null) {
				query = query.taskCompletedAfter(dateAfter);
			}

			if (dateBefore.getTime() == 0) {
				dateBefore = new Date();
				query = query.taskCompletedBefore(dateBefore);
			}

			if (dateBefore != null) {
				query = query.taskCompletedBefore(dateBefore);
			}

			taskList = query.list();

			for (HistoricTaskInstance task : taskList) {
				try {
					instance = processRepository.getInstanceById(task.getProcessInstanceId());
				} catch (EmptyResultDataAccessException e) {
					instance = null;
				}
				// check if admin or process admin in order to display all
				// instances
				if (hasRole(ROLE_ADMIN) || hasRole(ROLE_PROCESS_ADMIN)) {

					if (instance != null) {
						if (StringUtils.containsIgnoreCase(instance.getTitle(), instanceTitle)) {

							WfTask wfTask = new WfTask(task);
							wfTask.setProcessInstance(new WfProcessInstance(instance));
							wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
							returnList.add(wfTask);
						}
					}
					// user is not admin or process admin
				} else {
					if (instance != null) {
						if (StringUtils.containsIgnoreCase(instance.getTitle(), instanceTitle)
								&& instance.getSupervisor().equals(getAccessToken().getEmail())) {

							WfTask wfTask = new WfTask(task);
							wfTask.setProcessInstance(new WfProcessInstance(instance));
							wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
							returnList.add(wfTask);
						}
					}

				} // end of check roles
			}
		}

		return returnList;
	}

	/**
	 * Get user's completed tasks by selected ids
	 * 
	 * @return
	 */

	public List<WfTask> getUserCompledTasksByInstanceIds(List<String> instanceIds) {
		List<WfTask> returnList = new ArrayList<WfTask>();
		String assignee = getAccessToken().getEmail();

		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();

		completedUserTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().processInstanceIdIn(instanceIds)
				.taskAssignee(assignee).orderByHistoricTaskInstanceEndTime().desc().list();

		for (HistoricTaskInstance completedUserTask : completedUserTasks) {
			DefinitionVersion definitionVersion = processRepository
					.getVersionByProcessDefinitionId(completedUserTask.getProcessDefinitionId());
			WorkflowInstance instance = processRepository.getInstanceById(completedUserTask.getProcessInstanceId());

			WfTask wfTask = new WfTask(completedUserTask);
			wfTask.setIcon(definitionVersion.getWorkflowDefinition().getIcon());
			wfTask.setProcessInstance(new WfProcessInstance(instance));

			returnList.add(wfTask);
		}
		return returnList;
	}

	/**
	 * Get completed tasks by selected ids
	 * 
	 * @return
	 */

	public List<WfTask> getCompletedTasksByInstances(List<String> instanceIds) {
		List<WfTask> returnList = new ArrayList<WfTask>();
		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();

		completedUserTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().processInstanceIdIn(instanceIds)
				.orderByHistoricTaskInstanceEndTime().desc().list();

		for (HistoricTaskInstance completedUserTask : completedUserTasks) {
			DefinitionVersion definitionVersion = processRepository
					.getVersionByProcessDefinitionId(completedUserTask.getProcessDefinitionId());
			WorkflowInstance instance = processRepository.getInstanceById(completedUserTask.getProcessInstanceId());

			WfTask wfTask = new WfTask(completedUserTask);
			wfTask.setIcon(definitionVersion.getWorkflowDefinition().getIcon());
			wfTask.setProcessInstance(new WfProcessInstance(instance));

			returnList.add(wfTask);
		}
		return returnList;
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
	 * @throws InvalidRequestException
	 */
	public WfTask getTask(String taskId) throws InvalidRequestException {
		HistoricTaskInstance task;

		try {
			task = activitiHistorySrv.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();

		} catch (ActivitiException noRes) {
			throw new InvalidRequestException("There is no task with the given id");
		}

		WfTask wfTask = new WfTask(task);

		WorkflowInstance taskInstance = processRepository.getInstanceById(task.getProcessInstanceId());

		wfTask.setStartForm(taskInstance.getDefinitionVersion().getWorkflowDefinition().hasStartForm());
		wfTask.setProcessInstance(new WfProcessInstance(taskInstance));

		List<UserTaskDetails> taskDetails = processRepository.getUserTaskDetailsByDefinitionKey(
				task.getTaskDefinitionKey(), taskInstance.getDefinitionVersion().getId());

		for (UserTaskDetails details : taskDetails) {
			wfTask.setTaskDetails(new WfTaskDetails(details));
		}

		hydrateTask(wfTask);

		return wfTask;
	}

	/**
	 * Returns a task by task definition key
	 * 
	 * @param taskDefinitionKey
	 * @return
	 * @throws InvalidRequestException
	 */
	public List<WfFormProperty> getTaskFormPropertiesByTaskDefintionKey(String taskDefinitionKey,
			String processDefinitionId) throws InvalidRequestException {

		List<WfFormProperty> returnList = new ArrayList<WfFormProperty>();
		List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
		Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
		UserTaskDetails taskDetails = new UserTaskDetails();
		List<org.activiti.bpmn.model.FormProperty> formProperties = new ArrayList<org.activiti.bpmn.model.FormProperty>();

		//
		try {
			formProperties = ActivitiHelper.getTaskFormDefinition(activitiRepositorySrv, processDefinitionId,
					taskDefinitionKey);
			taskDetails = processRepository.getUserTaskDetailByDefinitionKey(taskDefinitionKey, processDefinitionId);
			taskFormElements = processRepository.getUserTaskFromElements(processDefinitionId, taskDetails.getId());

			// create the map
			for (UserTaskFormElement userTaskFormElement : taskFormElements) {
				mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
			}

			for (org.activiti.bpmn.model.FormProperty formPropery : formProperties) {

				// prepare formValues
				Map<String, String> values = new HashMap<String, String>();

				// date pattern
				String dateFormat = "";

				UserTaskFormElement userTaskFormElement = null;

				// get the user task form element from map
				if (!mappedUserTaskFormElements.isEmpty()) {
					userTaskFormElement = mappedUserTaskFormElements.get(formPropery.getId());
				}

				if (formPropery.getType().equals("enum")) {

					List<FormValue> formValues = formPropery.getFormValues();

					for (int i = 0; formValues != null && i < formValues.size(); i++) {
						values.put(formValues.get(i).getId(), formValues.get(i).getName());
					}

				} else if (formPropery.getType().equals("date")) {

					dateFormat = formPropery.getDatePattern();
				}

				WfFormProperty wfProperty = null;

				wfProperty = new WfFormProperty(formPropery.getId(), formPropery.getName(), formPropery.getType(), "",
						formPropery.isReadable(), formPropery.isWriteable(), formPropery.isRequired(), values,
						dateFormat, userTaskFormElement.getDescription(), userTaskFormElement.getDevice());

				returnList.add(wfProperty);
			}

		} catch (Exception e) {

			formProperties = ActivitiHelper.getTaskFormDefinition(activitiRepositorySrv, processDefinitionId);
			taskDetails = processRepository.getUserTaskDetailByDefinitionKey(taskDefinitionKey, processDefinitionId);
			taskFormElements = processRepository.getUserTaskFromElements(processDefinitionId, taskDetails.getId());

			// create the map
			for (UserTaskFormElement userTaskFormElement : taskFormElements) {
				mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
			}

			for (org.activiti.bpmn.model.FormProperty formPropery : formProperties) {

				// prepare formValues
				Map<String, String> values = new HashMap<String, String>();

				// date pattern
				String dateFormat = "";

				UserTaskFormElement userTaskFormElement = null;

				// get the user task form element from map
				if (!mappedUserTaskFormElements.isEmpty()) {
					userTaskFormElement = mappedUserTaskFormElements.get(formPropery.getId());
				}

				if (formPropery.getType().equals("enum")) {

					List<FormValue> formValues = formPropery.getFormValues();

					for (int i = 0; formValues != null && i < formValues.size(); i++) {
						values.put(formValues.get(i).getId(), formValues.get(i).getName());
					}

				} else if (formPropery.getType().equals("date")) {

					dateFormat = formPropery.getDatePattern();
				}

				WfFormProperty wfProperty = null;

				wfProperty = new WfFormProperty(formPropery.getId(), formPropery.getName(), formPropery.getType(), "",
						formPropery.isReadable(), formPropery.isWriteable(), formPropery.isRequired(), values,
						dateFormat, userTaskFormElement.getDescription(), userTaskFormElement.getDevice());

				returnList.add(wfProperty);
			}
		}
		return returnList;
	}

	/**
	 * Returns a completed task
	 * 
	 * @param taskId
	 * @return
	 */
	public WfTask getCompletedTask(String taskId) {
		HistoricTaskInstance task = activitiHistorySrv.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		WorkflowInstance taskInstance = processRepository.getInstanceById(task.getProcessInstanceId());

		WfTask wfTask = new WfTask(task);
		wfTask.setProcessInstance(new WfProcessInstance(taskInstance));
		wfTask.setIcon(taskInstance.getDefinitionVersion().getWorkflowDefinition().getIcon());
		return wfTask;
	}

	/**
	 * Completes a task
	 * 
	 * @param wfTask
	 *            The task to be completed
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void completeTask(WfTask wfTask) throws InvalidRequestException {
		String assignee = getAccessToken().getEmail();

		// check if task has the same assignee as the person requests to
		// complete it or if that person has role admin
		if (wfTask.getAssignee().equals(assignee) || hasRole(ROLE_ADMIN)) {
			try {
				// check if task's instance exists before doing anything else
				WorkflowInstance tasksInstance = processRepository.getInstanceById(wfTask.getProcessInstance().getId());

				if (tasksInstance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
					throw new InvalidRequestException("instanceSuspended");

				if (tasksInstance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
					throw new InvalidRequestException("instanceDeleted");

				if (wfTask.getTaskForm() != null) {
					for (WfFormProperty property : wfTask.getTaskForm()) {

						if (property.getType().equals("conversation"))
							property.setValue(fixConversationMessage(property.getValue(), assignee));
					}

					Map<String, String> variableValues = wfTask.getVariableValues();

					if (variableValues != null && !variableValues.isEmpty())
						activitiFormSrv.saveFormData(wfTask.getId(), variableValues);

					activitiTaskSrv.complete(wfTask.getId());
				}

			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());

			} catch (NoResultException | EmptyResultDataAccessException noResult) {
				throw new InvalidRequestException(
						"The task's instance does not exists. Please contact system administrator");
			}
			// task's assignee not matched with the person who requests to
			// complete it or not admin
		} else
			throw new InvalidRequestException("Seems you are not the authorized to complete the task");
	}

	/**
	 * Completes a task using Multipart files
	 * 
	 * @param wfTask
	 *            The task to be completed
	 * 
	 * @param files
	 *            {@link MultipartFile} The files required to complete the task
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void completeTask(WfTask wfTask, MultipartFile[] files) throws InvalidRequestException {
		String userId = getAccessToken().getEmail();
		String user = getAccessToken().getName();

		// check if task's instance exists before doing anything else
		WorkflowInstance tasksInstance = processRepository.getInstanceById(wfTask.getProcessInstance().getId());

		if (tasksInstance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
			throw new InvalidRequestException("instanceSuspended");

		if (tasksInstance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
			throw new InvalidRequestException("instanceDeleted");

		Map<String, MultipartFile> filesMap = new HashMap<String, MultipartFile>();

		for (MultipartFile file : files) {
			filesMap.put(file.getOriginalFilename(), file);
		}

		Folder folder = cmisFolder.getFolderById(wfTask.getProcessInstance().getFolderId());

		try {

			if (wfTask.getTaskForm() != null) {

				for (WfFormProperty property : wfTask.getTaskForm()) {

					if (property.getType().equals("document")) {

						ObjectMapper mapper = new ObjectMapper();
						WfDocument wfDocument;

						try {
							wfDocument = mapper.readValue(property.getValue(), WfDocument.class);

							MultipartFile file = filesMap.get(property.getId());

							Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(),
									file.getContentType());
							document.refresh();

							wfDocument.setDocumentId(document.getId());
							wfDocument.setVersion(document.getVersionLabel());

							Calendar now = Calendar.getInstance();

							DocumentType documentType = new DocumentType(wfDocument.getTitle(), wfDocument.getVersion(),
									wfDocument.getDocumentId(), user, userId, now.getTime(), wfDocument.getRefNo());

							property.setValue(mapper.writeValueAsString(documentType));

						} catch (JsonParseException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

					} else if (property.getType().equals("conversation")) {
						property.setValue(fixConversationMessage(property.getValue(), userId));
					}
				}
			}

			Map<String, String> variableValues = wfTask.getVariableValues();

			if (variableValues != null && !variableValues.isEmpty())
				activitiFormSrv.saveFormData(wfTask.getId(), variableValues);

			activitiTaskSrv.complete(wfTask.getId());

		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());

		} catch (NoResultException | EmptyResultDataAccessException noResult) {
			throw new InvalidRequestException(
					"The task's instance does not exists. Please contact system administrator");
		}
	}

	/**
	 * Temporary save the task's data
	 * 
	 * @param task
	 * @param files
	 * @throws InvalidRequestException
	 */
	public void tempTaskSave(WfTask task, MultipartFile[] files) throws InvalidRequestException {
		String userId = getAccessToken().getEmail();
		String user = getAccessToken().getName();

		// check if task's instance exists before doing anything else
		WorkflowInstance tasksInstance = processRepository.getInstanceById(task.getProcessInstance().getId());

		if (tasksInstance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
			throw new InvalidRequestException("instanceSuspended");

		if (tasksInstance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
			throw new InvalidRequestException("instanceDeleted");

		Map<String, MultipartFile> filesMap = new HashMap<String, MultipartFile>();

		for (MultipartFile file : files) {
			filesMap.put(file.getOriginalFilename(), file);
		}

		Folder folder = cmisFolder.getFolderById(task.getProcessInstance().getFolderId());

		try {

			if (task.getTaskForm() != null) {

				for (WfFormProperty property : task.getTaskForm()) {

					if (property.getType().equals("document")) {

						ObjectMapper mapper = new ObjectMapper();
						WfDocument wfDocument;

						try {
							wfDocument = mapper.readValue(property.getValue(), WfDocument.class);

							MultipartFile file = filesMap.get(property.getId());

							Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(),
									file.getContentType());
							document.refresh();

							wfDocument.setDocumentId(document.getId());
							wfDocument.setVersion(document.getVersionLabel());

							Calendar now = Calendar.getInstance();

							DocumentType documentType = new DocumentType(wfDocument.getTitle(), wfDocument.getVersion(),
									wfDocument.getDocumentId(), user, userId, now.getTime(), wfDocument.getRefNo());

							property.setValue(mapper.writeValueAsString(documentType));

						} catch (JsonParseException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

					} else if (property.getType().equals("conversation")) {

						property.setValue(fixConversationMessage(property.getValue(), userId));
					}
				}
			}

			Map<String, String> variableValues = task.getVariableValues();

			if (variableValues != null && !variableValues.isEmpty())
				activitiFormSrv.saveFormData(task.getId(), variableValues);

		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());

		} catch (NoResultException | EmptyResultDataAccessException noResult) {
			throw new InvalidRequestException(
					"The task's instance does not exists. Please contact system administrator");
		}
	}

	/**
	 * Temporary save task without document
	 * 
	 * @param task
	 * @throws InvalidRequestException
	 */

	public void tempTaskSave(WfTask task) throws InvalidRequestException {

		try {
			// check if task's instance exists before doing anything else
			WorkflowInstance tasksInstance = processRepository.getInstanceById(task.getProcessInstance().getId());

			if (tasksInstance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
				throw new InvalidRequestException("instanceSuspended");

			if (tasksInstance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
				throw new InvalidRequestException("instanceDeleted");

			activitiFormSrv.saveFormData(task.getId(), task.getVariableValues());

		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());

		} catch (NoResultException | EmptyResultDataAccessException noResult) {
			throw new InvalidRequestException(
					"The task's instance does not exists. Please contact system administrator");
		}
	}

	/**
	 * Retuns a list of user ids based on workflow definition of the task
	 * 
	 * @param taskId
	 * @return
	 */
	public List<WfUser> getCandidatesByTaskId(String taskId) {
		List<WfUser> candidates = new ArrayList<WfUser>();
		List<WfUser> tempList = new ArrayList<WfUser>();

		List<IdentityLink> links = activitiTaskSrv.getIdentityLinksForTask(taskId);

		if (links.size() == 0 || links == null) {
			for (WfUser user : realmService.getAllUsers()) {

				if (user.getEmail() != null)
					user.setPendingTasks(
							activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());

				if (!candidates.contains(user))
					candidates.add(user);
			}
		}

		for (IdentityLink link : links) {
			if (IdentityLinkType.CANDIDATE.equals(link.getType())) {

				String candidateExpr = " " + link.getGroupId() + " : ";
				String[] splittedVals = candidateExpr.split(":");

				String term1 = splittedVals[0].trim();
				String term2 = splittedVals[1].trim();

				if (!term1.isEmpty() && term2.isEmpty()) {
					if (splittedVals.length == 2) {
						// role only (term1)
						try {
							tempList = realmService.getUsersByRole(term1);

						} catch (Exception e) {
							logger.error("Error getting groups " + term1 + " " + e.getMessage());
							continue;
						}
						for (WfUser user : tempList) {
							user.setPendingTasks(
									activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());

							if (!candidates.contains(user))
								candidates.add(user);
						}
						logger.debug("Getting candidates for Role: other " + term1);

					} else {
						// group only (term1)
						for (WfUser user : realmService.getUsersByGroup(term1)) {
							user.setPendingTasks(
									activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());

							if (!candidates.contains(user))
								candidates.add(user);
						}
						logger.debug("Getting candidates for Group: " + term1);
					}
				} else if (term1.isEmpty() && !term2.isEmpty()) {
					// role only (term2)
					for (WfUser user : realmService.getUsersByRole(term2)) {
						user.setPendingTasks(
								activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());

						if (!candidates.contains(user))
							candidates.add(user);
					}
					logger.debug("Getting candidates for Role: test " + term2);

				} else {
					// term1 = group, term2: role
					for (WfUser user : realmService.getUsersByGroupAndRole(term1, term2)) {
						user.setPendingTasks(
								activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());

						if (!candidates.contains(user))
							candidates.add(user);
					}
					logger.debug("Getting candidates for User Group : " + term1 + " and Role: " + term2);
				}
			}
		}

		return candidates;
	}

	/**
	 * 
	 * @return
	 */
	public List<WfUser> getAllCandidates() {
		List<WfUser> candidates = new ArrayList<WfUser>();

		for (WfUser user : realmService.getAllUsers()) {

			user.setPendingTasks(activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());
			candidates.add(user);
		}

		return candidates;
	}

	/**
	 * <p>
	 * This function is called when there is no candidate for a specific task.
	 * </p>
	 * <p>
	 * Sends an e-mail to the administrator to notify him for the absence of
	 * candidates for assigning the task to.
	 * </p>
	 *
	 * @param taskId
	 *            The ID of the task that has no candidates
	 */
	public void notifyAdminForTask(String taskId) throws InvalidRequestException {
        String adminEmail = environment.getProperty("mail.admin");
        Task workflowTask = activitiTaskSrv.createTaskQuery().taskId(taskId).singleResult();

        try {
            mailService.sendNoCandidatesEmail(adminEmail, workflowTask.getName(), workflowTask.getId());
        } catch (MessagingException e) {
            throw new InvalidRequestException("The e-mail could not be sent to the administrator. Please contact " +
                    "administrator directly.");
        }
    }

	/**
	 * Returns from in progress instances assigned tasks to logged in user
	 * 
	 * @return A list of {@link WfTask}
	 */
	public List<WfTask> getTasksForUser() throws InvalidRequestException {
		List<WfTask> returnList = new ArrayList<WfTask>();
		List<Task> taskList = new ArrayList<Task>();
		String userId = getAccessToken().getEmail();

		// Getting tasks for user
		taskList = activitiTaskSrv.createTaskQuery().orderByTaskCreateTime().taskAssignee(userId).asc().list();

		for (Task task : taskList) {
			TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());

			// check if tasks's instance is running
			if (taskPath.getInstance().getStatus().equals(WorkflowInstance.STATUS_RUNNING)) {
				WfTask wfTask = new WfTask(task);
				wfTask.setStartForm(taskPath.getDefinition().hasStartForm());
				wfTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));

				returnList.add(hydrateTask(wfTask));
			}
		}
		return returnList;
	}

	/**
	 * Assigns a task to given assignee
	 * 
	 * @param wfTask
	 *            The task to be assigned to
	 * 
	 * @param assigneeId
	 *            The assignee's email
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void assignTask(WfTask wfTask, String assigneeId) throws InvalidRequestException {

		String loggedInUser = getAccessToken().getEmail();

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
			throw new InvalidRequestException("claimTaskInstanceSuspended");

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_DELETED))
			throw new InvalidRequestException("claimTaskInstanceDeleted");

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_ENDED))
			throw new InvalidRequestException("claimTaskInstanceEnded");

		// check if task is supervised by the person who request to assign the
		// task or if is admin
		if (wfTask.getProcessInstance().getSupervisor().equals(loggedInUser) || hasRole(ROLE_ADMIN)) {
			try {

				if (wfTask.getTaskForm() != null) {
					for (WfFormProperty property : wfTask.getTaskForm()) {

						if (property.getType().equals("conversation"))
							property.setValue(fixConversationMessage(property.getValue(), loggedInUser));
					}
				}

				activitiTaskSrv.claim(wfTask.getId(), assigneeId);
				mailService.sendTaskAssignedMail(assigneeId, wfTask.getId(), wfTask.getName(), wfTask.getDueDate());

			} catch (ActivitiException e) {
				logger.error("Failed to assign task " + e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}
			// the person who request to assign the task, is not supervisor for
			// the task or admin
		} else
			throw new InvalidRequestException("noAuthorizedToAssignTask");
	}

	/**
	 * Assigns a task to given assignee (used for tasks with document as form
	 * property)
	 * 
	 * @param wfTask
	 *            The task to be assigned to
	 * 
	 * @param assigneeId
	 *            The assignee's email
	 * 
	 * @param files
	 *            Task's files
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void assignTask(WfTask wfTask, String assigneeId, MultipartFile[] files) throws InvalidRequestException {
		String loggedInUserEmail = getAccessToken().getEmail();
		String loggedInUserName = getAccessToken().getName();

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
			throw new InvalidRequestException("claimTaskInstanceSuspended");

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_DELETED))
			throw new InvalidRequestException("claimTaskInstanceDeleted");

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_ENDED))
			throw new InvalidRequestException("claimTaskInstanceEnded");

		// check if task is supervised by the person who request to assign the
		// task or if is admin
		if (wfTask.getProcessInstance().getSupervisor().equals(loggedInUserEmail) || hasRole(ROLE_ADMIN)) {

			Map<String, MultipartFile> filesMap = new HashMap<String, MultipartFile>();

			for (MultipartFile file : files) {
				filesMap.put(file.getOriginalFilename(), file);
			}

			Folder folder = cmisFolder.getFolderById(wfTask.getProcessInstance().getFolderId());

			try {

				if (wfTask.getTaskForm() != null) {

					for (WfFormProperty property : wfTask.getTaskForm()) {

						if (property.getType().equals("document")) {

							ObjectMapper mapper = new ObjectMapper();
							WfDocument wfDocument;

							try {
								wfDocument = mapper.readValue(property.getValue(), WfDocument.class);

								MultipartFile file = filesMap.get(property.getId());

								Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(),
										file.getContentType());
								document.refresh();

								wfDocument.setDocumentId(document.getId());
								wfDocument.setVersion(document.getVersionLabel());

								Calendar now = Calendar.getInstance();

								DocumentType documentType = new DocumentType();
								documentType.setTitle(wfDocument.getTitle());
								documentType.setVersion(wfDocument.getVersion());
								documentType.setDocumentId(wfDocument.getDocumentId());
								documentType.setAuthor(loggedInUserName);
								documentType.setAuthorId(loggedInUserEmail);
								documentType.setSubmittedDate(now.getTime());
								documentType.setRefNo(wfDocument.getRefNo());

								property.setValue(mapper.writeValueAsString(documentType));

							} catch (JsonParseException e) {
								e.printStackTrace();
							} catch (JsonMappingException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}

						} else if (property.getType().equals("conversation")) {
							property.setValue(fixConversationMessage(property.getValue(), loggedInUserEmail));
						}
					}
				}

				activitiTaskSrv.claim(wfTask.getId(), assigneeId);
				mailService.sendTaskAssignedMail(assigneeId, wfTask.getId(), wfTask.getName(), wfTask.getDueDate());

			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}
			// the person who request to assign the task, is not supervisor for
			// the task or admin
		} else
			throw new InvalidRequestException("noAuthorizedToAssignTask");
	}

	/**
	 * Removes assignee from a task
	 * 
	 * @param taskId
	 *            Task's id to remove assignee from
	 * 
	 * @throws InvalidRequestException
	 */
	public void unClaimTask(String taskId) throws InvalidRequestException {
		String user = getAccessToken().getEmail();
		Task task;
		WorkflowInstance instance;

		try {
			task = activitiTaskSrv.createTaskQuery().taskId(taskId).singleResult();
			instance = processRepository.getInstanceById(task.getProcessInstanceId());

			if (instance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
				throw new InvalidRequestException("claimTaskInstanceSuspended");

			if (instance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
				throw new InvalidRequestException("claimTaskInstanceDeleted");

			if (instance.getStatus().equals(WorkflowInstance.STATUS_ENDED))
				throw new InvalidRequestException("claimTaskInstanceEnded");

			// assume that the exception equals to a deleted instance
		} catch (Exception noResult) {
			logger.error(noResult.getMessage());
			throw new InvalidRequestException("claimTaskInstanceDeleted");
		}

		// check if user is supervisor of the task's instance, or the assignee
		// itself or user has role admin
		if (instance.getSupervisor().equals(user) || task.getAssignee().equals(user) || hasRole(ROLE_ADMIN)) {
			try {
				activitiTaskSrv.unclaim(taskId);

			} catch (Exception e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}

		} else
			throw new InvalidRequestException("noAuthorizedToUnclaim");
	}

	/**
	 * Sets logged in user as assignee to a task
	 * 
	 * @param taskId
	 *            The task's id to set assignee
	 * 
	 * @throws InvalidRequestException
	 */
	public void claimTask(String taskId) throws InvalidRequestException {
		Task task;
		WorkflowInstance instance;

		try {
			task = activitiTaskSrv.createTaskQuery().taskId(taskId).singleResult();
			instance = processRepository.getInstanceById(task.getProcessInstanceId());

			if (instance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
				throw new InvalidRequestException("claimTaskInstanceSuspended");

			if (instance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
				throw new InvalidRequestException("claimTaskInstanceDeleted");

			if (instance.getStatus().equals(WorkflowInstance.STATUS_ENDED))
				throw new InvalidRequestException("claimTaskInstanceEnded");

			activitiTaskSrv.claim(taskId, getAccessToken().getEmail());

			// assume that the exception equals to a deleted instance
		} catch (Exception noResult) {
			logger.error(noResult.getMessage());
			throw new InvalidRequestException("claimTaskInstanceDeleted");
		}
	}

	/**
	 * Returns a list of Wftasks to be claim by user according to user role
	 * 
	 * @return
	 */
	public List<WfTask> getCandidateUserTasks() throws InvalidRequestException {

		// list to store tasks according to user role/group
		List<Task> taskList = new ArrayList<Task>();

		// final list with tasks to be claimed by user
		List<WfTask> returnList = new ArrayList<WfTask>();

		AccessToken token = getAccessToken();

		Set<String> userRoles = token.getRealmAccess().getRoles();

		// get active tasks
		List<Task> tasks = activitiTaskSrv.createTaskQuery().active().taskUnassigned().orderByTaskId().asc()
				.orderByProcessInstanceId().asc().list();

		List<String> userGroups = realmService.getUserGroups();

		// filter tasks according to group and roles
		for (Task task : tasks) {
			DefinitionVersion definitionVersion = processRepository
					.getVersionByProcessDefinitionId(task.getProcessDefinitionId());

			List<String[]> taskGroupAndRoles = getCandidateGroupAndRole(task.getId());

			// exclude tasks marked as to be assigned by suppervisor
			if (definitionVersion.getWorkflowDefinition().isAssignBySupervisor())
				continue;

			// if no restrictions apply to groups and roles add task to result
			if (taskGroupAndRoles.size() == 0) {
				taskList.add(task);
				continue;
			}

			// check every group-role designator for match with user gorups and
			// roles
			boolean groupOk = true;
			boolean roleOk = true;

			for (String[] groupAndRole : taskGroupAndRoles) {
				if (!groupAndRole[0].isEmpty() && !userGroups.contains(groupAndRole[0]))
					groupOk = false;

				if (!groupAndRole[1].isEmpty() && !userRoles.contains(groupAndRole[1]))
					roleOk = false;

				if (!groupOk)
					break;

				if (!roleOk)
					break;
			}

			if (groupOk && roleOk)
				taskList.add(task);
		}

		for (Task task : taskList) {
			TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());

			// check if task's instance is running
			if (taskPath.getInstance().getStatus().equals(WorkflowInstance.STATUS_RUNNING)) {

				// check if tasks is assignable only by supervisor
				if (!taskPath.getTaskDetails().isAssign()) {
					WfTask hydratedTask = new WfTask(task);

					hydratedTask.setStartForm(taskPath.getDefinition().hasStartForm());
					hydratedTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));
					hydrateTask(hydratedTask);

					returnList.add(hydratedTask);
				}
			}
		}
		return returnList;
	}

	/**
	 * Helper function to get links from task
	 * 
	 * @param taskId
	 * @return
	 */
	private List<String[]> getCandidateGroupAndRole(String taskId) {

		List<String[]> result = new ArrayList<String[]>();
		String[] groupAndRole;

		List<IdentityLink> links = activitiTaskSrv.getIdentityLinksForTask(taskId);
		for (IdentityLink link : links) {
			if (IdentityLinkType.CANDIDATE.equals(link.getType())) {

				String candidateExpr = " " + link.getGroupId() + " : ";
				String[] splittedVals = candidateExpr.split(":");

				String term1 = splittedVals[0].trim();
				String term2 = splittedVals[1].trim();

				if (term1.isEmpty() && term2.isEmpty()) {
					return result;
				}

				if (!term1.isEmpty() && term2.isEmpty()) {
					if (splittedVals.length == 2) {
						// role only (term1)
						groupAndRole = new String[2];
						groupAndRole[1] = term1;
						groupAndRole[0] = "";

					} else {
						// group only (term1)
						groupAndRole = new String[2];
						groupAndRole[0] = term1;
						groupAndRole[1] = "";
					}
				} else if (term1.isEmpty() && !term2.isEmpty()) {
					// role only (term2)
					groupAndRole = new String[2];
					groupAndRole[1] = term2;
					groupAndRole[0] = "";

				} else {
					// term1 = group, term2: role
					groupAndRole = new String[2];
					groupAndRole[0] = term1;
					groupAndRole[1] = term2;
				}
				result.add(groupAndRole);
			}
		}
		return result;
	}

	@Scheduled(cron = "${mail.sendCronString}")
	public void evaluateAlerts() {

		logger.info("Evaluate Alerts");

		WorkflowSettings settings = getSettings();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Date today = cal.getTime();

		cal.add(Calendar.DATE, settings.getDuedateAlertPeriod() + 1);

		Date alertDate = cal.getTime();

		List<Task> dueTasks = activitiTaskSrv.createTaskQuery().active().taskDueBefore(alertDate).taskDueAfter(today)
				.list();

		for (Task task : dueTasks) {

			String recipient = task.getAssignee();
			boolean unAssigned = false;

			if (recipient == null) {

				WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
				recipient = instance.getSupervisor();
				unAssigned = true;
			}

			mailService.sendDueTaskMail(recipient, task.getId(), task.getName(), task.getDueDate(), unAssigned);
		}

		List<Task> expiredTasks = activitiTaskSrv.createTaskQuery().active().taskDueBefore(today).list();

		for (Task task : expiredTasks) {

			String recipient = task.getAssignee();
			boolean unAssigned = false;

			if (recipient == null) {

				WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
				recipient = instance.getSupervisor();
				unAssigned = true;
			}

			mailService.sendTaskExpiredMail(recipient, task.getId(), task.getName(), task.getDueDate(), unAssigned);
		}
	}

	/**
	 * Returns a workflow defition by workflow definition key
	 * 
	 * @param definitionKey
	 * @return
	 */
	public WorkflowDefinition getDefinitionByKey(String definitionKey) {

		return processRepository.getDefinitionByKey(definitionKey);
	}

	/**
	 * 
	 * @param wfTaskDetails
	 * @return
	 * @throws InvalidRequestException
	 */
	@Transactional
	public WfTaskDetails updateTaskDetails(WfTaskDetails wfTaskDetails) throws InvalidRequestException {

		UserTaskDetails taskDetails;
		try {
			taskDetails = processRepository.getUserTaskDetailsById(wfTaskDetails.getId());

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No task details entity was found with the given id");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(taskDetails.getDefinitionVersion().getWorkflowDefinition().getOwner())) {
			taskDetails.updateFrom(wfTaskDetails);
			taskDetails = processRepository.save(taskDetails);

			return new WfTaskDetails(taskDetails);
		} else
			throw new InvalidRequestException("You are not authorized to update the task details");
	}

	/**
	 * Called from listener. Notifies the end of a process.
	 * 
	 * @param processInstanceId
	 */
	@Transactional
	public void notifyInstanceEnding(String processInstanceId) {
		WorkflowInstance instance = processRepository.getProcessInstance(processInstanceId);
		instance.setStatus(WorkflowInstance.STATUS_ENDED);
		instance.setEndDate(new Date());
		processRepository.save(instance);
	}

	/**
	 * Used to start any instance with or wo documents.<br>
	 * Creates a record in workflowinstance's table by getting the variable
	 * values from the
	 * {@link ProcessService#startProcess(WorkflowDefinition, WfProcessInstance, String, String, MultipartFile[])}
	 * if the start form has files and the <br>
	 * {@link ProcessService#startProcess(WorkflowDefinition, WfProcessInstance, String)}
	 * if there are no files in the start form of the instance.
	 * 
	 * Those two function in general, prepare the instance to be created and
	 * sumbit/start the variables to activiti.
	 * 
	 * 
	 * @param processInstanceId
	 *            From the start event the process instance id
	 */
	@Transactional
	public void notifyInstanceStarted(String processInstanceId) throws ServiceException {
		// get instance's variables, after we added them from startProcess
		// function
		Map<String, Object> instanceVariables = activitiRuntimeSrv.getVariables(processInstanceId);

		try {
			// implicity add instance's id variable to instance
			activitiRuntimeSrv.setVariable(processInstanceId, "instanceId", processInstanceId);

			// checking if not null else it will throw an exception ?!
			if (instanceVariables != null) {
				WorkflowInstance workflowInstance = new WorkflowInstance();

				workflowInstance.setId(processInstanceId);
				workflowInstance.setFolderId((String) instanceVariables.get("folderId"));
				workflowInstance.setTitle((String) instanceVariables.get("instanceTitle"));
				workflowInstance.setSupervisor((String) instanceVariables.get("instanceSupervisor"));
				workflowInstance.setStartDate(new Date());
				workflowInstance.setStatus(WorkflowInstance.STATUS_RUNNING);

				String definitionVersionId = (String) instanceVariables.get("definitionVersionId");

				DefinitionVersion definitionVersion = processRepository
						.getDefinitionVersionById(Integer.parseInt(definitionVersionId));
				workflowInstance.setDefinitionVersion(definitionVersion);

				processRepository.save(workflowInstance);

			} else
				throw new ServiceException("Failed to start instance", "Please contact system administator");

		} catch (ServiceException ex) {
			throw new ServiceException(ex.getCode(), ex.getMessage());
		}
	}

	/**
	 * Returns all active tasks
	 * 
	 * @return
	 */
	@Transactional
	public List<WfTask> getAllActiveTasks() {
		List<WfTask> wfTasks = new ArrayList<WfTask>();

		// get all active tasks
		List<Task> tasks = activitiTaskSrv.createTaskQuery().active().list();

		for (Task task : tasks) {
			WfTask wfTask = new WfTask(task);
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());

			wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
			wfTask.setDefinitionName(instance.getDefinitionVersion().getWorkflowDefinition().getName());
			wfTask.setProcessInstance(new WfProcessInstance(instance));

			wfTasks.add(wfTask);
		}

		return wfTasks;
	}

	/**
	 * Returns tasks of ended processes based on certain criteria.
	 * 
	 * @param title
	 * @param after
	 * @param before
	 * @param anonymous
	 * @return
	 */
	public List<WfTask> getEndedProcessInstancesTasks(String title, long after, long before, boolean anonymous) {

		List<WfTask> wfTasks = new ArrayList<WfTask>();

		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);

		if (title.isEmpty() || title.equals(" "))
			title = null;
		String assignee = (anonymous) ? null : getAccessToken().getEmail();

		List<HistoricTaskInstance> historicTasks = activitiHistorySrv.createHistoricTaskInstanceQuery()
				.taskAssignee(assignee).processFinished().list();

		WorkflowInstance instance;

		for (HistoricTaskInstance hit : historicTasks) {
			try {
				instance = processRepository.getInstanceById(hit.getProcessInstanceId());
			} catch (EmptyResultDataAccessException e) {
				instance = null;
			}
			if (instance != null) {
				String instanceTitle = instance.getTitle().toLowerCase();

				if (dateBefore.getTime() == 0) {
					dateBefore = new Date();
				}

				if (instance.getStatus().equals(WorkflowInstance.STATUS_ENDED)
						&& (title == null || instanceTitle.indexOf(title.toLowerCase()) > -1)
						&& instance.getEndDate().after(dateAfter) && instance.getEndDate().before(dateBefore)
						&& (assignee == null || instance.getSupervisor().equals(assignee))) {

					WfTask wfTask = new WfTask(hit);
					wfTask.setProcessInstance(new WfProcessInstance(instance));
					wfTasks.add(wfTask);
				}
			}
		}

		return wfTasks;
	}

	/**
	 * Get all tasks of specified user
	 * 
	 * @param after
	 * @param before
	 * @param userId
	 * @return
	 * @throws InvalidRequestException
	 */
	public List<WfTask> getUserActivity(long after, long before, String userId) throws InvalidRequestException {
		List<WfTask> wfTasks = new ArrayList<WfTask>();
		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);

		String assignee = "";

		if (userId != null && !assignee.equals(""))
			assignee = realmService.getUser(userId).getEmail();

		HistoricTaskInstanceQuery taskQuery = activitiHistorySrv.createHistoricTaskInstanceQuery();

		if (assignee != null && !assignee.equals(""))
			taskQuery.taskAssignee(assignee);

		if (after != 0)
			taskQuery.taskCreatedAfter(dateAfter);

		if (before != 0)
			taskQuery.taskCreatedBefore(dateBefore);

		List<HistoricTaskInstance> historicTasks = taskQuery.list();

		for (HistoricTaskInstance hit : historicTasks) {
			WfTask wfTask = new WfTask(hit);

			try {
				WorkflowInstance instance = processRepository.getInstanceById(hit.getProcessInstanceId());

				WorkflowDefinition workflowDefinition = instance.getDefinitionVersion().getWorkflowDefinition();

				wfTask.setProcessInstance(new WfProcessInstance(instance));
				wfTask.setIcon(workflowDefinition.getIcon());
				wfTask.setDefinitionName(workflowDefinition.getName());
				wfTask.setProcessId(workflowDefinition.getId());

				// finally add task if not already exists to return list
				if (!wfTasks.contains(wfTask))
					wfTasks.add(hydrateTask(wfTask));

			} catch (Exception e) {

			}
		}

		return wfTasks;
	}

	/**
	 * <p>
	 * This function is called when a new task is created.
	 * </p>
	 * <p>
	 * Based on system settings will check if its enabled to assign a user to
	 * the task if there is only one candidate.
	 * </p>
	 * <p>
	 * Also will check if the assigned notification option is enabled in order
	 * to send an email to the user who has been assigned to a task
	 * </p>
	 * 
	 * @param task
	 *            Task which has been created
	 */
	@Transactional
	public void applyTaskSettings(Task task) {
		WorkflowSettings settings = getSettings();
		List<WfUser> users = getCandidatesByTaskId(task.getId());

		if (users == null || users.isEmpty()) {
			String adminEmail = environment.getProperty("mail.admin");

			WorkflowDefinition workflowDef = processRepository.getProcessByDefinitionId(task.getProcessDefinitionId());
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
			mailService.sendBpmnErrorEmail(adminEmail, workflowDef, task.getName(), instance.getSupervisor());
			return;
		}

		if (!settings.isAutoAssignment() || users.size() > 1)
			return;

		String userEmail = users.get(0).getEmail();

		activitiTaskSrv.claim(task.getId(), userEmail);

		if (settings.isAssignmentNotification())
			mailService.sendTaskAssignedMail(userEmail, task.getId(), task.getName(), task.getDueDate());
	}

	/**
	 * Return the system settings
	 * 
	 * @return
	 */
	public WorkflowSettings getSettings() {

		WorkflowSettings settings = settingsStatus.getWorkflowSettings();

		if (settings == null) {
			settings = processRepository.getSettings();
			settingsStatus.setWorkflowSettings(settings);
		}

		return settings;
	}

	/**
	 * Updates system settings
	 * 
	 * @param wfSettings
	 *            {@link WfSettings} The updated settings
	 * 
	 * @return {@link WfSettings} The updated settings
	 */
	@Transactional
	public WorkflowSettings updateSettings(WfSettings wfSettings) {

		WorkflowSettings settings = new WorkflowSettings(wfSettings);
		settingsStatus.setWorkflowSettings(settings);

		return processRepository.updateSettings(settings);
	}

	/**
	 * Update the system settings
	 * 
	 * @param settings
	 * @return
	 */
	@Transactional
	public WorkflowSettings updateSettings(WorkflowSettings settings) {
		settingsStatus.setWorkflowSettings(settings);
		return processRepository.updateSettings(settings);
	}

	/**
	 * Saves a task form element
	 * 
	 * @param wfFormProperty
	 *            {@link WfFormProperty} contains the details for the task's
	 *            form property
	 * 
	 * @param taskDefinitionKey
	 *            The task's definition key (not the task's id)
	 * 
	 * @param definitionVersion
	 *            Process version's id
	 * 
	 * 
	 * @return The saved {@link UserTaskFormElement}
	 */
	@Transactional
	public UserTaskFormElement saveTaskFormElement(WfFormProperty wfFormProperty, String taskDefinitionKey,
			String definitionVersion) {

		UserTaskFormElement taskFormElement = processRepository.getUserTaskFromElement(definitionVersion,
				taskDefinitionKey, wfFormProperty.getId());

		taskFormElement.setDescription(wfFormProperty.getDescription());
		taskFormElement.setDevice(wfFormProperty.getDevice());

		return processRepository.save(taskFormElement);
	}

	/**
	 * Returns the start form which the instance started
	 * 
	 * @param instanceId
	 * @return
	 * @throws InvalidRequestException
	 */
	public List<WfFormProperty> getStartFormByInstanceId(String instanceId) throws InvalidRequestException {

		List<WfFormProperty> returnList = new ArrayList<WfFormProperty>();

		List<HistoricDetail> taskDetails = activitiHistorySrv.createHistoricDetailQuery().formProperties().taskId(null)
				.processInstanceId(instanceId).list();
		Map<String, String> detailMap = new LinkedHashMap<String, String>();

		WorkflowInstance instance = processRepository.getInstanceById(instanceId);

		// fill the map using as key the detail
		for (HistoricDetail detail : taskDetails) {
			HistoricFormProperty historicFormProperty = (HistoricFormProperty) detail;
			detailMap.put(historicFormProperty.getPropertyId(), historicFormProperty.getPropertyValue());

		}

		List<org.activiti.bpmn.model.FormProperty> formProperties = ActivitiHelper.getTaskFormDefinition(
				activitiRepositorySrv, instance.getDefinitionVersion().getWorkflowDefinition().getKey());

		for (org.activiti.bpmn.model.FormProperty formPropery : formProperties) {

			String propertyValue = detailMap.get(formPropery.getId());

			// prepare formValues
			Map<String, String> values = new HashMap<String, String>();

			// date pattern
			String dateFormat = "";

			if (formPropery.getType().equals("enum")) {

				List<FormValue> formValues = formPropery.getFormValues();

				for (int i = 0; formValues != null && i < formValues.size(); i++) {
					values.put(formValues.get(i).getId(), formValues.get(i).getName());
				}

			} else if (formPropery.getType().equals("date")) {

				dateFormat = CustomTaskFormFields.DATETIME_PATTERN_PRESENTATION;
				TimeZone timeZone = TimeZone.getTimeZone("UTC");

				if (propertyValue != null) {
					Calendar dt = Calendar.getInstance(timeZone);

					Calendar refDt = javax.xml.bind.DatatypeConverter.parseDateTime(propertyValue);
					dt.setTimeInMillis(refDt.getTimeInMillis());

					DateFormat df = new SimpleDateFormat(dateFormat);

					df.setTimeZone(timeZone);
					propertyValue = df.format(dt.getTime());
				}
			}

			WfFormProperty wfProperty = null;

			wfProperty = new WfFormProperty(formPropery.getId(), formPropery.getName(), formPropery.getType(),
					propertyValue, formPropery.isReadable(), formPropery.isWriteable(), formPropery.isRequired(),
					values, dateFormat, "", // usertaskform element description
					"" // usertaskform element client
			);
			returnList.add(wfProperty);
		}

		return returnList;
	}

	/**
	 * 
	 * @param instanceId
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfProcessInstance getProcessInstanceById(String instanceId) throws InvalidRequestException {

		WfProcessInstance wfProcessInstance;

		try {
			wfProcessInstance = new WfProcessInstance(processRepository.getInstanceById(instanceId));
		} catch (Exception e) {
			throw new InvalidRequestException("Request not found");
		}

		return wfProcessInstance;
	}

	/**
	 * Deletes an instance by instance id
	 * 
	 * @param instanceId
	 */
	@Transactional
	public void deleteProcessCompletedInstance(String instanceId) {
		// delete from activiti
		activitiHistorySrv.deleteHistoricProcessInstance(instanceId);

		// delete from workflow instance table
		processRepository.deleteProcessInstance(instanceId);
	}

	/**
	 * Get supervisors by process id
	 * 
	 * @param processId
	 * @return
	 * @throws InvalidRequestException
	 */
	public List<WfUser> getSupervisorsByProcess(int processId) throws InvalidRequestException {
		WfProcess process = new WfProcess(processRepository.getById(processId));

		return realmService.getUsersByGroupAndRole(process.getOwner(), ROLE_SUPERVISOR);
	}

	/**
	 * Returns all in progress instances
	 * 
	 * @return a list of in progress instances
	 * @throws InvalidRequestException
	 */
	public List<WfProcessInstance> getInProgressInstances() throws InvalidRequestException {
		ArrayList<WfProcessInstance> returnList = new ArrayList<WfProcessInstance>();

		returnList.addAll(WfProcessInstance.fromWorkflowInstances(processRepository.getInProgressInstances()));

		return returnList;
	}

	@Transactional(rollbackFor = Exception.class)
	public void changeInstanceSupervisor(String instanceId, String supervisor) throws InvalidRequestException {

		WorkflowInstance instance = null;

		try {
			instance = processRepository.getInstanceById(instanceId);
			instance.setSupervisor(supervisor);
			processRepository.save(instance);
		} catch (Exception e) {
			throw new InvalidRequestException("Instance supervisor change failed");
		}
	}

	/**
	 * Private method for retrieving logged user token
	 * 
	 * @return Logged-in user's token
	 */
	private AccessToken getAccessToken() {
		KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();
		AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();

		return token;
	}

	/**
	 * Hydrate a wfTask with extra information
	 * 
	 * <br>
	 * 
	 * TODO: Hydrate task function should be responsible to set instance, icon,
	 * process definition etc. Should remove from any other point that uses the
	 * hydrate the settting task's properties. <br>
	 * 
	 * <p>
	 * <strong>Also check for redunant "set" of various properties!!!</strong>
	 * </p>
	 * 
	 * @param wfTask
	 *            The task to be hydrated with extra info
	 * 
	 * @return {@link WfTask} The hydrated task
	 * 
	 * @throws InvalidRequestException
	 */
	private WfTask hydrateTask(WfTask wfTask) throws InvalidRequestException {
		List<WfFormProperty> formProperties = new ArrayList<WfFormProperty>();

		// for task which is running
		try {
			TaskFormData taskForm = activitiFormSrv.getTaskFormData(wfTask.getId());
			formProperties = getWfFormProperties(taskForm.getFormProperties(), wfTask);

			// handle completed task
		} catch (ActivitiObjectNotFoundException e) {

			HistoricTaskInstance historicTaskInstance = activitiHistorySrv.createHistoricTaskInstanceQuery()
					.taskId(wfTask.getId()).singleResult();
			List<org.activiti.bpmn.model.FormProperty> historicFormProperties = new ArrayList<org.activiti.bpmn.model.FormProperty>();
			List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
			Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
			UserTaskDetails taskDetails = new UserTaskDetails();

			// get properties for task
			historicFormProperties = ActivitiHelper.getTaskFormDefinition(activitiRepositorySrv,
					wfTask.getProcessDefinitionId(), historicTaskInstance.getTaskDefinitionKey());

			// get the task details
			taskDetails = processRepository.getUserTaskDetailByDefinitionKey(
					historicTaskInstance.getTaskDefinitionKey(), wfTask.getProcessDefinitionId());

			// get the task form elements
			taskFormElements = processRepository.getUserTaskFromElements(wfTask.getProcessDefinitionId(),
					taskDetails.getId());

			// fill the usertaskform element map using as key the element id and
			// as value the user taskform element
			for (UserTaskFormElement userTaskFormElement : taskFormElements) {
				mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
			}

			// get the map contains the value as a map of property id and
			// property value
			HashMap<String, String> propertyValueMap = getFormItemsValues(wfTask.getProcessInstance().getId(),
					wfTask.getEndDate());

			// loop through form properties
			for (org.activiti.bpmn.model.FormProperty formProperty : historicFormProperties) {

				// get the property value from the map using the property id
				String propertyValue = propertyValueMap.get(formProperty.getId());

				// prepare formValues
				Map<String, String> values = new HashMap<String, String>();

				if (formProperty.getType().equals("enum")) {
					List<FormValue> formValues = formProperty.getFormValues();

					for (int i = 0; formValues != null && i < formValues.size(); i++) {
						values.put(formValues.get(i).getId(), formValues.get(i).getName());
					}
				}

				// date pattern
				String dateFormat = "";

				if (formProperty.getDatePattern() != null)
					dateFormat = formProperty.getDatePattern();
				else
					// get the default date pattern
					dateFormat = CustomTaskFormFields.DATETIME_PATTERN_PRESENTATION;

				// get the task form element values
				UserTaskFormElement userTaskFormElement = null;

				if (!mappedUserTaskFormElements.isEmpty())
					userTaskFormElement = mappedUserTaskFormElements.get(formProperty.getId());

				// create new form property and add it to list
				WfFormProperty wfFormProperty = new WfFormProperty();
				wfFormProperty.setId(formProperty.getId());
				wfFormProperty.setName(formProperty.getName());
				wfFormProperty.setType(formProperty.getType());
				wfFormProperty.setValue(propertyValue);
				wfFormProperty.setReadable(formProperty.isReadable());
				wfFormProperty.setWritable(formProperty.isWriteable());
				wfFormProperty.setRequired(formProperty.isRequired());
				wfFormProperty.setFormValues(values);
				wfFormProperty.setFormat(dateFormat);
				wfFormProperty.setDescription(userTaskFormElement.getDescription());
				wfFormProperty.setDevice(userTaskFormElement.getDevice());

				formProperties.add(wfFormProperty);
			}
		}

		wfTask.setTaskForm(formProperties);
		DefinitionVersion definitionVersion = processRepository
				.getVersionByProcessDefinitionId(wfTask.getProcessDefinitionId());
		wfTask.initFromDefinitionVersion(definitionVersion);
		wfTask.setIcon(definitionVersion.getWorkflowDefinition().getIcon());
		wfTask.setDefinitionName(definitionVersion.getWorkflowDefinition().getName());

		return wfTask;
	}

	/**
	 * Returns a map formPropertyId-value for the tasks which are completed and
	 * the value is not accessible from any activiti service.<br>
	 * 
	 * @param instanceId
	 *            The instance's id
	 * 
	 * @param taskEndDate
	 *            The task's end date
	 * 
	 * @return A {@link HashMap} formPropertyId-formPropertyValue
	 * 
	 * @throws InvalidRequestException
	 */
	private HashMap<String, String> getFormItemsValues(String instanceId, Date taskEndDate) {
		HashMap<String, String> returnMap = new HashMap<>();
		List<HistoricDetail> details = activitiHistorySrv.createHistoricDetailQuery().processInstanceId(instanceId)
				.orderByTime().desc().list();

		for (HistoricDetail historicDetail : details) {
			if (historicDetail instanceof HistoricFormPropertyEntity) {
				HistoricFormPropertyEntity formEntity = (HistoricFormPropertyEntity) historicDetail;

				if (formEntity.getTime().before(taskEndDate) || formEntity.getTime().equals(taskEndDate)) {

					if (formEntity.getPropertyValue() != null)
						returnMap.put(formEntity.getPropertyId(), formEntity.getPropertyValue());
				}
			}
		}
		return returnMap;
	}

	@SuppressWarnings("unchecked")
	private List<WfFormProperty> getWfFormProperties(List<FormProperty> formProperties, WfTask wfTask) {
		List<WfFormProperty> wfFormProperties = new ArrayList<WfFormProperty>();
		List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
		Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();

		Task task = activitiTaskSrv.createTaskQuery().taskId(wfTask.getId()).singleResult();

		UserTaskDetails taskDetails = processRepository.getUserTaskDetailByDefinitionKey(task.getTaskDefinitionKey(),
				wfTask.getProcessDefinitionId());

		taskFormElements = processRepository.getUserTaskFromElements(wfTask.getProcessDefinitionId(),
				taskDetails.getId());

		// create the map
		for (UserTaskFormElement userTaskFormElement : taskFormElements) {
			mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
		}

		for (FormProperty property : formProperties) {
			// get the value for the property
			String propertyValue = property.getValue();

			// get the date format
			String dateFormat = (String) property.getType().getInformation("datePattern");

			// get the task form element values
			UserTaskFormElement userTaskFormElement = null;

			if (!mappedUserTaskFormElements.isEmpty())
				userTaskFormElement = mappedUserTaskFormElements.get(property.getId());

			// create the form property
			WfFormProperty wfProperty = new WfFormProperty();
			wfProperty.setId(property.getId());
			wfProperty.setName(property.getName());
			wfProperty.setType(property.getType().getName());
			wfProperty.setValue(propertyValue);
			wfProperty.setReadable(property.isReadable());
			wfProperty.setWritable(property.isWritable());
			wfProperty.setRequired(property.isRequired());
			wfProperty.setFormValues((Map<String, String>) property.getType().getInformation("values"));
			wfProperty.setFormat(dateFormat);
			wfProperty.setDescription(userTaskFormElement.getDescription());
			wfProperty.setDevice(userTaskFormElement.getDevice());

			wfFormProperties.add(wfProperty);
		}
		return wfFormProperties;
	}

	@SuppressWarnings("unchecked")
	private List<WfFormProperty> getWfFormProperties(List<FormProperty> formProperties) {
		List<WfFormProperty> wfFormProperties = new ArrayList<WfFormProperty>();

		for (FormProperty property : formProperties) {

			// get the date format
			String dateFormat = (String) property.getType().getInformation("datePattern");

			// get the property's value
			String propertyValue = property.getValue();

			// create the form property
			WfFormProperty wfFormProperty = new WfFormProperty();
			wfFormProperty.setId(property.getId());
			wfFormProperty.setName(property.getName());
			wfFormProperty.setType(property.getType().getName());
			wfFormProperty.setValue(propertyValue);
			wfFormProperty.setReadable(property.isReadable());
			wfFormProperty.setWritable(property.isWritable());
			wfFormProperty.setRequired(property.isRequired());
			wfFormProperty.setFormValues((Map<String, String>) property.getType().getInformation("values"));
			wfFormProperty.setFormat(dateFormat);
			wfFormProperty.setDevice("");
			wfFormProperty.setDescription("");

			wfFormProperties.add(wfFormProperty);
		}

		return wfFormProperties;
	}

	/**
	 * private
	 * 
	 * Creates the task details when a new workflow or a new version is created
	 * 
	 * @param workflow
	 */
	private void createTaskDetails(WorkflowDefinition workflow) {

		List<DefinitionVersion> definitionVersions = workflow.getDefinitionVersions();
		int indexOfUpToDateVersion = definitionVersions.size() - 1;
		DefinitionVersion latestDefinitionVersion = definitionVersions.get(indexOfUpToDateVersion);
		DefinitionVersion previousDefinitionVersion = null;

		if (indexOfUpToDateVersion > 0)
			previousDefinitionVersion = definitionVersions.get(indexOfUpToDateVersion - 1);

		BpmnModel bpmnModel = activitiRepositorySrv.getBpmnModel(latestDefinitionVersion.getProcessDefinitionId());

		List<org.activiti.bpmn.model.Process> processes = bpmnModel.getProcesses();
		for (org.activiti.bpmn.model.Process p : processes) {

			List<StartEvent> startEvents = p.findFlowElementsOfType(StartEvent.class);

			List<UserTask> userTasks = p.findFlowElementsOfType(UserTask.class);

			// create a user task detail for start event and then get the form
			// elements

			if (startEvents != null && !startEvents.isEmpty()) {
				for (StartEvent startEvent : startEvents) {
					UserTaskDetails startEventDetails = new UserTaskDetails();

					startEventDetails.setName(startEvent.getName());
					startEventDetails.setTaskId(startEvent.getId());

					String startEventDescription = (previousDefinitionVersion == null) ? ""
							: copyDescriptionFromSimilarTask(previousDefinitionVersion.getId(), startEvent.getName());

					startEventDetails.setDescription(startEventDescription);
					startEventDetails.setAssign(workflow.isAssignBySupervisor());
					startEventDetails.setDefinitionVersion(latestDefinitionVersion);
					startEventDetails.setType(UserTaskDetails.START_EVENT_TASK);
					startEventDetails = processRepository.save(startEventDetails);

					// get form elements from start event
					if (startEvent.getFormProperties().size() > 0) {
						for (org.activiti.bpmn.model.FormProperty formProperty : startEvent.getFormProperties()) {

							UserTaskFormElement userTaskFormElement = new UserTaskFormElement();
							userTaskFormElement.setUserTaskDetail(startEventDetails);

							String formItemDescription = (previousDefinitionVersion == null) ? ""
									: copyFormElementDescriptionFromSimilar(formProperty.getId(),
											startEventDetails.getId());
							userTaskFormElement.setDescription(formItemDescription);

							String formItemDevice = (previousDefinitionVersion == null)
									? UserTaskFormElement.ALL_DEVICES
									: copyFormElementDeviceFromSimilar(formProperty.getId(), startEventDetails.getId());
							userTaskFormElement.setDevice(formItemDevice);

							userTaskFormElement.setElementId(formProperty.getId());
							processRepository.save(userTaskFormElement);
						}
						// update definition start form property since we do
						// have form at start event
						workflow.setStartForm(true);

						// no start form found
					} else if (startEvent.getFormProperties() == null || startEvent.getFormProperties().size() == 0)
						workflow.setStartForm(false);
				}
			}

			if (userTasks != null && !userTasks.isEmpty()) {

				for (UserTask userTask : userTasks) {

					UserTaskDetails userTaskDetails = new UserTaskDetails();
					userTaskDetails.setName(userTask.getName());
					userTaskDetails.setTaskId(userTask.getId());

					String description = (previousDefinitionVersion == null) ? ""
							: copyDescriptionFromSimilarTask(previousDefinitionVersion.getId(), userTask.getName());

					userTaskDetails.setDescription(description);
					userTaskDetails.setAssign(workflow.isAssignBySupervisor());
					userTaskDetails.setDefinitionVersion(latestDefinitionVersion);
					userTaskDetails.setType(UserTaskDetails.USER_TASK);
					userTaskDetails = processRepository.save(userTaskDetails);

					List<org.activiti.bpmn.model.FormProperty> formProperties = ActivitiHelper.getTaskFormDefinition(
							activitiRepositorySrv, latestDefinitionVersion.getProcessDefinitionId(), userTask.getId());

					// create task form elements
					for (org.activiti.bpmn.model.FormProperty formProperty : formProperties) {
						UserTaskFormElement userTaskFormElement = new UserTaskFormElement();
						userTaskFormElement.setUserTaskDetail(userTaskDetails);

						String formItemDescription = (previousDefinitionVersion == null) ? ""
								: copyFormElementDescriptionFromSimilar(formProperty.getId(), userTaskDetails.getId());
						userTaskFormElement.setDescription(formItemDescription);

						String formItemDevice = (previousDefinitionVersion == null) ? UserTaskFormElement.ALL_DEVICES
								: copyFormElementDeviceFromSimilar(formProperty.getId(), userTaskDetails.getId());
						userTaskFormElement.setDevice(formItemDevice);

						userTaskFormElement.setElementId(formProperty.getId());
						processRepository.save(userTaskFormElement);
					}
				}
			}
		}
	}

	/**
	 * private
	 * 
	 * Checks for user tasks with the same name between the currently deployed
	 * and the latest version, so that it is possible to copy the descriptions
	 * 
	 * @param id
	 * @param taskName
	 * @return
	 */
	private String copyDescriptionFromSimilarTask(int id, String taskName) {
		List<UserTaskDetails> taskDetails = processRepository.getVersionTaskDetails(id);
		for (UserTaskDetails task : taskDetails) {
			if (task.getName().equals(taskName)) {
				if (task.getDescription() != null && !task.getDescription().isEmpty()) {
					return task.getDescription();
				}
			}
		}
		return "";
	}

	private String copyFormElementDescriptionFromSimilar(String elementId, int taskDetailId) {

		List<UserTaskFormElement> userTaskFormElemets = processRepository.getUserTaskFormElements(elementId,
				taskDetailId);

		for (UserTaskFormElement element : userTaskFormElemets) {

			if (element.getElementId().equals(elementId)) {

				if (element.getDescription() != null && !element.getDescription().isEmpty())
					return element.getDescription();
			}

		}

		return "";
	}

	private String copyFormElementDeviceFromSimilar(String elementId, int taskDetailId) {

		List<UserTaskFormElement> userTaskFormElemets = processRepository.getUserTaskFormElements(elementId,
				taskDetailId);

		for (UserTaskFormElement element : userTaskFormElemets) {

			if (element.getElementId().equals(elementId)) {

				if (element.getDevice() != null && !element.getDevice().isEmpty())
					return element.getDevice();
			}
		}

		return UserTaskFormElement.ALL_DEVICES;
	}

	/**
	 * Checks if a process already exists with the same key
	 * 
	 * @param key
	 * @return
	 */
	private boolean definitionExistenceCheck(String key) {
		List<ProcessDefinition> processDefinitions = activitiRepositorySrv.createProcessDefinitionQuery()
				.processDefinitionKey(key).list();

		if (processDefinitions == null || processDefinitions.isEmpty())
			return false;

		return true;
	}

	/**
	 * Checks whether the new version to be deployed has the same key with the
	 * latest (current) version.
	 * 
	 * @param workflowId
	 * @param key
	 * @return
	 */
	private boolean definitionVersionExistenceCheck(int workflowId, String key) {

		List<DefinitionVersion> definitionVersions = processRepository.getVersionsByProcessId(workflowId);

		if (definitionVersions == null || definitionVersions.isEmpty())
			return false;

		int lastVersionIndex = definitionVersions.size() - 1;

		DefinitionVersion lastVersion = definitionVersions.get(lastVersionIndex);

		if (lastVersion.getProcessDefinitionId().indexOf(key + ":") == -1)
			return false;

		return true;
	}

	/**
	 * private
	 * 
	 * Parses the bpmn file to retrieve the id of the process.
	 * 
	 * @param inputStream
	 * @return
	 * @throws InvalidRequestException
	 */

	private String parseProcessId(String bpmn) throws InvalidRequestException {

		String processId = null;

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();

		try {

			XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(bpmn));

			while (streamReader != null && streamReader.hasNext()) {
				streamReader.nextTag();

				if (streamReader.getLocalName().equals("process")) {
					processId = streamReader.getAttributeValue(null, "id");
					break;
				}
			}

		} catch (XMLStreamException e) {
			logger.error("BPMN XML Stream Exception. " + e.getMessage());
		}

		return processId;
	}

	/**
	 * Check if user has role
	 * 
	 * @param role
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	private boolean hasRole(String role) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();

		for (GrantedAuthority authority : authorities) {

			if (authority.getAuthority().equals(role))
				return true;
		}

		return false;
	}

	/**
	 * Check if user has group
	 * 
	 * @param group
	 * @return boolean
	 */
	private boolean hasGroup(String group) {
		List<String> userGroups = realmService.getUserGroups();

		if (userGroups == null || userGroups.size() == 0)
			return false;

		return userGroups.contains(group);
	}

	/**
	 * A custom exception which extends {@link ActivitiException}, used by
	 * service layer in order to catch activiti-relation exceptions and then
	 * throw an {@link InternalException} or {@link InvalidRequestException} to
	 * propagate it to the controller.
	 * 
	 * @author kkoutros
	 *
	 */
	class ServiceException extends ActivitiException {

		private static final long serialVersionUID = 1L;

		private String code;
		private String message;

		public ServiceException(String code, String message) {
			super(message);
			this.code = code;
			this.message = message;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}