/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.persistence.NonUniqueResultException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FormValue;
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
import org.activiti.engine.impl.form.FormPropertyHandler;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cyberstream.util.string.StringUtil;
import gr.cyberstream.workflow.engine.cmis.CMISDocument;
import gr.cyberstream.workflow.engine.cmis.CMISFolder;
import gr.cyberstream.workflow.engine.config.SettingsStatus;
import gr.cyberstream.workflow.engine.customtypes.ConversationType;
import gr.cyberstream.workflow.engine.customtypes.DocumentType;
import gr.cyberstream.workflow.engine.customtypes.MessageType;
import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.ExternalForm;
import gr.cyberstream.workflow.engine.model.Registry;
import gr.cyberstream.workflow.engine.model.TaskPath;
import gr.cyberstream.workflow.engine.model.UserTaskDetails;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowDefinitionStatus;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.WfDocument;
import gr.cyberstream.workflow.engine.model.api.WfExternalForm;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfProcessMetadata;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;
import gr.cyberstream.workflow.engine.model.api.WfUser;
import gr.cyberstream.workflow.engine.persistence.Processes;
import nl.captcha.Captcha;
import nl.captcha.text.producer.DefaultTextProducer;

/**
 * Implements all the business rules related to process definitions and process
 * instances
 * 
 * @author nlyke
 *
 */
@Service
public class ProcessService {

	final static Logger logger = LoggerFactory.getLogger(ProcessService.class);

	@Autowired
	Processes processRepository;

	@Autowired
	CMISFolder cmisFolder;

	@Autowired
	CMISDocument cmisDocument;

	@Autowired
	RepositoryService activitiRepositorySrv;

	@Autowired
	HistoryService activitiHistorySrv;

	@Autowired
	FormService activitiFormSrv;

	@Autowired
	TaskService activitiTaskSrv;

	@Autowired
	RuntimeService activitiRuntimeSrv;

	@Autowired
	RealmService realmService;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	SettingsStatus settingsStatus;
	
	private static byte[] keyBytes = {48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1};
	
	private static Key key = null;
		
	public ProcessService() {
		
		key = new SecretKeySpec(keyBytes, "AES");
	}
	
	// TODO:vpap
	/**
	 * 
	 * @param id
	 * @return
	 */
	public WfProcess getProcessById(int id) {
		WorkflowDefinition workflow = processRepository.getById(id);
		return new WfProcess(workflow);
	}

	// TODO:vpap
	/**
	 * 
	 * @return
	 */
	public List<WfProcess> getAll() {
		List<WorkflowDefinition> workflows = processRepository.getAll();
		return WfProcess.fromWorkflowDefinitions(workflows);
	}

	// TODO:vpap
	/**
	 * 
	 * @param id
	 * @return
	 */
	public List<WfTaskDetails> getVersionTaskDetails(int id) {
		List<UserTaskDetails> taskDetails = processRepository.getVersionTaskDetails(id);
		return WfTaskDetails.fromUserTaskDetails(taskDetails);
	}

	// TODO:vpap
	/**
	 * Return all running instances for a process with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public List<WfProcessInstance> getActiveProcessInstances(int id) {

		List<WorkflowInstance> workflowInstances = processRepository.getActiveProcessInstances(id);

		if (workflowInstances == null || workflowInstances.isEmpty())
			return null;

		List<WfProcessInstance> wfProcessInstances = new ArrayList<WfProcessInstance>();

		for (WorkflowInstance instance : workflowInstances) {
			wfProcessInstances.add(new WfProcessInstance(instance));
		}

		return wfProcessInstances;
	}

	// TODO:vpap
	/**
	 * Delete process instance
	 * 
	 * @param id
	 * @throws InvalidRequestException
	 */
	@Transactional
	public void cancelProcessInstance(String id) throws InvalidRequestException {
		
		WorkflowInstance instance;
		
		try{
			instance = processRepository.getInstanceById(id);
		}
		catch(EmptyResultDataAccessException e){
			throw new InvalidRequestException("Process instance with id " + id + " not found");
		}

		activitiRuntimeSrv.deleteProcessInstance(id, null);
		processRepository.cancelProcessInstance(instance);
	}

	// TODO:vpap
	/**
	 * Suspend process instance.
	 * 
	 * @param id
	 * @return
	 * @throws InvalidRequestException
	 */
	@Transactional
	public WfProcessInstance suspendProcessInstance(String id) throws InvalidRequestException {

		WorkflowInstance instance;
		
		try{
			instance = processRepository.getInstanceById(id);
		}
		catch(EmptyResultDataAccessException e){
			throw new InvalidRequestException("Process instance with id " + id + " not found");
		}

		try {
			activitiRuntimeSrv.suspendProcessInstanceById(id);
		} catch (ActivitiObjectNotFoundException nfe) {
			throw new InvalidRequestException(nfe.getMessage());
		} catch (ActivitiException aexc) {
			throw new InvalidRequestException(aexc.getMessage());
		}

		instance.setStatus(WorkflowInstance.STATUS_SUSPENDED);
		instance = processRepository.save(instance);

		return new WfProcessInstance(instance);
	}

	// TODO:vpap
	/**
	 * Resume a suspended process instance.
	 * 
	 * @param id
	 * @return
	 * @throws InvalidRequestException
	 */
	@Transactional
	public WfProcessInstance resumeProcessInstance(String id) throws InvalidRequestException {

		WorkflowInstance instance;
		
		try{
			instance = processRepository.getInstanceById(id);
		}
		catch(EmptyResultDataAccessException e){
			throw new InvalidRequestException("Process instance with id " + id + " not found");
		}

		try {
			activitiRuntimeSrv.activateProcessInstanceById(id);
		} catch (ActivitiObjectNotFoundException nfe) {
			throw new InvalidRequestException(nfe.getMessage());
		} catch (ActivitiException aexc) {
			throw new InvalidRequestException(aexc.getMessage());
		}

		instance.setStatus(WorkflowInstance.STATUS_RUNNING);
		instance = processRepository.save(instance);

		return new WfProcessInstance(instance);
	}

	/**
	 * Creates a new process definition from just its metadata. No BPMN
	 * definition is attached yet.
	 * 
	 * @param process
	 *            the metadata of the process
	 * @return the saved process definition
	 * @throws InvalidRequestException
	 */
	// TODO Validate groups/roles
	public WfProcess createNewProcessDefinition(WfProcess process) throws InvalidRequestException {

		WorkflowDefinition definition = new WorkflowDefinition();
		definition.updateFrom(process);

		// 1. apply some rules
		if (StringUtil.isEmpty(definition.getName())) {
			throw new InvalidRequestException("the name is required for the new process definition");
		}

		try {

			WorkflowDefinition sameNameWorkflowDefinition = processRepository.getByName(definition.getName());

			if (sameNameWorkflowDefinition != null) {

				process.setName(definition.getName() + " - "
						+ DateFormatUtils.format(Calendar.getInstance(), "d-M-yyyy HH.mm.ss"));
			}

		} catch (EmptyResultDataAccessException e) {
		}

		// 2. Initialize process definition
		definition.setActiveDeploymentId(null);

		// 3. Create Process Definition Folder
		Folder folder = cmisFolder.createFolder(null, definition.getName());
		definition.setFolderId(folder.getId());

		// 4. Set default icon
		PropertyResourceBundle properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");		
		String defaultIcon = properties.getString("defaultIcon");
		definition.setIcon(defaultIcon);
		
		// 5. ask repository to save the new process definition
		return new WfProcess(processRepository.save(definition));
	}

	/**
	 * Rertuns a list of workflow definition API models
	 * 
	 * @return
	 */
	public List<WfProcess> getProcessDefinitions() {
		List<WorkflowDefinition> definitions = processRepository.getAll();
		return WfProcess.fromWorkflowDefinitions(definitions);
	}

	/**
	 * Returns as list workflow definition API models by selected owners
	 * 
	 * @param ownerName
	 * @return as list workflow definition API models by owner
	 */

	public List<WfProcess> getDefinitionsByOwner(String owner) {
		List<WorkflowDefinition> definitions = processRepository.getDefinitionsByOwner(owner);
		return WfProcess.fromWorkflowDefinitions(definitions);
	}
	
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
	 * @return the saved process definition
	 * @throws InvalidRequestException
	 */
	@Transactional
	public WfProcess update(WfProcess process) throws InvalidRequestException {

		WorkflowDefinition definition;

		try {
			definition = processRepository.getById(process.getId());
		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process found with the given id");
		}

		// 1. apply some rules
		if (StringUtil.isEmpty(definition.getName())) {
			throw new InvalidRequestException("the name is required for the process definition");
		}

		try {
			Long nameCount = processRepository.getCheckName(definition);

			if (nameCount > 0) {
				process.setName(definition.getName() + " - "
						+ DateFormatUtils.format(Calendar.getInstance(), "d-M-yyyy HH.mm.ss"));
			}

		} catch (EmptyResultDataAccessException e) {
		}

		// 3. Update Process Definition Folder
		cmisFolder.updateFolderName(definition.getFolderId(), definition.getName());

		definition.updateFrom(process);
		
		if(process.getRegistryId() != null)
			definition.setRegistry(processRepository.getRegistryById(process.getRegistryId()));
		processRepository.save(definition);

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
		
		PropertyResourceBundle properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");		
		String defaultIcon = properties.getString("defaultIcon");
		
		String bpmn;
		
		try {
			
			bpmn = IOUtils.toString(inputStream);
			
		} catch (IOException e) {

			logger.error("Unable to read BPMN Input Stream. " + e.getMessage());
			throw new InvalidRequestException("Unable to read BPMN Input Stream.");
		}
		
		// parse the id of the process from the bpmn file
		String processId = parseProcessId(bpmn);
		
		// check whether another process with the same process id in its bpmn file exists
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
			deployment = activitiRepositorySrv.createDeployment().addString("input.bpmn20.xml", bpmn)
					.name(filename).deploy();
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
		
		if(processDef == null) {
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

		// 5. Get task information from the bpmn model and create task details entities
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		createTaskDetails(workflow);

		return new WfProcess(workflow);
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
	public WfProcessVersion createNewProcessVersion(int id, InputStream inputStream, String filename)
			throws InvalidRequestException {

		Deployment deployment;
		ProcessDefinition processDef;
		
		String bpmn;
		
		try {
			
			bpmn = IOUtils.toString(inputStream);
			
		} catch (IOException e) {

			logger.error("Unable to read BPMN Input Stream. " + e.getMessage());
			throw new InvalidRequestException("Unable to read BPMN Input Stream.");
		}
		
		// parse the id of the process from the bpmn file
		String processId = parseProcessId(bpmn);	
		
		// verify that the latest version has a bpmn file with the same process id
		if(!definitionVersionExistenceCheck(id, processId)){
			logger.error("Successive process versions should have the same key");
			throw new InvalidRequestException("Successive process versions should have the same key");
		}
		
		try {
			deployment = ActivitiHelper.createDeployment(activitiRepositorySrv, bpmn, filename);
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
		createTaskDetails(workflow);
		
		return new WfProcessVersion(definitionVersion);
	}

	/**
	 * 
	 * @param processId
	 * @param version
	 * @return
	 * @throws InvalidRequestException
	 */

	public WfProcessVersion updateVersion(int processId, WfProcessVersion version) throws InvalidRequestException {

		DefinitionVersion definitionVersion;
		try {
			definitionVersion = processRepository.getVersionById(version.getId());

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process version found with the given id");
		}

		definitionVersion.updateFrom(version);
		processRepository.saveVersion(processId, definitionVersion);

		return new WfProcessVersion(definitionVersion);
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
	 * Deletes all versions of the process. Throw exception if there are
	 * instances (active or old ones)
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

		cmisFolder.deleteFolderById(definition.getFolderId());
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
		// if the deleted version was the active one, set the active deployment
		// to most recent one
		if (definition.getActiveDeploymentId().equals(deploymentId)) {
			definition.setActiveDeploymentId(definition.getDefinitionVersions().get(0).getDeploymentId());
		}

		return new WfProcess(processRepository.save(definition));
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
	public WfProcess setActiveVersion(int processId, int versionId) throws InvalidRequestException {

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

		return new WfProcess(processRepository.save(definition));
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
	public WfProcessVersion deactivateVersion(int processId, int versionId) throws InvalidRequestException {

		WorkflowDefinition definition = processRepository.getById(processId);
		DefinitionVersion version = definition.getVersion(versionId);
		version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());

		if (definition.getActiveDeploymentId() != null && definition.getActiveDeploymentId().equals(version.getDeploymentId())) {
			definition.setActiveDeploymentId(null);
			processRepository.save(definition);
		}

		return new WfProcessVersion(processRepository.saveVersion(processId, version));
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

		List<WfFormProperty> formProperties = getWfFormProperties(startForm.getFormProperties());

		process.setProcessForm(formProperties);

		return process;
	}
	
	/**
	 * Return the full metadata set for the workflow definition
	 * 
	 * @param id
	 *            the id of the workflow definition
	 * @return
	 */
	public WfProcessMetadata getPublicProcessMetadata(String formId) throws InvalidRequestException {

		try {
		
			ExternalForm form = processRepository.getFormById(formId);
			
			WfProcessMetadata processMetadata = new WfProcessMetadata();
			
			processMetadata.setName(form.getWorkflowDefinition().getName());
			processMetadata.setIcon(form.getWorkflowDefinition().getIcon());
			processMetadata.setDescription(form.getWorkflowDefinition().getDescription());
			
			final char[] NUMBERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
					
			Captcha captcha = new Captcha.Builder(130, 40)
					.addText(new DefaultTextProducer(5, NUMBERS))
					.addNoise()
					.build();
			
			try {
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				
				ImageIO.write(captcha.getImage(), "png", out);
				out.flush();
				
				byte[] imageBytes = out.toByteArray();
				out.close();
				
				String image = "data:image/png;base64," +  Base64.encodeBase64String(imageBytes);
				
				processMetadata.setCaptchaImage(image);
				
				String hash = generateCaptchaHash(captcha.getAnswer());
				
				if (hash == null) {
					
					logger.error("Generating Captcha Hash failed.");
				}
				
				processMetadata.setCaptchaHash(hash);
				
			} catch (IOException e) {
				
				logger.error("Generating Captcha Image failed. " + e.getMessage());
			}
			
			StartFormData startForm = activitiFormSrv.getStartFormData(form.getWorkflowDefinition().getKey());

			List<WfFormProperty> formProperties = getWfFormProperties(startForm.getFormProperties());
			
			processMetadata.setProcessForm(formProperties);
			
			return processMetadata;
			
		} catch (EmptyResultDataAccessException e) {
			
			throw new InvalidRequestException("the process definition cannot be externally started");
		}
	}
	
	/**
	 * Start a new process instance with form data
	 * 
	 * @param processId
	 *            the id of the workflow definition
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws CustomException
	 */
	public WfProcessInstance startProcess(int processId, WfProcessInstance instanceData)
			throws InvalidRequestException {
		
		WorkflowDefinition definition = processRepository.getById(processId);
		
		AccessToken token = this.retrieveToken();
		
		return startProcess(definition, instanceData, token.getEmail());
	}
	
	/**
	 * Start a new process instance with form data
	 * 
	 * @param processKey
	 *            the id of the workflow definition
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws CustomException
	 */
	public WfProcessInstance startPublicProcess(String formId, WfProcessInstance instanceData)
			throws InvalidRequestException {
		
		if (!validCaptcha(instanceData.getCaptchaHash(), instanceData.getCaptchaAnswer())) {
			
			throw new InvalidRequestException("the request captcha is not valid");
		}
		
		try {
			ExternalForm form = processRepository.getFormById(formId);
		
			instanceData.setSupervisor(form.getSupervisor());
			
			Registry registry = form.getWorkflowDefinition().getRegistry();
			
			if (registry != null) {
				
				instanceData.setReference(TemplateHelper.getReference(registry));
				processRepository.update(registry);
			}
			
			instanceData.setTitle(TemplateHelper.getTitle(form, instanceData));
			
			return startProcess(form.getWorkflowDefinition(), instanceData, null);
			
		} catch (EmptyResultDataAccessException e) {
			
			throw new InvalidRequestException("the process definition cannot be externally started");
		}
	}

	/**
	 * Start a new process instance with form data
	 * 
	 * @param definition
	 *            the id of the workflow definition
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws CustomException
	 */
	@Transactional
	public WfProcessInstance startProcess(WorkflowDefinition definition, WfProcessInstance instanceData, String userId)
			throws InvalidRequestException {

		WorkflowInstance instance = new WorkflowInstance();
		DefinitionVersion activeVersion = definition.getActiveVersion();

		if (activeVersion == null) {

			throw new InvalidRequestException("the process definition version is not active");
		}
		
		if (instanceData.getTitle() == null || instanceData.getTitle().length() == 0) {
			throw new InvalidRequestException("the process title is not set");
		}

		// check if title is unique
		if (processRepository.getCheckInstanceName(instanceData.getTitle()) > 0)
			throw new InvalidRequestException("The instance title should be unique");

		ProcessInstance activitiInstance = null;

		try {
			
			if (instanceData.getProcessForm() != null) {

				for (WfFormProperty property : instanceData.getProcessForm()) {

					if (property.getType().equals("conversation")) {
					
						property.setValue(fixConversationMessage(property.getValue(), userId));
					}
				}
			}
			
			Registry registry = definition.getRegistry();
			
			if (registry != null && instanceData.getReference() == null) {
				
				instanceData.setReference(TemplateHelper.getReference(registry));
				processRepository.update(registry);
			}
			
			Map<String, String> variableValues = instanceData.getVariableValues();
			variableValues.put("instanceTitle",instanceData.getTitle());
			variableValues.put("instanceReference",instanceData.getReference());
			variableValues.put("instanceSupervisor",instanceData.getSupervisor());
			if (variableValues != null && !variableValues.isEmpty()) {
				activitiInstance = activitiFormSrv.submitStartFormData(definition.getKey(), variableValues);
			} else {
				activitiInstance = activitiRuntimeSrv.startProcessInstanceById(definition.getKey());
			}

			Folder processFolder = cmisFolder.getFolderById(definition.getFolderId());

			Folder folder = cmisFolder.createFolder(processFolder, instanceData.getTitle());

			instance.updateFrom(instanceData);

			instance.setReference(instanceData.getReference());
			instance.setId(activitiInstance.getProcessInstanceId());
			instance.setDefinitionVersion(activeVersion);
			instance.setFolderId(folder.getId());
			instance.setStartDate(new Date());
			instance.setStatus(WorkflowInstance.STATUS_RUNNING);

			return new WfProcessInstance(processRepository.save(instance));

		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}

	/**
	 * Start a new process instance with form data and files
	 * 
	 * @param processId
	 *            the id of the workflow definition
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws CustomException
	 */
	public WfProcessInstance startProcess(int processId, WfProcessInstance instanceData, MultipartFile[] files)
			throws InvalidRequestException {
		
		if (!validCaptcha(instanceData.getCaptchaHash(), instanceData.getCaptchaAnswer())) {
			
			throw new InvalidRequestException("the request captcha is not valid");
		}
		
		WorkflowDefinition definition = processRepository.getById(processId);
		
		AccessToken token = this.retrieveToken();
		
		return startProcess(definition, instanceData, token.getEmail(), token.getName(), files);
	}
	
	/**
	 * Start a new process instance with form data and files
	 * 
	 * @param processKey
	 *            the id of the workflow definition
	 * @param formData
	 *            the form data in key-value pairs
	 * @throws CustomException
	 */
	public WfProcessInstance startPublicProcess(String formId, WfProcessInstance instanceData, MultipartFile[] files)
			throws InvalidRequestException {
		
		ExternalForm form = processRepository.getFormById(formId);
		instanceData.setSupervisor(form.getSupervisor());
		
		Registry registry = form.getWorkflowDefinition().getRegistry();
		
		if (registry != null) {
			
			instanceData.setReference(TemplateHelper.getReference(registry));
			processRepository.update(registry);
		}
		
		instanceData.setTitle(TemplateHelper.getTitle(form, instanceData));
		
		return startProcess(form.getWorkflowDefinition(), instanceData, null, null, files);
	}
	
	/**
	 * Start a new process instance with form data and files
	 * 
	 * @param definition
	 *            is the id of the workflow definition
	 * @param instanceData
	 *            is the form data in key-value pairs
	 * @param files
	 *            is the uploaded files
	 */
	@Transactional
	public WfProcessInstance startProcess(WorkflowDefinition definition, WfProcessInstance instanceData,
			String userId, String user, MultipartFile[] files)
			throws InvalidRequestException {

		WorkflowInstance instance = new WorkflowInstance();
		DefinitionVersion activeVersion = definition.getActiveVersion();

		if (activeVersion == null) {

			throw new InvalidRequestException("the process definition version is not active");
		}
		
		if (instanceData.getTitle() == null || instanceData.getTitle().length() == 0) {
			throw new InvalidRequestException("the process title is not set");
		}

		// check if title is unique
		if (processRepository.getCheckInstanceName(instanceData.getTitle()) > 0)
			throw new InvalidRequestException("The instance title should be unique");
		
		Map<String, MultipartFile> filesMap = new HashMap<String, MultipartFile>();
		
		for (MultipartFile file : files) {
			
			filesMap.put(file.getOriginalFilename(), file);
		}
		
		Folder processFolder = cmisFolder.getFolderById(definition.getFolderId());

		Folder folder = cmisFolder.createFolder(processFolder, instanceData.getTitle());

		ProcessInstance activitiInstance = null;

		try {
			
			if (instanceData.getProcessForm() != null) {

				for (WfFormProperty property : instanceData.getProcessForm()) {

					if (property.getType().equals("document")) {

						ObjectMapper mapper = new ObjectMapper();
						WfDocument wfDocument;

						try {
							wfDocument = mapper.readValue(property.getValue(), WfDocument.class);
														
							MultipartFile file = filesMap.get(property.getId());
							
							Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(), file.getContentType());
							document.refresh();
							
							wfDocument.setDocumentId(document.getId());
							wfDocument.setVersion(document.getVersionLabel());
							
							Calendar now = Calendar.getInstance();
							
							DocumentType documentType = new DocumentType(wfDocument.getTitle(), wfDocument.getVersion(), wfDocument.getDocumentId(),
										user, userId, now.getTime(), wfDocument.getRefNo());

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
			
			Registry registry = definition.getRegistry();
			
			if (registry != null && instanceData.getReference() == null) {
				
				instanceData.setReference(TemplateHelper.getReference(registry));
				processRepository.update(registry);
			}
			
			Map<String, String> variableValues = instanceData.getVariableValues();
			variableValues.put("instanceTitle",instanceData.getTitle());
			variableValues.put("instanceReference",instance.getReference());
			variableValues.put("instanceSupervisor",instanceData.getSupervisor());
			if (variableValues != null && !variableValues.isEmpty()) {
				activitiInstance = activitiFormSrv.submitStartFormData(definition.getKey(), variableValues);
			} else {
				activitiInstance = activitiRuntimeSrv.startProcessInstanceById(definition.getKey());
			}
			
			instance.updateFrom(instanceData);
			
			instance.setId(activitiInstance.getProcessInstanceId());
			instance.setDefinitionVersion(activeVersion);
			instance.setFolderId(folder.getId());

			instance = processRepository.save(instance);

			return new WfProcessInstance(instance);

		} catch (ActivitiException e) {

			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}
	
	private boolean validCaptcha(String hash, String answer) {
		
		try {
			
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			byte[] recoveredBytes = cipher.doFinal(Base64.decodeBase64(hash));
			
			String decryptedHash = new String(recoveredBytes);
			
			return answer.equals(decryptedHash);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			
		} catch (BadPaddingException e) {
			
			e.printStackTrace();
		}
		
		return false;
	}
	
	private String generateCaptchaHash(String captcha) {
		
		try {
			
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			byte[] encrypted = cipher.doFinal(captcha.getBytes());
			
			return Base64.encodeBase64String(encrypted);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			
		} catch (IllegalBlockSizeException e) {
			
			e.printStackTrace();
		} catch (BadPaddingException e) {
			
			e.printStackTrace();
		}
						
		return null;
	}
	
	private String fixConversationMessage(String conversationValue, String userId) {
		
		Calendar now = Calendar.getInstance();
		
		ObjectMapper mapper = new ObjectMapper();
		ConversationType conversation;

		try {
			conversation = mapper.readValue(conversationValue, ConversationType.class);
			
			if (conversation.getComment() != null &&
					!conversation.getComment().isEmpty()) {
				
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

		// get email of Authenticated user
		AccessToken token = this.retrieveToken();
		String userId = (String) token.getEmail();

		// get intsances supervised by user
		return WfProcessInstance.fromWorkflowInstances(processRepository.getSupervisedProcesses(userId));
	}

	/**
	 * Return instances by process id
	 * 
	 * @param processes
	 * @return
	 */
	/*
	 * public WfProcessInstance getInstancesByProcess(int processId) {
	 * 
	 * return new
	 * WfProcessInstance(processRepository.getInstanceByProcessId(processId)); }
	 */
	
	/**
	 * Save a document
	 * 
	 * @param instanceId
	 *            is the process instance id the document will be added to
	 * @param document
	 *            is the document metadata
	 * @param inputStream
	 *            is the document file InputStream
	 * @param contentType
	 *            is the document file content type
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfDocument saveDocument(String instanceId, String variableName, WfDocument wfDocument, InputStream inputStream,
			String contentType) throws InvalidRequestException {

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
	 * @param document
	 *            is the document metadata
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfDocument updateDocument(String instanceId, String variableName, WfDocument wfDocument) throws InvalidRequestException {

		if (wfDocument.getDocumentId() == null) {

			throw new InvalidRequestException("the document ID is null.");
		}

		Document document = cmisDocument.updateDocumentById(wfDocument.getDocumentId(), wfDocument.getTitle());
		document.refresh();

		wfDocument.setDocumentId(document.getId());
		wfDocument.setVersion(document.getVersionLabel());
		
		updateDocumentType(instanceId, variableName, wfDocument);

		return wfDocument;
	}
	
	private void updateDocumentType(String instanceId, String variableName, WfDocument wfDocument) {
		
		Calendar now = Calendar.getInstance();

		AccessToken token = this.retrieveToken();

		String userId = (String) token.getEmail();
		String user = (String) token.getName();
		
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
		
		logger.info("Saving document " + wfDocument.getTitle() + " document.");

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
	
	public List<WfDocument> getProcessInstanceDocuments(int id) throws InvalidRequestException {
		
		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById("" + id);

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
			
			wfDocuments.add(wfDocument);
		}
		
		return wfDocuments;
	}
	
	public List<WfDocument> getProcessInstanceDocumentsByTask(int id) throws InvalidRequestException {
		
		Task task = activitiTaskSrv.createTaskQuery().taskId("" + id).singleResult();
		
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
			
			wfDocuments.add(wfDocument);
		}
		
		return wfDocuments;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<WfTask> getSupervisedTasks() {

		List<Task> tasks = new ArrayList<Task>();
		
		List<WfTask> returnList = new ArrayList<WfTask>();

		tasks = activitiTaskSrv.createTaskQuery().active().orderByDueDateNullsLast().asc().list();
		
		for(Task task : tasks){
			TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());
			if(taskPath.getInstance().getSupervisor().equals(retrieveToken().getEmail())){
				WfTask wfTask = new WfTask(task);
				wfTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));
				hydrateTask(wfTask);
				
				returnList.add(wfTask);
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
	public List<WfTask> getTasksByInstanceId(String instanceId) {
		List<WfTask> returnList = new ArrayList<WfTask>();
		List<HistoricTaskInstance> historicTasks = new ArrayList<HistoricTaskInstance>();

		historicTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().processInstanceId(instanceId).list();

		WorkflowInstance instance = new WorkflowInstance();

		// loop through completed tasks
		for (HistoricTaskInstance task : historicTasks) {
			instance = processRepository.getInstanceById(task.getProcessInstanceId());

			WfTask wfTask = new WfTask(task);
			wfTask.setProcessInstance(new WfProcessInstance(instance));
			wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
			
			returnList.add(wfTask);
		}

		return returnList;

	}
	
	/**
	 * Get the user's completed instances
	 * @return
	 */
	public List<WfProcessInstance> getUserCompletedInstances(){
		List<WfProcessInstance> returnList = new ArrayList<WfProcessInstance>();
		
		AccessToken token = this.retrieveToken();

		String assignee = (String) token.getEmail();
		
		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();
		
		completedUserTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().taskAssignee(assignee).orderByHistoricTaskInstanceEndTime().desc().list();
		
		for(HistoricTaskInstance completedUserTask : completedUserTasks){
			WorkflowInstance instance = processRepository.getInstanceById(completedUserTask.getProcessInstanceId());
			
			returnList.add(new WfProcessInstance(instance));
		}		
		return returnList;
	}
	
	/**
	 * Get the user's completed tasks
	 * @return
	 */
	public List<WfTask> getUserCompletedTasks(){
		List<WfTask> returnList = new ArrayList<WfTask>();
		
		AccessToken token = this.retrieveToken();

		String assignee = (String) token.getEmail();
		
		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();
		
		completedUserTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().taskAssignee(assignee).orderByHistoricTaskInstanceEndTime().desc().list();
		
		for(HistoricTaskInstance completedUserTask : completedUserTasks){
			WorkflowInstance instance = processRepository.getInstanceById(completedUserTask.getProcessInstanceId());
			DefinitionVersion definitionVersion = processRepository.getVersionByProcessDefinitionId(completedUserTask.getProcessDefinitionId());
			
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
	public List<WfTask> getCompletedTasksForUser(){
		
		List<WfTask> returnList = new ArrayList<WfTask>();
		List<HistoricTaskInstance> taskList = new ArrayList<HistoricTaskInstance>();
		
		AccessToken token = this.retrieveToken();
		String assignee = (String) token.getEmail();
		
		taskList = activitiHistorySrv.createHistoricTaskInstanceQuery().taskAssignee(assignee).orderByTaskCreateTime().asc().list();
		
		for(HistoricTaskInstance task : taskList){
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
			
			WfTask wfTask = new WfTask(task);
			wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
			wfTask.setDefinitionName(instance.getDefinitionVersion().getWorkflowDefinition().getName());
			wfTask.setProcessInstance(new WfProcessInstance(instance));
			
			returnList.add(wfTask);
		}
		
		return returnList;
	}
	
	
	public List<WfTask> getSearchedCompletedTasks(String definitionKey, String instanceTitle, long after, long before, String isSupervisor) {
		List<WfTask> returnList = new ArrayList<WfTask>();
		
		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);
		
		List<HistoricTaskInstance> taskList = new ArrayList<HistoricTaskInstance>();
		
		WorkflowInstance instance = new WorkflowInstance();
		
		if(isSupervisor.equals("false")){
			//Process defintion id == process definition key
			
			HistoricTaskInstanceQuery query = activitiHistorySrv.createHistoricTaskInstanceQuery();
	
			//Process defintion id == process definition key
			if( ! definitionKey.equals("null") && ! definitionKey.isEmpty() ) {
				query = query.processDefinitionId(definitionKey);
			}
			
			if ( dateAfter != null ) {
				query = query.taskCompletedAfter(dateAfter);				
			}
			
			if ( dateBefore != null ) {
				query = query.taskCompletedBefore(dateBefore);				
			}
			
			taskList = query.taskAssignee(retrieveToken().getEmail()).list();
			
			for(HistoricTaskInstance task : taskList){
				try{
					instance = processRepository.getInstanceById(task.getProcessInstanceId());
				}catch (EmptyResultDataAccessException e){
					instance = null;
				}
				if(instance != null){
					if(StringUtils.containsIgnoreCase(instance.getTitle(), instanceTitle)){
						
						WfTask wfTask = new WfTask(task);
						wfTask.setProcessInstance(new WfProcessInstance(instance));
						wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
						returnList.add(wfTask);
					}
				}
			}

		}else if(isSupervisor.equals("true")){
			
			HistoricTaskInstanceQuery query = activitiHistorySrv.createHistoricTaskInstanceQuery();
			
			//Process defintion id == process definition key
			if( ! definitionKey.equals("null") && ! definitionKey.isEmpty() ) {
				query = query.processDefinitionId(definitionKey);
			}
			
			if ( dateAfter != null ) {
				query = query.taskCompletedAfter(dateAfter);				
			}
			
			if ( dateBefore != null ) {
				query = query.taskCompletedBefore(dateBefore);				
			}
			
			taskList = query.list();
			
			for(HistoricTaskInstance task : taskList){
				try{
					instance = processRepository.getInstanceById(task.getProcessInstanceId());
				}catch (EmptyResultDataAccessException e){
					instance = null;
				}
				if(instance != null){
					if(StringUtils.containsIgnoreCase(instance.getTitle(), instanceTitle) && instance.getSupervisor().equals(retrieveToken().getEmail())){
						
						WfTask wfTask = new WfTask(task);
						wfTask.setProcessInstance(new WfProcessInstance(instance));
						wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
						returnList.add(wfTask);
					}
				}
			}

		}
		
		return returnList;
	}
	
	/**
	 * Get user's completed tasks by selected ids
	 * @return
	 */
	public List<WfTask> getUserCompledTasksByInstanceIds(List<String> instanceIds){
		List<WfTask> returnList = new ArrayList<WfTask>();
		
		AccessToken token = this.retrieveToken();
		String assignee = (String) token.getEmail();
		
		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();
		
		completedUserTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().processInstanceIdIn(instanceIds).
				taskAssignee(assignee).orderByHistoricTaskInstanceEndTime().desc().list();
		
		for(HistoricTaskInstance completedUserTask : completedUserTasks){
			DefinitionVersion definitionVersion = processRepository.getVersionByProcessDefinitionId(completedUserTask.getProcessDefinitionId());
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
	 * @return
	 */
	public List<WfTask> getCompletedTasksByInstances(List<String> instanceIds){
		List<WfTask> returnList = new ArrayList<WfTask>();
		
		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();
		
		completedUserTasks = activitiHistorySrv.createHistoricTaskInstanceQuery().processInstanceIdIn(instanceIds).orderByHistoricTaskInstanceEndTime().desc().list();
		
		for(HistoricTaskInstance completedUserTask : completedUserTasks){
			DefinitionVersion definitionVersion = processRepository.getVersionByProcessDefinitionId(completedUserTask.getProcessDefinitionId());
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

		HistoricTaskInstance task = activitiHistorySrv.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		
		if(task==null){
			throw new InvalidRequestException("There is no task with the given id");
		}
		
		WfTask wfTask = new WfTask(task);
		
		WorkflowInstance taskInstance = processRepository.getInstanceById(task.getProcessInstanceId());

		hydrateTask(wfTask);
		
		wfTask.setProcessInstance(new WfProcessInstance(taskInstance));

		List<UserTaskDetails> taskDetails = processRepository.getUserTaskDetailsByDefinitionKey(
				task.getTaskDefinitionKey(), taskInstance.getDefinitionVersion().getId());
		
		for(UserTaskDetails details : taskDetails){
			wfTask.setTaskDetails(new WfTaskDetails(details));
		}
		
		return wfTask;
	}
	
	/**
	 * Returns a completed task
	 * 
	 * @param taskId
	 * @return
	 */
	public WfTask getCompletedTask(String taskId){
		
		HistoricTaskInstance task = activitiHistorySrv.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		
		WorkflowInstance taskInstance = processRepository.getInstanceById(task.getProcessInstanceId());
		
		WfTask wfTask = new WfTask(task);
		
		wfTask.setProcessInstance(new WfProcessInstance(taskInstance));
		
		wfTask.setIcon(taskInstance.getDefinitionVersion().getWorkflowDefinition().getIcon());
		
		return wfTask;
	}
	
	/**
	 * Completes task
	 * 
	 * @param task
	 * @throws InvalidRequestException 
	 */
	public void completeTask(WfTask task) throws InvalidRequestException {
		
		AccessToken token = this.retrieveToken();

		String assignee = (String) token.getEmail();

		try {
			
			if (task.getTaskForm() != null) {
	
				for (WfFormProperty property : task.getTaskForm()) {
	
					if (property.getType().equals("conversation")) {
	
						property.setValue(fixConversationMessage(property.getValue(), assignee));
					}
				}
				
				Map<String, String> variableValues = task.getVariableValues();
	
				if (variableValues != null && !variableValues.isEmpty()) {
					
					activitiFormSrv.saveFormData(task.getId(), variableValues);
				}
				
				activitiTaskSrv.complete(task.getId());
				
			}
		} catch (ActivitiException e) {
	
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
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
		AccessToken token = this.retrieveToken();

		String userId = (String) token.getEmail();
		String user = (String) token.getName();
		
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
							
							Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(), file.getContentType());
							document.refresh();
							
							wfDocument.setDocumentId(document.getId());
							wfDocument.setVersion(document.getVersionLabel());
							
							Calendar now = Calendar.getInstance();
							
							DocumentType documentType = new DocumentType(wfDocument.getTitle(), wfDocument.getVersion(), wfDocument.getDocumentId(),
										user, userId, now.getTime(), wfDocument.getRefNo());

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

			if (variableValues != null && !variableValues.isEmpty()) {
				
				activitiFormSrv.saveFormData(task.getId(), variableValues);
			}
			
		} catch (ActivitiException e) {

			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}
	
	/**
	 * Temporary save task without document
	 * 
	 * @param task
	 * @throws InvalidRequestException
	 */
	public void tempTaskSave(WfTask task) throws InvalidRequestException {
		try{
			activitiFormSrv.saveFormData(task.getId(), task.getVariableValues());
			
		} catch (ActivitiException e) {

			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}

	/**
	 * Completes task
	 * 
	 * @param task
	 * @throws InvalidRequestException 
	 */
	public void completeTask(WfTask task, MultipartFile[] files) throws InvalidRequestException {

		AccessToken token = this.retrieveToken();

		String userId = (String) token.getEmail();
		String user = (String) token.getName();
		
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
							
							Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(), file.getContentType());
							document.refresh();
							
							wfDocument.setDocumentId(document.getId());
							wfDocument.setVersion(document.getVersionLabel());
							
							Calendar now = Calendar.getInstance();
							
							DocumentType documentType = new DocumentType(wfDocument.getTitle(), wfDocument.getVersion(), wfDocument.getDocumentId(),
										user, userId, now.getTime(), wfDocument.getRefNo());

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

			if (variableValues != null && !variableValues.isEmpty()) {
				
				activitiFormSrv.saveFormData(task.getId(), variableValues);
			}
			
			activitiTaskSrv.complete(task.getId());
			
		} catch (ActivitiException e) {

			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
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
			for(WfUser user : realmService.getAllUsers()){
				user.setPendingTasks(activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());
				if(!candidates.contains(user))
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
						//role only (term1)
						try {
							tempList = realmService.getUsersByRole(term1);
						} catch (Exception e) {
							logger.info("Error getting groups " + term1 + " " + e.getMessage());
							continue;
						}
						for (WfUser user : tempList) {
							user.setPendingTasks(activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());
							if(!candidates.contains(user))
								candidates.add(user);
						}
						logger.info("Getting candidates for Role: other " + term1);

					} else {
						//group only (term1)
						for (WfUser user : realmService.getUsersByGroup(term1)) {
							user.setPendingTasks(activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());
							if(!candidates.contains(user))
								candidates.add(user);
						}
						logger.info("Getting candidates for Group: " + term1);
					}
				} else if (term1.isEmpty() && !term2.isEmpty()) {
					//role only (term2)
					for (WfUser user : realmService.getUsersByRole(term2)) {
						user.setPendingTasks(activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());
						if(!candidates.contains(user))
							candidates.add(user);
					}
					logger.info("Getting candidates for Role: test " + term2);

				} else {
					//term1 = group, term2: role
					for (WfUser user : realmService.getUsersByGroupAndRole(term1, term2)) {
						user.setPendingTasks(activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());
						if(!candidates.contains(user))
							candidates.add(user);
					}
					logger.info("Getting candidates for User Group : " + term1 + " and Role: " + term2);
				}
			}
		}

		return candidates;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<WfUser> getAllCandidates(){
		List<WfUser> candidates = new ArrayList<WfUser>();
		
		for(WfUser user : realmService.getAllUsers()){
			user.setPendingTasks(activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());
			candidates.add(user);
		}
		
		return candidates;
	}

	/**
	 * Returns Assigned tasks for the user in context (Logged in user)
	 * 
	 * @return
	 */
	public List<WfTask> getTasksForUser() {
		
		List<WfTask> returnList = new ArrayList<WfTask>();

		List<Task> taskList = new ArrayList<Task>();

		AccessToken token = this.retrieveToken();

		String userId = (String) token.getEmail();

		// Getting tasks for user
		taskList = activitiTaskSrv.createTaskQuery().orderByTaskCreateTime().taskAssignee(userId).asc().list();
		

		for (Task task : taskList) {
			TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());
			WfTask wfTask = new WfTask(task);
			wfTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));
			
			returnList.add(hydrateTask(wfTask));
		}

		return returnList;
	}
	

	/**
	 * Set assignee to a task
	 * 
	 * @param taskId
	 * @param assigneeId
	 * @throws InvalidRequestException 
	 */
	@Transactional(rollbackFor = Exception.class)
	public void assignTask(WfTask wfTask, String assigneeId) throws InvalidRequestException {
		
		AccessToken token = this.retrieveToken();

		String userId = (String) token.getEmail();
		
		try {
			
			if (wfTask.getTaskForm() != null) {

				for (WfFormProperty property : wfTask.getTaskForm()) {
	
					if (property.getType().equals("conversation")) {
	
						property.setValue(fixConversationMessage(property.getValue(), userId));
					}
				}
			}
			
			Map<String, String> variableValues = wfTask.getVariableValues();

			if (variableValues != null && !variableValues.isEmpty()) {
				
				activitiFormSrv.saveFormData(wfTask.getId(), variableValues);
			}
			
			activitiTaskSrv.claim(wfTask.getId(), assigneeId);
			mailService.sendTaskAssignedMail(assigneeId, wfTask.getId(), wfTask.getName(), wfTask.getDueDate());
			
		} catch (ActivitiException e) {

			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}
	
	/**
	 * Set assignee to a task
	 * 
	 * @param wfTask
	 * @param assigneeId
	 * @param files
	 * @throws InvalidRequestException 
	 */
	@Transactional(rollbackFor = Exception.class)
	public void assignTask(WfTask wfTask, String assigneeId, MultipartFile[] files) throws InvalidRequestException {
		
		AccessToken token = this.retrieveToken();

		String userId = (String) token.getEmail();
		String user = (String) token.getName();
		
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
							
							Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(), file.getContentType());
							document.refresh();
							
							wfDocument.setDocumentId(document.getId());
							wfDocument.setVersion(document.getVersionLabel());
							
							Calendar now = Calendar.getInstance();
							
							DocumentType documentType = new DocumentType(wfDocument.getTitle(), wfDocument.getVersion(), wfDocument.getDocumentId(),
										user, userId, now.getTime(), wfDocument.getRefNo());

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

			if (variableValues != null && !variableValues.isEmpty()) {
				
				activitiFormSrv.saveFormData(wfTask.getId(), variableValues);
			}
			
			activitiTaskSrv.claim(wfTask.getId(), assigneeId);
			mailService.sendTaskAssignedMail(assigneeId, wfTask.getId(), wfTask.getName(), wfTask.getDueDate());
			
		} catch (ActivitiException e) {

			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}
	
	/**
	 * Unclaims a the user from a task
	 * @param taskId
	 * @throws InvalidRequestException
	 */
	public void unClaimTask(String taskId) throws InvalidRequestException{
		
		try{
			activitiTaskSrv.unclaim(taskId);
		}catch(Exception e){
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}
	
	/**
	 * Claims the logged in assignee to the task
	 * 
	 * @param taskId
	 * @throws InvalidRequestException
	 */
	public void claimTask(String taskId) throws InvalidRequestException {
		try {
			String assignee = retrieveToken().getEmail();
			activitiTaskSrv.claim(taskId, assignee);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}

	/**
	 * Returns a list of Wftasks to be claim by user according to user role
	 * 
	 * @return
	 */
	public List<WfTask> getCandidateUserTasks() {

		// list to store tasks according to user role/group
		List<Task> taskList = new ArrayList<Task>();

		// final list with tasks to be claimed by user
		List<WfTask> returnList = new ArrayList<WfTask>();

		AccessToken token = this.retrieveToken();

		Set<String> userRoles = token.getRealmAccess().getRoles();
		
		// get active tasks
		List<Task> tasks = activitiTaskSrv.createTaskQuery()
				.active()
				.taskUnassigned()
				.orderByTaskId().asc()
				.orderByProcessInstanceId().asc()
				.list();

		List<String> userGroups = realmService.getUserGroups();
		
		// filter tasks according to group and roles
		for (Task task : tasks) {
			DefinitionVersion definitionVersion = processRepository.getVersionByProcessDefinitionId(task.getProcessDefinitionId());
			
			List<String[]> taskGroupAndRoles = getCandidateGroupAndRole(task.getId());

			// exclude tasks marked as to be assigned by suppervisor
			if(definitionVersion.getWorkflowDefinition().isAssignBySupervisor()) continue;
			
			// if no restrictions apply to groups and roles add task to result
			if (taskGroupAndRoles.size() == 0) {
				taskList.add(task);
				continue;
			}
			
			
			// check every group-role designator for match with user gorups and roles
			boolean groupOk = true;
			boolean roleOk = true;
			
			for (String[] groupAndRole : taskGroupAndRoles) {			
				
				if(!groupAndRole[0].isEmpty() && !userGroups.contains(groupAndRole[0]))		groupOk = false;	
				
				if (!groupAndRole[1].isEmpty() && !userRoles.contains(groupAndRole[1]))		roleOk = false;
				
				if(!groupOk)	break;
				
				if(!roleOk)		break;								
			}
			
			if (groupOk && roleOk)
				taskList.add(task);
			
		}
	
		for(Task task : taskList) {
			
			TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());
			if(!taskPath.getTaskDetails().isAssign()){
				WfTask hydratedTask = hydrateTask(new WfTask(task));
				hydratedTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));
				returnList.add(hydratedTask);
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
	
	@Scheduled(cron="0 0 12 * * MON-FRI")
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
		
		List<Task> dueTasks = activitiTaskSrv.createTaskQuery()
				.active()
				.taskDueBefore(alertDate)
				.taskDueAfter(today)
				.list();
		
		for (Task task : dueTasks) {
			
			String recipient = task.getAssignee();
			boolean unAssigned = false;
			
			if (recipient == null) {
				
				WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
				recipient = instance.getSupervisor();
				unAssigned = true;
			}
			
			mailService.sendDueTaskMail(recipient, task.getId(),
					task.getName(), task.getDueDate(), unAssigned);
		}
		
		List<Task> expiredTasks = activitiTaskSrv.createTaskQuery()
				.active().taskDueBefore(today).list();
		
		for (Task task : expiredTasks) {
			
			String recipient = task.getAssignee();
			boolean unAssigned = false;
			
			if (recipient == null) {
				
				WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
				recipient = instance.getSupervisor();
				unAssigned = true;
			}
			
			mailService.sendTaskExpiredMail(recipient, task.getId(),
					task.getName(), task.getDueDate(), unAssigned);
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

	// TODO:vpap
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
		} 
		catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No task details entity was found with the given id");
		}

		taskDetails.updateFrom(wfTaskDetails);
		taskDetails = processRepository.save(taskDetails);

		return new WfTaskDetails(taskDetails);
	}

	// TODO:vpap
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
	
	
	//TODO:vpap
	/**
	 * Returns all active tasks
	 * 
	 * @return
	 */
	@Transactional
	public List<WfTask> getAllActiveTasks(){
		List<WfTask> wfTasks = new ArrayList<WfTask>();
		
		// get all active tasks
		List<Task> tasks = activitiTaskSrv.createTaskQuery().active().list();
		
		for(Task task : tasks){
			WfTask wfTask = new WfTask(task);
			WorkflowDefinition workflow = processRepository.getProcessByDefinitionId(task.getProcessDefinitionId());			
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
			
			wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
			wfTask.setDefinitionName(workflow.getName());
			wfTask.setProcessInstance(new WfProcessInstance(instance));
			
			wfTasks.add(wfTask);
		}
		
		return wfTasks;
	}
	
	//TODO:vpap
	/**
	 * Returns tasks of ended processes based on certain criteria.
	 * 
	 * @param title
	 * @param after
	 * @param before
	 * @param anonymous
	 * @return
	 */
	public List<WfTask> getEndedProcessInstancesTasks(String title, long after, long before, boolean anonymous){
		
		List<WfTask> wfTasks = new ArrayList<WfTask>();	
		
		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);

		if(title.isEmpty() || title.equals(" "))	title=null;
		String assignee = (anonymous) ? null : retrieveToken().getEmail();
		
		List<HistoricTaskInstance> historicTasks = activitiHistorySrv
				.createHistoricTaskInstanceQuery()
				.taskAssignee(assignee)
				.processFinished()
				.list();
		
		WorkflowInstance instance;
		
		for(HistoricTaskInstance hit : historicTasks){
			try{
				instance = processRepository.getInstanceById(hit.getProcessInstanceId());
			}
			catch (EmptyResultDataAccessException e) {
					instance=null;
			}
			if(instance!=null){
				if(instance.getStatus().equals(WorkflowInstance.STATUS_ENDED) 
						&& (title==null || instance.getTitle().indexOf(title) > -1) 
						&& instance.getEndDate().after(dateAfter)
						&& instance.getEndDate().before(dateBefore)
						&& (assignee==null || instance.getSupervisor().equals(assignee))
						){
					
					WfTask wfTask = new WfTask(hit);
					wfTask.setProcessInstance(new WfProcessInstance(instance));
					wfTasks.add(wfTask);
				}
			}
		}
		
		return wfTasks;
	}
		
	
	//TODO:vpap
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
		
		WfUser user = realmService.getUser(userId);
		if(user==null)	throw new InvalidRequestException("No user exists with id " + userId);
		
		String assignee = user.getEmail();
		
		WorkflowInstance instance = new WorkflowInstance();
		
		List<HistoricTaskInstance> historicTasks = activitiHistorySrv
				.createHistoricTaskInstanceQuery()
				.taskAssignee(assignee)
				.taskCreatedAfter(dateAfter)
				.taskCreatedBefore(dateBefore)
				.list();
		
		for(HistoricTaskInstance hit : historicTasks){
			WfTask wfTask = new WfTask(hit);
			instance = processRepository.getInstanceById(hit.getProcessInstanceId());
			hydrateTask(wfTask);
			if(!wfTasks.contains(wfTask))	wfTasks.add(wfTask);
		}		
				
		return wfTasks;
	}
	
	
	//TODO:vpap
	/**
	 * Apply current workflow settings
	 * 
	 * @param executionId
	 */
	@Transactional
	public void applyTaskSettings(Task task){
	
		WorkflowSettings settings = getSettings();				

		List<WfUser> users = this.getCandidatesByTaskId(task.getId());
		
		if(users==null || users.isEmpty()){
			
			PropertyResourceBundle properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");		
			String adminEmail = properties.getString("mail.admin");			
			WorkflowDefinition workflowDef = processRepository.getProcessByDefinitionId(task.getProcessDefinitionId());

			mailService.sendBpmnErrorEmail(adminEmail, workflowDef, task.getName());
			return;
		}
		
		if(!settings.isAutoAssignment() || users.size()>1)	return;
		
		String userEmail = users.get(0).getEmail();
		
		activitiTaskSrv.claim(task.getId(), userEmail);
		
		if(settings.isAssignmentNotification())	
			mailService.sendTaskAssignedMail(userEmail, task.getId(), task.getName(), task.getDueDate());
	}
	

	//TODO:vpap
	/**
	 * Return the settings
	 * 
	 * @return
	 */
	public WorkflowSettings getSettings(){
		
		WorkflowSettings settings = settingsStatus.getWorkflowSettings();
		
		if(settings == null){
			settings = processRepository.getSettings();
			settingsStatus.setWorkflowSettings(settings);
		}
		
		return settings;
	}
	
	
	//TODO:vpap
	/**
	 * Update settings
	 * 
	 * @param settings
	 * @return
	 */
	public WorkflowSettings updateSettings(WorkflowSettings settings){
		settingsStatus.setWorkflowSettings(settings);
		return processRepository.updateSettings(settings);		
	}
	
	
	//TODO:vpap
	/**
	 * Returns the external forms of a specified process 
	 * 
	 * @param processId
	 * @return
	 */
	public List<WfExternalForm> getProcessExternalForms(int processId){
		
		List<WfExternalForm> wfForms = new ArrayList<WfExternalForm>();
		List<ExternalForm> forms = new ArrayList<ExternalForm>();
		
		forms = processRepository.getProcessExternalForms(processId);
		
		wfForms = WfExternalForm.fromExternalForms(forms);
		
		return wfForms;
	}
	
	//TODO:vpap
	/**
	 * Return existing registries
	 * 
	 * @return
	 */
	public List<Registry> getRegistries(){
		return processRepository.getRegistries();
	}
	
	/**
	 * Update registry and create new registry
	 * 
	 * @param registryId
	 * @return
	 */
	public void updateRegistry(Registry registry) throws InvalidRequestException{
		
		processRepository.update(registry);
	}
	
	/**
	 * Create new registry
	 * 
	 * @param registryId
	 * @return
	 */
	public void createRegistry(Registry registry) throws InvalidRequestException{
		
		if(processRepository.checkIfRegistryExists(registry.getId()) > 0 )
			throw new InvalidRequestException("Registry with id " + registry.getId() + " exists");
		
		processRepository.update(registry);
	}
	
	/**
	 * Delete registry
	 * 
	 * @param registry
	 */
	public void deleteRegistry(String registryId) throws InvalidRequestException {
		if(processRepository.checkIfDefinitionHasRegistry(registryId) > 0 )
			throw new InvalidRequestException("Cannot delete registry because its referred to a definition");
		else
			processRepository.deleteRegistry(registryId);
	}
		
	
	//TODO:vpap
	/**
	 * Create an external form
	 * 
	 * @param wfXForm
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfExternalForm createExternalForm(WfExternalForm wfXForm) throws InvalidRequestException{
		
		WorkflowDefinition workflow;
		
		try{
			workflow = processRepository.getById(wfXForm.getWorkflowDefinitionId());
		}
		catch (EmptyResultDataAccessException e){
			throw new InvalidRequestException("There is no process with the specified id.");
		}
		
		Long count = processRepository.checkForExternalForm(wfXForm.getId());
		
		if(count>0){
			throw new InvalidRequestException("An external form with identical id exists.");
		}
		
		ExternalForm xform = new ExternalForm();
		xform.updateFrom(wfXForm, workflow);
		
		xform =  processRepository.saveExternalForm(xform);
		
		return (new WfExternalForm(xform));
	}
	
	/**
	 * Update external form
	 * 
	 * @param wfXForm
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfExternalForm updateExternalForm(WfExternalForm wfXForm) throws InvalidRequestException{
		
		WorkflowDefinition workflow;
		
		try{
			workflow = processRepository.getById(wfXForm.getWorkflowDefinitionId());
		}
		catch (EmptyResultDataAccessException e){
			throw new InvalidRequestException("There is no process with the specified id.");
		}
		
		ExternalForm xform = new ExternalForm();
		xform.updateFrom(wfXForm, workflow);
		
		xform =  processRepository.saveExternalForm(xform);
		
		return (new WfExternalForm(xform));
	}
	
	
	//TODO:vpap
	/**
	 * Delete an external form
	 * 
	 * @param id
	 * @throws InvalidRequestException 
	 */
	@Transactional
	public void deleteExternalForm(String id) throws InvalidRequestException{
		
		ExternalForm xform;
		
		try{
			xform = processRepository.getExternalForm(id);
		}
		catch (EmptyResultDataAccessException e){
			throw new InvalidRequestException("There is no external form with the specified id");
		}
		
		processRepository.deleteExternalForm(xform);
	}
	
	
	//TODO:vpap
	/**
	 * Suspend / Resume an external form
	 * 
	 * @param id
	 * @param enabled
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfExternalForm modifyExternalFormStatus(String id, boolean enabled) throws InvalidRequestException{
		
		ExternalForm xform;
		
		try{
			xform = processRepository.getExternalForm(id);
		}
		catch (EmptyResultDataAccessException e){
			throw new InvalidRequestException("There is no external form with the specified id");
		}
		
		xform.setEnabled(enabled);
		
		xform =  processRepository.saveExternalForm(xform);
		
		return new WfExternalForm(xform);
	}
	
	
	/**
	 * Private method for retrieving logged user token
	 * @return Logged-in user's token
	 */
	private AccessToken retrieveToken(){
		
		KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();

		return token;
	}

	
	/**
	 * Hydrate a wfTask with extra information
	 * 
	 * @param task
	 * @return
	 */
	
	private WfTask hydrateTask(WfTask wfTask) {

		List<WfFormProperty> formProperties = new ArrayList<WfFormProperty>();
		
		try {
			TaskFormData taskForm = activitiFormSrv.getTaskFormData(wfTask.getId());
			formProperties = getWfFormProperties(taskForm.getFormProperties());
		}
		
		// should work not tested kkoutros
		catch (ActivitiObjectNotFoundException e) {
			List<HistoricDetail> historicDetails = activitiHistorySrv.createHistoricDetailQuery().formProperties().taskId(wfTask.getId()).list();
			HistoricTaskInstance historicTaskInstance = activitiHistorySrv.createHistoricTaskInstanceQuery().taskId(wfTask.getId()).singleResult();
			
			List<org.activiti.bpmn.model.FormProperty> historicFormProperties = ActivitiHelper
					.getTaskFormDefinition(activitiRepositorySrv, wfTask.getProcessDefinitionId(), historicTaskInstance.getTaskDefinitionKey());
			
			Map<String,String> detailMap = new LinkedHashMap<String,String>();
			
		
		
			// fill the map using as key the detail
			 for(HistoricDetail detail : historicDetails) {
				 HistoricFormProperty historicFormProperty = (HistoricFormProperty) detail;
				 detailMap.put(historicFormProperty.getPropertyId(), historicFormProperty.getPropertyValue());
			 }
			 
			 for(org.activiti.bpmn.model.FormProperty formPropery : historicFormProperties){
				 
				 String propertyValue = detailMap.get(formPropery.getId());
				 
				 // prepare formValues
				 Map<String, String> values = new HashMap<String, String>();
				 
				 if (formPropery.getType().equals("enum")) {
					 
					 List<FormValue> formValues = formPropery.getFormValues();

					 for (int i = 0; formValues != null && i < formValues.size(); i++) {
						 values.put(formValues.get(i).getId(), formValues.get(i).getName());
					 }
				 }
				 
					WfFormProperty wfProperty = new WfFormProperty(
							formPropery.getId(), 
							formPropery.getName(), 
							formPropery.getType(),
							propertyValue, 
							formPropery.isReadable(), 
							formPropery.isWriteable(), 
							formPropery.isRequired(), 
							values
							);
					
					formProperties.add(wfProperty);
			 }
		}
		
		wfTask.setTaskForm(formProperties);

		DefinitionVersion definitionVersion = processRepository.getVersionByProcessDefinitionId(wfTask.getProcessDefinitionId());
		wfTask.initFromDefinitionVersion(definitionVersion);

		wfTask.setIcon(definitionVersion.getWorkflowDefinition().getIcon());
		
		wfTask.setDefinitionName(definitionVersion.getWorkflowDefinition().getName());
		return wfTask;
	}
	
	private List<WfFormProperty> getWfFormProperties(List<FormProperty> formProperties) {
		
		List<WfFormProperty> wfFormProperties = new ArrayList<WfFormProperty>();

		for (FormProperty property : formProperties) {

			WfFormProperty wfProperty = new WfFormProperty(property.getId(), property.getName(), property.getType().getName(),
					property.getValue(), property.isReadable(), property.isWritable(), property.isRequired(), (Map<String, String>)property.getType().getInformation("values"));
			
			wfFormProperties.add(wfProperty);
		}
		
		return wfFormProperties;
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
			WfTask hydratedTask = hydrateTask(new WfTask(task));
			
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
			hydratedTask.setProcessInstance(new WfProcessInstance(instance));
			wfTasks.add(hydratedTask);
		}
		return wfTasks;
	}
	
	//TODO:vpap
	/**
	 * private
	 * 
	 * Creates the task details when a new workflow or a new version is created
	 * 
	 * @param workflow
	 */
	private void createTaskDetails(WorkflowDefinition workflow){
		List<DefinitionVersion> definitionVersions = workflow.getDefinitionVersions();		
		int indexOfUpToDateVersion = definitionVersions.size() - 1;
		DefinitionVersion latestDefinitionVersion = definitionVersions.get(indexOfUpToDateVersion);
		DefinitionVersion previousDefinitionVersion = null;
		if(indexOfUpToDateVersion > 0) 	previousDefinitionVersion = definitionVersions.get(indexOfUpToDateVersion-1);
				
		BpmnModel bpmnModel = activitiRepositorySrv.getBpmnModel(latestDefinitionVersion.getProcessDefinitionId());

		List<org.activiti.bpmn.model.Process> processes = bpmnModel.getProcesses();
		for (org.activiti.bpmn.model.Process p : processes) {

			List<UserTask> userTasks = p.findFlowElementsOfType(UserTask.class);
			
			if (userTasks != null && !userTasks.isEmpty()) {
				
				for (UserTask userTask : userTasks) {
					
					UserTaskDetails userTaskDetails = new UserTaskDetails();
					userTaskDetails.setName(userTask.getName());
					userTaskDetails.setTaskId(userTask.getId());
					
					String description = (previousDefinitionVersion==null) ? "" : 
						copyDescriptionFromSimilarTask(previousDefinitionVersion.getId(), userTask.getName());
					
					userTaskDetails.setDescription(description);
					
					userTaskDetails.setAssign(workflow.isAssignBySupervisor());
					
					userTaskDetails.setDefinitionVersion(latestDefinitionVersion);

					processRepository.save(userTaskDetails);
				}
			}
		}
	}
	
	//TODO:vpap
	/**
	 * private
	 * 
	 * Checks for user tasks with the same name between the currently deployed and the latest 
	 * version, so that it is possible to copy the descriptions
	 * 
	 * @param id
	 * @param taskName
	 * @return
	 */
	private String copyDescriptionFromSimilarTask(int id, String taskName){		
		List<UserTaskDetails> taskDetails = processRepository.getVersionTaskDetails(id);
		for(UserTaskDetails task : taskDetails){
			if(task.getName().equals(taskName)){
				if(task.getDescription()!=null && !task.getDescription().isEmpty()){
					return task.getDescription();
				}
			}
		}
		return "";
	}
	
	/**
	 * private
	 * 
	 * Checks if a process already exists with the same key
	 * 
	 * @param key
	 * @return
	 */
	private boolean definitionExistenceCheck(String key){
		List<ProcessDefinition> processDefinitions = 
				activitiRepositorySrv.createProcessDefinitionQuery().processDefinitionKey(key).list();
		if(processDefinitions==null || processDefinitions.isEmpty())	return false;
		return true;
	}

	//TODO:vpap
	/**
	 * private
	 * 
	 * Checks whether the new version to be deployed has the same key with 
	 * the latest (current) version.
	 * 
	 * @param workflowId
	 * @param key
	 * @return
	 */
	private boolean definitionVersionExistenceCheck(int workflowId, String key){
		List<DefinitionVersion> definitionVersions = processRepository.getVersionsByProcessId(workflowId);
		if(definitionVersions==null || definitionVersions.isEmpty())	return false;
		int lastVersionIndex = definitionVersions.size() - 1;
		DefinitionVersion lastVersion = definitionVersions.get(lastVersionIndex);		
		if(lastVersion.getProcessDefinitionId().indexOf(key+":") == -1)		return false;
		return true;
	}
	
	//TODO:vpap
	/**
	 * private
	 * 
	 * Parses the bpmn file to retrieve the id of the process.
	 * 
	 * @param inputStream
	 * @return
	 * @throws InvalidRequestException 
	 */
	private String parseProcessId(String bpmn) throws InvalidRequestException{
		
		String processId = null;
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		
		try {
			
			XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(bpmn));
			streamReader.nextTag(); // definitions tag
			streamReader.nextTag(); // process tag
			
			if (streamReader.isStartElement()) {
				
				if (streamReader.getLocalName().equals("process")) {
					
					processId = streamReader.getAttributeValue(null, "id");
				}
            }
			
		} catch (XMLStreamException e) {

			logger.error("BPMN XML Stream Exception. " + e.getMessage());
		}
        
        return processId;
	}
	
	
}
