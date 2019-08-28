package gr.cyberstream.workflow.engine.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cyberstream.workflow.engine.cmis.CMISDocument;
import gr.cyberstream.workflow.engine.cmis.CMISFolder;
import gr.cyberstream.workflow.engine.config.SettingsStatus;
import gr.cyberstream.workflow.engine.customtypes.ApproveFormType;
import gr.cyberstream.workflow.engine.customtypes.ConversationType;
import gr.cyberstream.workflow.engine.customtypes.DocumentType;
import gr.cyberstream.workflow.engine.customtypes.MessageType;
import gr.cyberstream.workflow.engine.listeners.CustomTaskFormFields;
import gr.cyberstream.workflow.engine.model.*;
import gr.cyberstream.workflow.engine.model.api.*;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.util.string.StringUtil;
import nl.captcha.Captcha;
import nl.captcha.text.producer.DefaultTextProducer;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricFormPropertyEntity;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
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
import org.apache.commons.codec.binary.Base64;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.ws.rs.core.UriBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implements all the business rules related to process definitions and process
 * instances
 * 
 * @author nlyke
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
	private DefinitionService definitionService;

	@Autowired
	private FormService activitiFormSrv;

	@Autowired
	private TaskService activitiTaskSrv;

	@Autowired
	private RuntimeService activitiRuntimeSrv;
	
	@Autowired
	private ManagementService activitiManagementService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private MailService mailService;

	@Autowired
	private SettingsStatus settingsStatus;
	
	@Autowired
	private TwitterConnectionFactory twitterConnectionFactory;

	private static byte[] keyBytes = { 48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1 };

	private static Key key = null;

	// user roles
	private static final String ROLE_ADMIN = "ROLE_Admin";
	private static final String ROLE_PROCESS_ADMIN = "ROLE_ProcessAdmin";
	private static final String ROLE_SUPERVISOR = "ROLE_Supervisor";
	
	private OAuthToken requestToken;

	public ProcessService() {
		key = new SecretKeySpec(keyBytes, "AES");
	}

	/**
	 * Returns a a process by its id
	 * 
	 * @param processId
	 *            Process id
	 * 
	 * @return {@link WfProcess}
	 */
	@Deprecated
	public WfProcess getProcessById(int processId) {
		WorkflowDefinition workflowDefinition = processRepository.getById(processId);
		
		return new WfProcess(workflowDefinition);
	}

	/**
	 * Returns a list of all WfProcess depending on user
	 * 
	 * @return List of {@link WfProcess}
	 */
	@Deprecated
	public List<WfProcess> getAll() {
		List<WfProcess> returnList = new ArrayList<WfProcess>();
		List<WorkflowDefinition> workflowDefinitions = processRepository.getAll();

		if (hasRole(ROLE_ADMIN)) {
			returnList = WfProcess.fromWorkflowDefinitions(workflowDefinitions);
		} else {
			for (WorkflowDefinition workflowDefinition : workflowDefinitions) {
				if (hasGroup(workflowDefinition.getOwner())) {
					WfProcess wfProcess = new WfProcess(workflowDefinition);
					returnList.add(wfProcess);
				}
			}
		}
		return returnList;
	}

	/**
	 * Returns a list of active WfProcess depending on user
	 * 
	 * @return List of WfProcess
	 */
	@Deprecated
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
	 * 
	 * @param id
	 * @return
	 */
	@Deprecated
	public List<WfTaskDetails> getVersionTaskDetails(int id) {
		List<UserTaskDetails> taskDetails = processRepository.getVersionTaskDetails(id);
		
		return WfTaskDetails.fromUserTaskDetails(taskDetails);
	}

	/**
	 * Return all running instances for a process with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public List<WfProcessInstance> getActiveProcessInstances(int id) {
		List<WorkflowInstance> workflowInstances = processRepository.getActiveProcessInstances(id);
		List<WfProcessInstance> wfProcessInstances = new ArrayList<WfProcessInstance>();

		if (workflowInstances == null || workflowInstances.isEmpty())
			return null;

		for (WorkflowInstance instance : workflowInstances) {
			wfProcessInstances.add(new WfProcessInstance(instance));
		}

		return wfProcessInstances;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance setInstanceVersion(String instanceId, int version) throws InvalidRequestException {
		
		logger.warn("Setting instance " + instanceId + " to version " + version);
		
		WorkflowInstance instance;
		
		try {
			
			instance = processRepository.getInstanceById(instanceId);
			
		} catch (EmptyResultDataAccessException e) {
			
			throw new InvalidRequestException("processInstanceNotFound");
		}
		
		try {
			
			activitiManagementService.executeCommand(new SetProcessDefinitionVersionCmd(instanceId, version));
						
			DefinitionVersion definitionVersion = 
					processRepository.getDefinitionVersion(instance.getDefinitionVersion().getWorkflowDefinition(), version);
		
			instance.setDefinitionVersion(definitionVersion);
			processRepository.save(instance);
			
		} catch (ActivitiException e) {
			
			logger.error("Unable to set instance " + instanceId + " to version " + version + ". " + e.getMessage());
		}
		
		return new WfProcessInstance(instance);
	}

	/**
	 * Delete process instance
	 * 
	 * @param instanceId
	 *            Instance's id to be deleted
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void cancelProcessInstance(String instanceId) throws InvalidRequestException {

		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(instanceId);

		} catch (NoResultException | EmptyResultDataAccessException e) {
			throw new InvalidRequestException("processInstanceNotFound");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(instance.getDefinitionVersion().getWorkflowDefinition().getOwner())) {

			// delete it from activiti
			activitiRuntimeSrv.deleteProcessInstance(instanceId, null);

			// update instance's status to deleted
			instance.setStatus(WorkflowInstance.STATUS_DELETED);

			// save the instance after status changed to deleted
			processRepository.save(instance);

		} else
			throw new InvalidRequestException("notAuthorizedToCancel");
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
			throw new InvalidRequestException("processInstanceNotFound");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(instance.getDefinitionVersion().getWorkflowDefinition().getOwner())) {
			// delete it from activiti if instance is not marked as deleted
			// which means it is already deleted from activiti
			if (!instance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
				activitiRuntimeSrv.deleteProcessInstance(instanceId, null);

			// delete instance from WorkflowInstance table
			processRepository.cancelProcessInstance(instance);

		} else
			throw new InvalidRequestException("notAuthorizedToDelete");
	}

	/**
	 * Suspend process instance
	 * 
	 * @param instanceId
	 * @return
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance suspendProcessInstance(String instanceId) throws InvalidRequestException {
		WorkflowInstance workflowInstance;

		try {
			workflowInstance = processRepository.getInstanceById(instanceId);
			
		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("processInstanceNotFound");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(workflowInstance.getDefinitionVersion().getWorkflowDefinition().getOwner())) {
			try {
				activitiRuntimeSrv.suspendProcessInstanceById(instanceId);
				
			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException("suspendingFailed");
			}
			
			workflowInstance.setStatus(WorkflowInstance.STATUS_SUSPENDED);
			workflowInstance = processRepository.save(workflowInstance);
			
			return new WfProcessInstance(workflowInstance);
			
		} else
			throw new InvalidRequestException("notAuthorizedToSuspend");
	}

	/**
	 * Resume a suspended process instance
	 * 
	 * @param instanceId
	 * @return
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance resumeProcessInstance(String instanceId) throws InvalidRequestException {
		WorkflowInstance workflowInstance;
		
		try {
			workflowInstance = processRepository.getInstanceById(instanceId);
			
		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("processInstanceNotFound");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(workflowInstance.getDefinitionVersion().getWorkflowDefinition().getOwner())) {
			try {
				activitiRuntimeSrv.activateProcessInstanceById(instanceId);
				
			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException("resumingFailed");
			} 

			workflowInstance.setStatus(WorkflowInstance.STATUS_RUNNING);
			workflowInstance = processRepository.save(workflowInstance);
			
			return new WfProcessInstance(workflowInstance);
			
		} else
			throw new InvalidRequestException("notAuthorizedToResume");
	}

	/**
	 * Creates a new process definition from just its metadata. No BPMN
	 * definition is attached yet.
	 * 
	 * @param process
	 *            the metadata of the process
	 *            
	 * @return the saved process definition
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public WfProcess createNewProcessDefinition(WfProcess process) throws InvalidRequestException {
		WorkflowDefinition definition = new WorkflowDefinition();
		definition.updateFrom(process);
		
		// check user roles/groups
		if(hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner())) {
			// 1. apply some rules
			if (StringUtil.isEmpty(definition.getName()))
				throw new InvalidRequestException("nameRequired");

			try {
				WorkflowDefinition sameNameWorkflowDefinition = processRepository.getByName(definition.getName());

				if (sameNameWorkflowDefinition != null)
					process.setName(definition.getName() + " - " + DateFormatUtils.format(Calendar.getInstance(), "d-M-yyyy HH.mm.ss"));

			} catch (EmptyResultDataAccessException e) {
				
			}

			// 2. Initialize process definition
			definition.setActiveDeploymentId(null);

			// 3. Create Process Definition Folder
			Folder folder = cmisFolder.createFolder(null, definition.getName());
			definition.setFolderId(folder.getId());

			// 4. Set default icon
			String defaultIcon = environment.getProperty("defaultIcon");
			definition.setIcon(defaultIcon);

			// 5. ask repository to save the new process definition
			return new WfProcess(processRepository.save(definition));
			
		} else
			throw new InvalidRequestException("notAuthorizedToCreateDefinition");
	}

	/**
	 * Rertuns a list of workflow definition API models
	 * 
	 * @return
	 */
	@Deprecated
	public List<WfProcess> getProcessDefinitions() {
		List<WorkflowDefinition> definitions = processRepository.getAll();
		
		return WfProcess.fromWorkflowDefinitions(definitions);
	}

	/**
	 * Returns as list workflow definition API models by selected owners
	 * 
	 * @param owner
	 * @return as list workflow definition API models by owner
	 */
	@Deprecated
	public List<WfProcess> getDefinitionsByOwner(String owner) {
		List<WorkflowDefinition> definitions = processRepository.getDefinitionsByOwner(owner);
		
		return WfProcess.fromWorkflowDefinitions(definitions);
	}
	
	@Deprecated
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
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public WfProcess update(WfProcess process) throws InvalidRequestException {
		WorkflowDefinition definition;

		try {
			definition = processRepository.getById(process.getId());
			
		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("noProcessWithId");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner()) || definition.getOwner() == null) {
			
			// 1. apply some rules
			if (StringUtil.isEmpty(definition.getName()))
				throw new InvalidRequestException("nameRequired");

			try {
				Long nameCount = processRepository.getCheckName(definition);

				if (nameCount > 0)
					process.setName(definition.getName() + " - " + DateFormatUtils.format(Calendar.getInstance(), "d-M-yyyy HH.mm.ss"));

			} catch (EmptyResultDataAccessException e) {
			}

			// 3. Update Process Definition Folder
			cmisFolder.updateFolderName(definition.getFolderId(), definition.getName());

			definition.updateFrom(process);

			if (process.getRegistryId() != null)
				definition.setRegistry(processRepository.getRegistryById(process.getRegistryId()));
			else
				definition.setRegistry(null);

			processRepository.save(definition);

		} else 
			throw new InvalidRequestException("notAuthorizedToUpdateDefinition");

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
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public WfProcess createNewProcessDefinition(InputStream inputStream, String filename) throws InvalidRequestException {
		Deployment deployment;

		String defaultIcon = environment.getProperty("defaultIcon");
		String bpmn;

		try {
			bpmn = IOUtils.toString(inputStream);

		} catch (IOException e) {
			throw new InvalidRequestException("unableToReadBPMN");
		}

		// parse the id of the process from the bpmn file
		String processId = parseProcessId(bpmn);

		// check whether another process with the same process id in its bpmn
		// file exists
		if (processId == null) {
			throw new InvalidRequestException("processKeyNull");
		} else if (definitionExistenceCheck(processId)) {
			throw new InvalidRequestException("processWithKeyAlreadyExists");
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
			throw new InvalidRequestException("BPMNInputNotValid");
		}

		// 2. Check deployment and get metadata from the deployed process
		// definition ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if (deployment == null) {
			logger.error("BPMN file error");
			throw new InvalidRequestException("BPMNInputNotValid");
		}

		logger.info("New BPMN deployment: " + deployment.getName());

		ProcessDefinition processDef = ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositorySrv,
				deployment.getId());

		if (processDef == null) {
			logger.error("BPMN file error");
			throw new InvalidRequestException("BPMNInputNotValid");
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
	 * @param id
	 * @param inputStream
	 * @param filename
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public WfProcessVersion createNewProcessVersion(int id, InputStream inputStream, String filename) throws InvalidRequestException {
		Deployment deployment;
		ProcessDefinition processDef;
		String bpmn;
		WorkflowDefinition workflow;

		try {
			workflow = processRepository.getById(id);
			
		} catch(Exception e) {
			logger.error("Process with id " + id + " not found." + e.getMessage());
			throw new InvalidRequestException("noProcessDefinitionWithID");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(workflow.getOwner())) {
			try {
				bpmn = IOUtils.toString(inputStream);

			} catch (IOException e) {
				logger.error("Unable to read BPMN Input Stream. " + e.getMessage());
				throw new InvalidRequestException("unableToReadBPMN");
			}

			// parse the id of the process from the bpmn file
			String processId = parseProcessId(bpmn);

			// verify that the latest version has a bpmn file with the same
			// process id
			if (!definitionVersionExistenceCheck(id, processId)) {
				logger.error("Successive process versions should have the same key");
				throw new InvalidRequestException("successiveVersionsSameKey");
			}

			try {
				// get the deployment
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

			DefinitionVersion definitionVersion = new DefinitionVersion();
			definitionVersion.setDeploymentId(deployment.getId());
			definitionVersion.setStatus(WorkflowDefinitionStatus.NEW.toString());
			definitionVersion.setVersion(processDef.getVersion());
			definitionVersion.setDeploymentdate(deployment.getDeploymentTime());
			definitionVersion.setProcessDefinitionId(ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositorySrv, deployment.getId()).getId());

			workflow.addDefinitionVersion(definitionVersion);

			processRepository.save(workflow);
			createTaskDetails(workflow);

			return new WfProcessVersion(definitionVersion);

		} else
			throw new InvalidRequestException("definitionNotInYourGroup");
	}

	/**
	 * 
	 * @param processId
	 * @param version
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public WfProcessVersion updateVersion(int processId, WfProcessVersion version) throws InvalidRequestException {
		DefinitionVersion definitionVersion;
		
		try {
			definitionVersion = processRepository.getVersionById(version.getId());

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("noProcessVersionWithId");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(definitionVersion.getWorkflowDefinition().getOwner())) {
			definitionVersion.updateFrom(version);
			processRepository.saveVersion(processId, definitionVersion);

			return new WfProcessVersion(definitionVersion);

		} else
			throw new InvalidRequestException("definitionNotInYourGroup");
	}

	/**
	 * Create an image with the diagram of the process definition
	 * 
	 * @param processId
	 *            the id of process
	 * @return
	 */
	@Deprecated
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
		InputStream resource = processDiagramGenerator.generateDiagram(bpmnModel, "jpeg", activitiRuntimeSrv.getActiveActivityIds(instanceId));
		
		return new InputStreamResource(resource);
	}
	
	/**
	 * Generates a diagram based on given task.
	 * Shows the task's position in diagram
	 * 
	 * @param definitionId
	 * @param taskDefinition
	 * @return
	 */
	public InputStreamResource getTaskProcessDiagram(int definitionId, String taskDefinition) {
		WorkflowDefinition definition = processRepository.getById(definitionId);
		
		ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
		BpmnModel bpmnModel = activitiRepositorySrv.getBpmnModel(definition.getKey());
		List<String> taskId = new ArrayList<>();
		
		// the task's definition id
		taskId.add(taskDefinition);
		
		InputStream resource = processDiagramGenerator.generateDiagram(bpmnModel, "jpeg", taskId);
		return new InputStreamResource(resource);
	}

	/**
	 * Deletes all versions of the process. Throw exception if there are
	 * instances (active or old ones)
	 * 
	 * @param processId
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public void deleteProcessDefinition(int processId) throws InvalidRequestException {
		WorkflowDefinition definition;
		
		// get workflow definition
		try {
			definition = processRepository.getById(processId);
			
		} catch (Exception e) {
			throw new InvalidRequestException("noProcessDefinitionWithID");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner())) {

			// check if any of the process deployments have instances
			boolean found = false;
			
			for (DefinitionVersion version : definition.getDefinitionVersions()) {
				
				if (activitiHistorySrv.createHistoricProcessInstanceQuery().notDeleted().deploymentId(version.getDeploymentId()).count() > 0)
					found = true;
			}
			
			// throw an exception since there are assosiated instances with that definition
			if (found)
				throw new InvalidRequestException("definitionNotDeletedAssociatedEntries");

			// delete all process definitions (all versions)
			String activeDeploymentId = definition.getActiveDeploymentId();

			boolean activeDeleted = false;
			for (DefinitionVersion version : definition.getDefinitionVersions()) {
				
				if (version.getDeploymentId().isEmpty())
					continue;

				activitiRepositorySrv.deleteDeployment(version.getDeploymentId());
				
				if (version.getDeploymentId().equals(activeDeploymentId))
					activeDeleted = true;
			}

			// delete active deployment if not already deleted
			if (!activeDeleted && activeDeploymentId != null && !activeDeploymentId.isEmpty()) {
				activitiRepositorySrv.deleteDeployment(activeDeploymentId);
			}

			// delete workflow definition entry
			processRepository.delete(processId);

			cmisFolder.deleteFolderById(definition.getFolderId());
			
		} else
			throw new InvalidRequestException("notAuthorizedToDeleteDefinition");
	}

	/**
	 * Deletes the specific version of the process definition
	 * 
	 * @param processId
	 * @param deploymentId
	 * @return
	 * @throws InvalidRequestException If any instance found
	 */
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public WfProcess deleteProcessDefinitionVersion(int processId, String deploymentId) throws InvalidRequestException {
		WorkflowDefinition definition;
		
		// get workflow definition
		try {
			definition = processRepository.getById(processId);
			
		} catch (Exception e) {
			throw new InvalidRequestException("noProcessDefinitionWithID");
		}

		// no need to check anything
		if (hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner())) {
			
			// check if the version id the last one
			if (definition.getDefinitionVersions().size() < 2)
				throw new InvalidRequestException("errorDeleteLastVersion");
			
			// check the existence of the deploymentId
			boolean found = false;
			boolean used = false;
			List<DefinitionVersion> versions = definition.getDefinitionVersions();

			for (DefinitionVersion version : versions) {
				
				if (!version.getDeploymentId().equals(deploymentId)) {
					continue;
				}

				found = true;

				// check if the version is ever used
				if (activitiHistorySrv.createHistoricProcessInstanceQuery().notDeleted().deploymentId(deploymentId).count() > 0) {
					used = true;
					break;
				}
				
				// remove version
				versions.remove(version);
				break;
			}
			// definition version not found
			if (!found)
				throw new InvalidRequestException("processDefinitionNotInProcess");

			// definition with the specific version is used
			if (used)
				throw new InvalidRequestException("definitionNotDeletedAssociatedEntries");

			// delete the deployment
			activitiRepositorySrv.deleteDeployment(deploymentId);

			// remove the version for the process definition
			definition.setDefinitionVersions(versions);

			// update the process definition
			// if the deleted version was the active one, set the active
			// deployment
			// to most recent one
			if (definition.getActiveDeploymentId().equals(deploymentId))
				definition.setActiveDeploymentId(definition.getDefinitionVersions().get(0).getDeploymentId());

			return new WfProcess(processRepository.save(definition));

		} else
			throw new InvalidRequestException("definitionNotInYourGroup");
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
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public WfProcess setActiveVersion(int processId, int versionId) throws InvalidRequestException {

		WorkflowDefinition definition;
		
		try {
			definition = processRepository.getById(processId);
			
		} catch(Exception e) {
			throw new InvalidRequestException("noProcessDefinitionWithID");
		}

		// nothing to check
		if (hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner())) {
			boolean found = false;
			
			for (DefinitionVersion version : definition.getDefinitionVersions()) {
				if (version.getId() == versionId) {
					
					version.setStatus(WorkflowDefinitionStatus.ACTIVE.toString());
					definition.setActiveDeploymentId(version.getDeploymentId());
					definition.setKey(ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositorySrv, version.getDeploymentId()).getId());
					found = true;
					
				} else {
					if (version.getStatus().equals(WorkflowDefinitionStatus.ACTIVE.toString())) {
						version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());
					}
				}
			}

			if (!found) 
				throw new InvalidRequestException("processDefinitionNotInProcess");

			return new WfProcess(processRepository.save(definition));

		} else
			throw new InvalidRequestException("definitionNotInYourGroup");
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
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public WfProcessVersion deactivateVersion(int processId, int versionId) throws InvalidRequestException {

		WorkflowDefinition definition;
		
		try {
			definition = processRepository.getById(processId);
			
		} catch (Exception e) {
			throw new InvalidRequestException("noProcessDefinitionWithID");
		}
		
		DefinitionVersion version = definition.getVersion(versionId);

		// nothing to check
		if (hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner())) {
			version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());

			if (definition.getActiveDeploymentId() != null && definition.getActiveDeploymentId().equals(version.getDeploymentId())) {
				definition.setActiveDeploymentId(null);
				processRepository.save(definition);
			}

			return new WfProcessVersion(processRepository.saveVersion(processId, version));

		} else
			throw new InvalidRequestException("definitionNotInYourGroup");
	}

	/**
	 * Return the full metadata set for the workflow definition
	 * 
	 * @param id
	 *            the id of the workflow definition
	 * @param device
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	public WfProcess getProcessMetadata(int id, String device) throws InvalidRequestException {

		WorkflowDefinition definition = processRepository.getById(id);
		WfProcess process = new WfProcess(definition);

		StartFormData startForm = activitiFormSrv.getStartFormData(definition.getKey());

		BpmnModel bpmnModel = activitiRepositorySrv.getBpmnModel(process.getProcessDefinitionId());
		List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
		Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
		UserTaskDetails taskDetail = new UserTaskDetails();

		List<org.activiti.bpmn.model.Process> processes = bpmnModel.getProcesses();

		for (org.activiti.bpmn.model.Process p : processes) {
			List<StartEvent> startEvents = p.findFlowElementsOfType(StartEvent.class);
			for (StartEvent startEvent : startEvents) {

				process.setStartFormDocumentation(startEvent.getDocumentation());
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

		List<WfFormProperty> formProperties = new ArrayList<WfFormProperty>();

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
	 * Return the full metadata set for the workflow definition
	 * 
	 * @param formId
	 *            the id of the workflow definition
	 * @param device
	 * @return
	 */
	public WfProcessMetadata getPublicProcessMetadata(String formId, String device) throws InvalidRequestException {

		try {

			ExternalForm form = processRepository.getFormById(formId);

			// get the bpmn model in order to get the start form
			BpmnModel bpmnModel = activitiRepositorySrv.getBpmnModel(form.getWorkflowDefinition().getKey());
			List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
			Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
			UserTaskDetails taskDetail = new UserTaskDetails();

			WfProcessMetadata processMetadata = new WfProcessMetadata();

			processMetadata.setName(form.getWorkflowDefinition().getName());
			processMetadata.setIcon(form.getWorkflowDefinition().getIcon());
			processMetadata.setDescription(form.getWorkflowDefinition().getDescription());

			final char[] NUMBERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

			Captcha captcha = new Captcha.Builder(130, 40).addText(new DefaultTextProducer(5, NUMBERS)).addNoise()
					.build();

			try {

				ByteArrayOutputStream out = new ByteArrayOutputStream();

				ImageIO.write(captcha.getImage(), "png", out);
				out.flush();

				byte[] imageBytes = out.toByteArray();
				out.close();

				String image = "data:image/png;base64," + Base64.encodeBase64String(imageBytes);

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

			List<org.activiti.bpmn.model.Process> processes = bpmnModel.getProcesses();

			for (org.activiti.bpmn.model.Process p : processes) {
				List<StartEvent> startEvents = p.findFlowElementsOfType(StartEvent.class);
				for (StartEvent startEvent : startEvents) {

					// task detail
					taskDetail = processRepository.getUserTaskDetailByDefinitionKey(startEvent.getId(),
							form.getWorkflowDefinition().getKey());

					// get the task form elements
					taskFormElements = processRepository.getUserTaskFromElements(form.getWorkflowDefinition().getKey(),
							taskDetail.getId());

					// fill the usertaskform element map using as key the
					// element id and as value the user taskform element
					for (UserTaskFormElement userTaskFormElement : taskFormElements) {
						mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
					}

				}
			}

			List<WfFormProperty> formProperties = new ArrayList<WfFormProperty>();

			// loop through form properties
			for (WfFormProperty formProperty : getWfFormProperties(startForm.getFormProperties())) {
				UserTaskFormElement userTaskFormElement = mappedUserTaskFormElements.get(formProperty.getId());

				if (userTaskFormElement.getDevice().equalsIgnoreCase(device)
						|| userTaskFormElement.getDevice().equals(UserTaskFormElement.ALL_DEVICES)) {
					formProperty.setDescription(userTaskFormElement.getDescription());
					formProperties.add(formProperty);
				}
			}

			processMetadata.setProcessForm(formProperties);

			return processMetadata;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException("The process definition cannot be externally started");
		}
	}

	/**
	 * Start a new process instance with form data (used by workspace client, no
	 * files are present)
	 * 
	 * @param processId
	 *            The id of the workflow definition
	 * 
	 * @param instanceData
	 *            The instance's data to start the instance
	 * 
	 * @return The started {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance startProcess(int processId, WfProcessInstance instanceData) throws InvalidRequestException, InternalException {
		AccessToken token = getAccessToken();
		WorkflowDefinition definition;
		
		try {
			 definition = processRepository.getById(processId);
			 
		} catch(EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("noProcessFoundToStartInstance");
		}
		
		if (hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner()))
			return startProcess(definition, instanceData, token.getEmail());
		else
			throw new InvalidRequestException("notAuthorizedToStart");
	}
	
	/**
	 * Start a new process instance with form data (used by workspace client,
	 * files are present)
	 * 
	 * @param processId
	 *            The id of the workflow definition
	 * 
	 * @param instanceData
	 *            The instance's data to start the instance
	 * 
	 * @param files
	 *            The files to start the instance
	 * 
	 * @return The started {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance startProcess(int processId, WfProcessInstance instanceData, MultipartFile[] files) throws InvalidRequestException, InternalException {
		AccessToken token = getAccessToken();
		
		WorkflowDefinition workflowDefinition;
		
		try {
			workflowDefinition = processRepository.getById(processId);
			 
		} catch(EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("noProcessFoundToStartInstance");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(workflowDefinition.getOwner()))
			return startProcess(workflowDefinition, instanceData, token.getEmail(), token.getName(), files);
		else
			throw new InvalidRequestException("notAuthorizedToStart");
	}
	
	/**
	 * Starts a new instance using an external form (used by external client, no
	 * files are present)
	 * 
	 * @param formId
	 *            The form's id which the instance will be started
	 * 
	 * @param instanceData
	 *            The instance's data to start the instance
	 * 
	 * @return The started {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance startPublicProcess(String formId, WfProcessInstance instanceData) throws InvalidRequestException, InternalException {
		
		ExternalForm externalForm;
		
		// check captcha first
		if (!validCaptcha(instanceData.getCaptchaHash(), instanceData.getCaptchaAnswer()))
			throw new InvalidRequestException("The request captcha is not valid");
		
		// get the external form
		try {
			externalForm = processRepository.getFormById(formId);
			
		} catch(EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("Could not find external form to start the process");
		}
		
		// set supervisor
		instanceData.setSupervisor(externalForm.getSupervisor());
		
		Registry registry = externalForm.getWorkflowDefinition().getRegistry();

		if (registry != null && instanceData.getReference() == null) {
			instanceData.setReference(TemplateHelper.getReference(registry));
			processRepository.saveRegistry(registry);
		}
		
		// set title based on template
		instanceData.setTitle(TemplateHelper.getTitle(externalForm, instanceData));

		try {
			return startProcess(externalForm.getWorkflowDefinition(), instanceData, null);

		} catch (Exception e) {
			throw new InvalidRequestException("Could not start process. " + e.getMessage());
		}
	}
	
	/**
	 * Starts a new instance using an external form (used by external client,
	 * files are present)
	 * 
	 * @param formId
	 *            The form's id which the instance will be started
	 * 
	 * @param instanceData
	 *            The instance's data to start the instance
	 * 
	 * @param files
	 *            The files to start the instance
	 * 
	 * @return The started {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance startPublicProcess(String formId, WfProcessInstance instanceData, MultipartFile[] files) throws InvalidRequestException, InternalException {
		
		ExternalForm externalForm;

		// check captcha
		if (!validCaptcha(instanceData.getCaptchaHash(), instanceData.getCaptchaAnswer()))
			throw new InvalidRequestException("the request captcha is not valid");

		// get the external form
		try {
			externalForm = processRepository.getFormById(formId);

		} catch (EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("Could not find external form to start the process");
		}
		
		instanceData.setSupervisor(externalForm.getSupervisor());
		
		Registry registry = externalForm.getWorkflowDefinition().getRegistry();

		if (registry != null && instanceData.getReference() == null) {
			instanceData.setReference(TemplateHelper.getReference(registry));
			processRepository.saveRegistry(registry);
		}

		// set title based on template
		instanceData.setTitle(TemplateHelper.getTitle(externalForm, instanceData));
		
		try {
			return startProcess(externalForm.getWorkflowDefinition(), instanceData, null, null, files);

		} catch (Exception e) {
			throw new InvalidRequestException("Could not start process. " + e.getMessage());
		}
	}
	
	/**
	 * Starts a new instance using an external form (used by mobile client, no
	 * files are present)
	 * 
	 * @param formId
	 *            The form's id which the instance will be started
	 * 
	 * @param instanceData
	 *            The instance's data to start the instance
	 * 
	 * @return The started {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance startPublicMobileProcess(String formId, WfProcessInstance instanceData) throws InvalidRequestException, InternalException {
		
		ExternalForm externalForm;

		// get the external form
		try {
			externalForm = processRepository.getFormById(formId);

		} catch (EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("Could not find external form to start the process");
		}
		
		instanceData.setSupervisor(externalForm.getSupervisor());
		
		Registry registry = externalForm.getWorkflowDefinition().getRegistry();

		if (registry != null && instanceData.getReference() == null) {
			instanceData.setReference(TemplateHelper.getReference(registry));
			processRepository.saveRegistry(registry);
		}

		instanceData.setTitle(TemplateHelper.getTitle(externalForm, instanceData));

		try {
			return startProcess(externalForm.getWorkflowDefinition(), instanceData, null);

		} catch (Exception e) {
			throw new InvalidRequestException("Could not start process. " + e.getMessage());
		}
	}
	
	/**
	 *  Starts a new instance using an external form (used by mobile client,
	 *   files are present)
	 * 
	 * @param formId The form's id which the instance will be started
	 * 
	 * @param instanceData The instance's data to start the instance
	 * 
	 * @param files The files to start the instance
	 * 
	 * @return The started {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance startPublicMobileProcess(String formId, WfProcessInstance instanceData, MultipartFile[] files) throws InvalidRequestException, InternalException {

		ExternalForm externalForm;

		// get the external form
		try {
			externalForm = processRepository.getFormById(formId);

		} catch (EmptyResultDataAccessException | NoResultException noRes) {
			throw new InvalidRequestException("Could not find external form to start the process");
		}
		
		instanceData.setSupervisor(externalForm.getSupervisor());
		
		Registry registry = externalForm.getWorkflowDefinition().getRegistry();

		if (registry != null && instanceData.getReference() == null) {
			instanceData.setReference(TemplateHelper.getReference(registry));
			processRepository.saveRegistry(registry);
		}

		instanceData.setTitle(TemplateHelper.getTitle(externalForm, instanceData));

		try {
			return startProcess(externalForm.getWorkflowDefinition(), instanceData, null, null, files);

		} catch (Exception e) {
			throw new InvalidRequestException("Could not start process. " + e.getMessage());
		}
	}
	
	/**
	 * All the other start process functions, set the instance with some info
	 * such as supervisor, title based on template, registry, checking for captcha etc, and then all
	 * of them are resulting to this one function or the other one with
	 * MultipartFile.<br>
	 * 
	 * This function actually starts the all instances where no files are present.
	 * 
	 * @param definition The workflow definition which the instance will be started
	 * @param instanceData
	 * @param userId
	 * @return
	 * @throws InternalException
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public WfProcessInstance startProcess(WorkflowDefinition definition, WfProcessInstance instanceData, String userId) throws InternalException, InvalidRequestException {
		
		DefinitionVersion activeVersion = definition.getActiveVersion();
		
		if (activeVersion == null)
			throw new InvalidRequestException("processDefinitionVersionNotActive");

		if (instanceData.getTitle() == null || instanceData.getTitle().length() == 0)
			throw new InvalidRequestException("processTitleNotSet");
		
		// check if title is unique
		if (processRepository.getCheckInstanceName(instanceData.getTitle()) > 0)
			throw new InvalidRequestException("instanceTitleUnique");

		try {
			
			if (instanceData.getProcessForm() != null) {

				for (WfFormProperty property : instanceData.getProcessForm()) {
					if (property.getType().equals("conversation")) 
						property.setValue(fixConversationMessage(property.getValue(), userId));
					property.setWritable(true);
				}
			}

			Registry registry = definition.getRegistry();

			if (registry != null && instanceData.getReference() == null) {
				instanceData.setReference(TemplateHelper.getReference(registry));
				processRepository.saveRegistry(registry);
			}
			
			String instanceId = "";
			
			Map<String, String> variableValues = instanceData.getVariableValues();
			Folder processFolder = cmisFolder.getFolderById(definition.getFolderId());
			Folder folder = cmisFolder.createInstanceFolder(processFolder, instanceData.getTitle());
			
			if (variableValues != null) {
				variableValues.put("instanceTitle", instanceData.getTitle());
				variableValues.put("instanceReference", instanceData.getReference());
				variableValues.put("instanceSupervisor", instanceData.getSupervisor());
				variableValues.put("folderId", folder.getId());
				variableValues.put("definitionVersionId", "" + activeVersion.getId());
				variableValues.put("device", instanceData.getClient());
				variableValues.put("instanceStartDate", new Date().toString());
				
				instanceId = activitiFormSrv.submitStartFormData(definition.getKey(), variableValues).getId();
			} else
				instanceId = activitiRuntimeSrv.startProcessInstanceById(definition.getKey()).getId();
			
			instanceData.setId(instanceId);
			instanceData.setStartDate(new Date());
			instanceData.setDefinitionIcon(definition.getIcon());
			return instanceData;
			
		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			
			if(e.getCause() instanceof ServiceException) {
				ServiceException serviceException = (ServiceException) e.getCause();
				throw new InternalException(serviceException.getCode() + ". " + serviceException.getMessage());
			}
			else
				throw new InvalidRequestException(e.getMessage());
			
		} catch (CmisStorageException e) {
			logger.error(e.getMessage());
			throw new InternalException(e.getMessage());
			
		} catch (RuntimeException runTimeEx) {
			throw new InternalException(runTimeEx.getMessage());
		} 
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
	 * @throws InternalException 
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfProcessInstance startProcess(WorkflowDefinition definition, WfProcessInstance instanceData, String userId, String user, MultipartFile[] files)
			throws InvalidRequestException, InternalException {

		DefinitionVersion activeVersion = definition.getActiveVersion();

		if (activeVersion == null)
			throw new InvalidRequestException("processDefinitionVersionNotActive");

		if (instanceData.getTitle() == null || instanceData.getTitle().length() == 0)
			throw new InvalidRequestException("processTitleNotSet");

		// check if title is unique
		if (processRepository.getCheckInstanceName(instanceData.getTitle()) > 0)
			throw new InvalidRequestException("instanceTitleUnique");

		Map<String, MultipartFile> filesMap = new HashMap<String, MultipartFile>();

		for (MultipartFile file : files) {
			filesMap.put(file.getOriginalFilename(), file);
		}

		Folder processFolder = cmisFolder.getFolderById(definition.getFolderId());
		Folder folder = cmisFolder.createInstanceFolder(processFolder, instanceData.getTitle());

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

					} else if (property.getType().equals("conversation"))
						property.setValue(fixConversationMessage(property.getValue(), userId));
				}
			}

			Registry registry = definition.getRegistry();

			if (registry != null && instanceData.getReference() == null) {
				instanceData.setReference(TemplateHelper.getReference(registry));
				processRepository.saveRegistry(registry);
			}
			
			String instanceId = "";
			
			Map<String, String> variableValues = instanceData.getVariableValues();
			
			if (variableValues != null) {
				
				variableValues.put("instanceTitle", instanceData.getTitle());
				variableValues.put("instanceReference", instanceData.getReference());
				variableValues.put("instanceSupervisor", instanceData.getSupervisor());
				variableValues.put("folderId", folder.getId());
				variableValues.put("definitionVersionId", "" + activeVersion.getId());
				variableValues.put("device", instanceData.getClient());
				
				instanceId = activitiFormSrv.submitStartFormData(definition.getKey(), variableValues).getId();
			} else
				instanceId = activitiRuntimeSrv.startProcessInstanceById(definition.getKey()).getId();

			instanceData.setId(instanceId);
			instanceData.setStartDate(new Date());
			instanceData.setDefinitionIcon(definition.getIcon());
			return instanceData;
			
		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			
			if(e.getCause() instanceof ServiceException) {
				ServiceException serviceException = (ServiceException) e.getCause();
				throw new InternalException(serviceException.getCode() + ". " + serviceException.getMessage());
			}
			else
				throw new InvalidRequestException(e.getMessage());
			
		} catch (CmisStorageException e) {
			logger.error(e.getMessage());
			throw new InternalException(e.getMessage());
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
		return WfProcessInstance.fromWorkflowInstances(processRepository.getSupervisedProcesses(getAccessToken().getEmail()));
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
	 *            
	 * @param wfDocument
	 *            is the document metadata
	 *            
	 * @param inputStream
	 *            is the document file InputStream
	 *            
	 * @param contentType
	 *            is the document file content type
	 *            
	 * @return
	 * @throws InvalidRequestException
	 */
	
	public WfDocument saveDocument(String instanceId, String variableName, WfDocument wfDocument, InputStream inputStream, String contentType) throws InvalidRequestException {

		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(instanceId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("processInstanceIDNotValid");
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

		if (wfDocument.getDocumentId() == null) {

			throw new InvalidRequestException("documentIDNull");
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

		AccessToken token = this.getAccessToken();

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

				throw new InvalidRequestException("duplicateDocumentTitle");
			}
		}

		return document;
	}

	public List<WfDocument> getProcessInstanceDocuments(int id) throws InvalidRequestException {

		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById("" + id);

		} catch (EmptyResultDataAccessException e) {

			throw new InvalidRequestException("processInstanceIDNotValid");
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

			throw new InvalidRequestException("processInstanceIDNotValid");
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
	 * Returns instance's documents by id
	 * 
	 * @param instanceId
	 * @return
	 * @throws InvalidRequestException
	 */
	public List<WfDocument> getDocumentsByInstance(String instanceId) throws InvalidRequestException {

		WorkflowInstance instance;

		try {
			instance = processRepository.getInstanceById(instanceId);

		} catch (EmptyResultDataAccessException e) {

			throw new InvalidRequestException("processInstanceIDNotValid");
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
	 * @return
	 * @throws InvalidRequestException 
	 */
	@Deprecated
	public List<WfTask> getSupervisedTasks() throws InvalidRequestException {
		
		List<Task> tasks = new ArrayList<Task>();
		List<WfTask> returnList = new ArrayList<WfTask>();

		if (hasRole(ROLE_ADMIN)) {
			tasks = activitiTaskSrv.createTaskQuery().active().orderByDueDateNullsLast().asc().list();

			for (Task task : tasks) {
				TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());
				WfTask wfTask = new WfTask(task);
				wfTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));
				wfTask.setStartForm(taskPath.getDefinition().hasStartForm());
				hydrateTask(wfTask);
				returnList.add(wfTask);
			}
		} else {
			tasks = activitiTaskSrv.createTaskQuery().active().orderByDueDateNullsLast().asc().list();
			for (Task task : tasks) {
				TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());
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
	 * @throws InvalidRequestException
	 */
	@Deprecated
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
	@Deprecated
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
	 * On new service function name -> searchCompletedTasks
	 * @param definitionKey
	 * @param instanceTitle
	 * @param after
	 * @param before
	 * @param isSupervisor
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
	public List<WfTask> getSearchedCompletedTasks(String definitionKey, String instanceTitle, long after, long before, String isSupervisor) throws InvalidRequestException {
		
		List<WfTask> returnList = new ArrayList<WfTask>();
		
		Date dateAfter = null;
		Date dateBefore = null;
		
		if(after != 0)
			dateAfter = new Date(after);
		
		if(before != 0)
			dateBefore = new Date(before);
		
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
							for (WorkflowInstance processInstance : processRepository.getInstancesByDefinitionVersionId(version.getId())) {
								processInstanceIds.add(processInstance.getId());
							}
							
						} catch (EmptyResultDataAccessException e) {
							throw new InvalidRequestException("noInstanceFoundForProcess");
						}

					}
					
					if (processInstanceIds.size() > 0)
						query.processInstanceIdIn(processInstanceIds);

				} else
					query.processDefinitionId(definitionKey);
			}

			if (dateAfter != null) {
				query = query.taskCompletedAfter(dateAfter);
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
							for (WorkflowInstance processInstance : processRepository.getInstancesByDefinitionVersionId(version.getId())) {
								processInstanceIds.add(processInstance.getId());
							}
						} catch (EmptyResultDataAccessException e) {
							throw new InvalidRequestException("noInstanceFoundForProcess");
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
				if (hasRole(ROLE_ADMIN) || hasRole(ROLE_SUPERVISOR)) {

					if (instance != null) {
						if (StringUtils.containsIgnoreCase(instance.getTitle(), instanceTitle)) {

							WfTask wfTask = new WfTask(task);
							wfTask.setProcessInstance(new WfProcessInstance(instance));
							wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
							returnList.add(wfTask);
						}
					}
					// user is not admin or process admin
				} else if (hasRole(ROLE_PROCESS_ADMIN)){
					if (instance != null && hasGroup(instance.getDefinitionVersion().getWorkflowDefinition().getOwner())) {
						if (StringUtils.containsIgnoreCase(instance.getTitle(), instanceTitle)) {

							WfTask wfTask = new WfTask(task);
							wfTask.setProcessInstance(new WfProcessInstance(instance));
							wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
							returnList.add(wfTask);
						}
					}
				} else {
					if (instance != null) {
						if (StringUtils.containsIgnoreCase(instance.getTitle(), instanceTitle) && instance.getSupervisor().equals(getAccessToken().getEmail())) {

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
	@Deprecated
	public List<WfTask> getUserCompledTasksByInstanceIds(List<String> instanceIds) {
		List<WfTask> returnList = new ArrayList<WfTask>();

		AccessToken token = this.getAccessToken();
		String assignee = (String) token.getEmail();

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
	@Deprecated
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
			wfTask.setDefinitionName(wfTask.getProcessInstance().getDefinitionName());

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
	 * Returns a {@link WfTask} by id
	 * 
	 * @param taskId
	 *            The task's id
	 * 
	 * @return {@link WfTask}
	 * @throws InvalidRequestException
	 */
	public WfTask getTask(String taskId) throws InvalidRequestException {
		HistoricTaskInstance task;

		try {
			task = activitiHistorySrv.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();

		} catch (ActivitiException noRes) {
			throw new InvalidRequestException("noTaskWithID");
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
	public List<WfFormProperty> getTaskFormPropertiesByTaskDefintionKey(String taskDefinitionKey, String processDefinitionId) throws InvalidRequestException {

		List<WfFormProperty> returnList = new ArrayList<WfFormProperty>();
		List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
		Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
		UserTaskDetails taskDetails = new UserTaskDetails();
		List<org.activiti.bpmn.model.FormProperty> formProperties = new ArrayList<org.activiti.bpmn.model.FormProperty>();

		//
		try {
			formProperties = ActivitiHelper.getTaskFormDefinition(activitiRepositorySrv, processDefinitionId, taskDefinitionKey);
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
				if (!mappedUserTaskFormElements.isEmpty())
					userTaskFormElement = mappedUserTaskFormElements.get(formPropery.getId());

				if (formPropery.getType().equals("enum")) {
					List<FormValue> formValues = formPropery.getFormValues();

					for (int i = 0; formValues != null && i < formValues.size(); i++) {
						values.put(formValues.get(i).getId(), formValues.get(i).getName());
					}

				} else if (formPropery.getType().equals("date")) {
					dateFormat = formPropery.getDatePattern();
				}

				WfFormProperty wfFormProperty = new WfFormProperty();
				
				wfFormProperty.setId(formPropery.getId());
				wfFormProperty.setName(formPropery.getName());
				wfFormProperty.setType(formPropery.getType());
				wfFormProperty.setValue("");
				wfFormProperty.setReadable(formPropery.isReadable());
				wfFormProperty.setWritable(formPropery.isWriteable());
				wfFormProperty.setRequired(formPropery.isRequired());
				wfFormProperty.setFormValues(values);
				wfFormProperty.setFormat(dateFormat);
				wfFormProperty.setDescription(userTaskFormElement.getDescription());
				wfFormProperty.setDevice(userTaskFormElement.getDevice());

				returnList.add(wfFormProperty);
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
				if (!mappedUserTaskFormElements.isEmpty())
					userTaskFormElement = mappedUserTaskFormElements.get(formPropery.getId());

				if (formPropery.getType().equals("enum")) {
					List<FormValue> formValues = formPropery.getFormValues();

					for (int i = 0; formValues != null && i < formValues.size(); i++) {
						values.put(formValues.get(i).getId(), formValues.get(i).getName());
					}

				} else if (formPropery.getType().equals("date")) {
					dateFormat = formPropery.getDatePattern();
				}

				WfFormProperty wfFormProperty = new WfFormProperty();
				
				wfFormProperty.setId(formPropery.getId());
				wfFormProperty.setName(formPropery.getName());
				wfFormProperty.setType(formPropery.getType());
				wfFormProperty.setValue("");
				wfFormProperty.setReadable(formPropery.isReadable());
				wfFormProperty.setWritable(formPropery.isWriteable());
				wfFormProperty.setRequired(formPropery.isRequired());
				wfFormProperty.setFormValues(values);
				wfFormProperty.setFormat(dateFormat);
				wfFormProperty.setDescription(userTaskFormElement.getDescription());
				wfFormProperty.setDevice(userTaskFormElement.getDevice());
				
				returnList.add(wfFormProperty);
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
	 * Completes task
	 * 
	 * @param task
	 * @throws InvalidRequestException
	 */
	@Deprecated
	public void completeTask(WfTask task) throws InvalidRequestException {
		String assignee = getAccessToken().getEmail();

		// check if task has the same assignee as the person requests to
		// complete it or if that person has role admin
		if (task.getAssignee().equals(assignee) || hasRole(ROLE_ADMIN)) {
			try {
				
				// check if task's instance exists before doing anything else
				WorkflowInstance tasksInstance = processRepository.getInstanceById(task.getProcessInstance().getId());
				
				if(tasksInstance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
					throw new InvalidRequestException("taskInstanceSuspendedContactAdmin");
					
				if(tasksInstance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
					throw new InvalidRequestException("taskInstanceDeletedContactAdmin");

				if (task.getTaskForm() != null) {

					for (WfFormProperty property : task.getTaskForm()) {

						// add a new "conversation" message
						if (property.getType().equals("conversation"))
							property.setValue(fixConversationMessage(property.getValue(), assignee));
					}

					// get task's variable values in order to save them
					Map<String, String> variableValues = task.getVariableValues();

					if (variableValues != null && !variableValues.isEmpty())
						activitiFormSrv.saveFormData(task.getId(), variableValues);

					// finally complete the task
					activitiTaskSrv.complete(task.getId());
				}
			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				throw new InvalidRequestException(e.getMessage());
				
			} catch(NoResultException | EmptyResultDataAccessException noResult ) { 
				throw new InvalidRequestException("taskInstanceNotExistsContactAdmin");
			}
			// task's assignee not matched with the person who requests to
			// complete it or not admin
		} else {
			throw new InvalidRequestException("notAuthorizedToCompleteTask");
		}

	}

	/**
	 * Temporary save the task's data
	 * 
	 * @param task
	 * @param files
	 * @throws InvalidRequestException
	 */
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public void tempTaskSave(WfTask task, MultipartFile[] files) throws InvalidRequestException {
		
		String userId =  getAccessToken().getEmail();
		String user = getAccessToken().getName();
		WorkflowInstance workflowInstance;

		Map<String, MultipartFile> filesMap = new HashMap<String, MultipartFile>();
		
		for (MultipartFile file : files) {
			filesMap.put(file.getOriginalFilename(), file);
		}

		Folder folder = cmisFolder.getFolderById(task.getProcessInstance().getFolderId());
		
		try {
			// check if task's instance exists before doing anything else
			workflowInstance = processRepository.getInstanceById(task.getProcessInstance().getId());
			
		} catch(NoResultException | EmptyResultDataAccessException noResult ) { 
			throw new InvalidRequestException("taskInstanceNotExistsContactAdmin");
		}
		
		if(workflowInstance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
			throw new InvalidRequestException("taskInstanceSuspendedContactAdmin");
			
		if(workflowInstance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
			throw new InvalidRequestException("taskInstanceDeletedContactAdmin");

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
	@Deprecated
	public void tempTaskSave(WfTask task) throws InvalidRequestException {
		try {
			// check if task's instance exists before doing anything else
			WorkflowInstance tasksInstance = processRepository.getInstanceById(task.getProcessInstance().getId());
			
			if(tasksInstance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
				throw new InvalidRequestException("taskInstanceSuspendedContactAdmin");
			
			if(tasksInstance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
				throw new InvalidRequestException("taskInstanceDeletedContactAdmin");
			
			activitiFormSrv.saveFormData(task.getId(), task.getVariableValues());
			
		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
			
		} catch(NoResultException | EmptyResultDataAccessException noResult ) { 
			throw new InvalidRequestException("taskInstanceNotExistsContactAdmin");
		}
	}

	/**
	 * Completes task
	 * 
	 * @param task
	 * @throws InvalidRequestException
	 */
	@Deprecated
	public void completeTask(WfTask task, MultipartFile[] files) throws InvalidRequestException {
		
		String userId = getAccessToken().getEmail();
		String user = getAccessToken().getName();
		
		Map<String, MultipartFile> filesMap = new HashMap<String, MultipartFile>();
		
		for (MultipartFile file : files) {
			filesMap.put(file.getOriginalFilename(), file);
		}

		Folder folder = cmisFolder.getFolderById(task.getProcessInstance().getFolderId());
		
		if (task.getAssignee().equals(userId) || hasRole(ROLE_ADMIN)) {
			try {
				
				// check if task's instance exists before doing anything else
				WorkflowInstance tasksInstance = processRepository.getInstanceById(task.getProcessInstance().getId());
				
				if(tasksInstance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
					throw new InvalidRequestException("taskInstanceSuspendedContactAdmin");
				
				if(tasksInstance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
					throw new InvalidRequestException("taskInstanceDeletedContactAdmin");

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

				// get the task's variable values in order to save them
				Map<String, String> variableValues = task.getVariableValues();

				if (variableValues != null && !variableValues.isEmpty())
					activitiFormSrv.saveFormData(task.getId(), variableValues);

				// finally complete the task
				activitiTaskSrv.complete(task.getId());

			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
				
			} catch(NoResultException | EmptyResultDataAccessException noResult ) { 
				throw new InvalidRequestException("taskInstanceNotExistsContactAdmin");
			}
		}
	}

	/**
	 * Retuns a list of user ids based on workflow definition of the task
	 * 
	 * @param taskId
	 * @return
	 */
	@Deprecated
	public List<WfUser> getCandidatesByTaskId(String taskId) {
		List<WfUser> candidates = new ArrayList<WfUser>();
		List<WfUser> tempList = new ArrayList<WfUser>();

		List<IdentityLink> links = activitiTaskSrv.getIdentityLinksForTask(taskId);

		if (links == null || links.size() == 0) {
			return candidates;
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

	public List<WfUser> getCandidatesWithEmptyListAlso(String taskId){
		List<WfUser> users = this.getCandidatesByTaskId(taskId);

		if(users.isEmpty()){
			for (WfUser user : realmService.getAllUsers()) {
				if (user.getEmail() != null)
					user.setPendingTasks(activitiTaskSrv.createTaskQuery().active().taskAssignee(user.getEmail()).count());
				if (!users.contains(user))
					users.add(user);
			}
		}

		return users;
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
	 * Returns Assigned tasks for the user in context (Logged in user)
	 * 
	 * @return
	 * @throws InvalidRequestException 
	 */
	@Deprecated
	public List<WfTask> getTasksForUser() throws InvalidRequestException {
		List<WfTask> returnList = new ArrayList<WfTask>();
		List<Task> taskList = new ArrayList<Task>();
		String userId = getAccessToken().getEmail();

		// Getting tasks for user
		taskList = activitiTaskSrv.createTaskQuery().orderByTaskCreateTime().taskAssignee(userId).asc().list();

		for (Task task : taskList) {
			TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());
			
			// check if tasks's instance is running
			if(taskPath.getInstance().getStatus().equals(WorkflowInstance.STATUS_RUNNING)) {
				WfTask wfTask = new WfTask(task);
				wfTask.setStartForm(taskPath.getDefinition().hasStartForm());
				wfTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));

				returnList.add(hydrateTask(wfTask));
			}
		}
		return returnList;
	}

	/**
	 * Set assignee to a task
	 * 
	 * @param wfTask
	 * @param assigneeId
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	@Deprecated
	public void assignTask(WfTask wfTask, String assigneeId) throws InvalidRequestException {
		
		String userId = getAccessToken().getEmail();
		
		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
			throw new InvalidRequestException("claimTaskInstanceSuspended");

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_DELETED))
			throw new InvalidRequestException("claimTaskInstanceDeleted");

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_ENDED))
			throw new InvalidRequestException("claimTaskInstanceEnded");

		// check if task is supervised by the person who request to assign the
		// task or if is admin
		if (wfTask.getProcessInstance().getSupervisor().equals(userId) || hasRole(ROLE_ADMIN)) {
			try {

				if (wfTask.getTaskForm() != null) {

					for (WfFormProperty property : wfTask.getTaskForm()) {

						if (property.getType().equals("conversation")) {

							property.setValue(fixConversationMessage(property.getValue(), userId));
						}
					}
				}

				//Map<String, String> variableValues = wfTask.getVariableValues();
				//
				// if (variableValues != null && !variableValues.isEmpty()) {
				//
				// activitiFormSrv.saveFormData(wfTask.getId(), variableValues);
				// }

				activitiTaskSrv.claim(wfTask.getId(), assigneeId);
				mailService.sendTaskAssignedMail(assigneeId, wfTask);

			} catch (ActivitiException e) {

				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}
			// the person who request to assign the task, is not supervisor for
			// the task or admin
		} else {
			throw new InvalidRequestException("noAuthorizedToAssignTask");
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
	@Deprecated
	public void assignTask(WfTask wfTask, String assigneeId, MultipartFile[] files) throws InvalidRequestException {
		
		AccessToken token = getAccessToken();
		String userId = token.getEmail();
		String user = token.getName();

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
			throw new InvalidRequestException("claimTaskInstanceSuspended");

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_DELETED))
			throw new InvalidRequestException("claimTaskInstanceDeleted");

		if (wfTask.getProcessInstance().getStatus().equals(WorkflowInstance.STATUS_ENDED))
			throw new InvalidRequestException("claimTaskInstanceEnded");
		
		// check if task is supervised by the person who request to assign the
		// task or if is admin
		if (wfTask.getProcessInstance().getSupervisor().equals(userId) || hasRole(ROLE_ADMIN)) {

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

								DocumentType documentType = new DocumentType(wfDocument.getTitle(),
										wfDocument.getVersion(), wfDocument.getDocumentId(), user, userId,
										now.getTime(), wfDocument.getRefNo());

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

				// Map<String, String> variableValues =
				// wfTask.getVariableValues();
				//
				// if (variableValues != null && !variableValues.isEmpty()) {
				//
				// activitiFormSrv.saveFormData(wfTask.getId(), variableValues);
				// }

				activitiTaskSrv.claim(wfTask.getId(), assigneeId);
				mailService.sendTaskAssignedMail(assigneeId, wfTask);

			} catch (ActivitiException e) {

				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}
			// the person who request to assign the task, is not supervisor for
			// the task or admin
		} else {
			throw new InvalidRequestException("noAuthorizedToAssignTask");
		}
	}

	/**
	 * Removes assignee from a task
	 * 
	 * @param taskId
	 * @throws InvalidRequestException
	 */
	@Deprecated
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
	 * Claims the logged in assignee to the task
	 * 
	 * @param taskId
	 * @throws InvalidRequestException
	 */
	@Deprecated
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
	 * @throws InvalidRequestException 
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
				if (!groupAndRole[0].isEmpty() && !realmService.groupContainsUser(groupAndRole[0]))
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
			if(taskPath.getInstance().getStatus().equals(WorkflowInstance.STATUS_RUNNING)) {
				
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

		List<Task> dueTasks = activitiTaskSrv.createTaskQuery().active().taskDueBefore(alertDate).taskDueAfter(today).list();

		for (Task task : dueTasks) {

			String recipient = task.getAssignee();
			boolean unAssigned = false;

			if (recipient == null) {

				WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
				recipient = instance.getSupervisor();
				unAssigned = true;
			}

			mailService.sendDueTaskMail(recipient, task, unAssigned);
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

			mailService.sendTaskExpiredMail(recipient, task, unAssigned);
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
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public WfTaskDetails updateTaskDetails(WfTaskDetails wfTaskDetails) throws InvalidRequestException {

		UserTaskDetails taskDetails;
		try {
			taskDetails = processRepository.getUserTaskDetailsById(wfTaskDetails.getId());
			
		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("noTaskDetailsEntity");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(taskDetails.getDefinitionVersion().getWorkflowDefinition().getOwner())) {
			taskDetails.updateFrom(wfTaskDetails);
			taskDetails = processRepository.save(taskDetails);
			
		} else 
			throw new InvalidRequestException("notAuthorizedToUpdateTaskDetails");

		return new WfTaskDetails(taskDetails);
	}

	/**
	 * Called from listener. Notifies the end of a process.
	 * 
	 * @param processInstanceId
	 */
	@Transactional(rollbackFor = Exception.class)
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
	@Transactional(rollbackFor = Exception.class)
	public void notifyInstanceStarted(String processInstanceId) throws ServiceException  {
		// get instance's variables, after we added them from startProcess function
		Map<String, Object> instanceVariables = activitiRuntimeSrv.getVariables(processInstanceId);
	
		try {
			// implicity add instance's id variable to instance 
			activitiRuntimeSrv.setVariable(processInstanceId, "instanceId", processInstanceId);
			
			// checking if not null else it will throw an exception ?!
			if(instanceVariables != null) {
				
				String definitionVersionId = (String) instanceVariables.get("definitionVersionId");
				
				if (definitionVersionId == null || definitionVersionId.isEmpty()) {
					logger.info("Sub process started");
					return;
				}
				
				WorkflowInstance workflowInstance = new WorkflowInstance();
				
				workflowInstance.setId(processInstanceId);
				workflowInstance.setFolderId((String) instanceVariables.get("folderId"));
				workflowInstance.setTitle((String) instanceVariables.get("instanceTitle"));
				workflowInstance.setReference((String) instanceVariables.get("instanceReference"));
				workflowInstance.setSupervisor((String) instanceVariables.get("instanceSupervisor"));
				workflowInstance.setStartDate(new Date());
				workflowInstance.setStatus(WorkflowInstance.STATUS_RUNNING);
				workflowInstance.setClient((String) instanceVariables.get("device"));
				
				DefinitionVersion definitionVersion = processRepository.getDefinitionVersionById(Integer.parseInt(definitionVersionId));
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
	 * Searches for ended instances based on given criteria
	 * 
	 * @param title
	 *            The instance's title
	 * 
	 * @param after
	 *            The instance should be completed after
	 * 
	 * @param before
	 *            The instance should be completed before
	 * 
	 * @param anonymous
	 *            If true will return all instances matched with the given
	 *            criteria, else will return the instances not only matched with
	 *            the criteria, but the instances which logged in user is
	 *            supervisor
	 * 
	 * @return List of {@link WfTask}
	 */
	@Deprecated
	public List<WfTask> getEndedProcessInstancesTasks(String title, long after, long before, boolean anonymous) {
		List<WfTask> wfTasks = new ArrayList<WfTask>();
		Date dateAfter = new Date(after);
		Date dateBefore;
		
		if(before != 0)
			dateBefore = new Date(before);
		
		// get the next day
		else {
			Date today = new Date();
			dateBefore = new Date(today.getTime() + (1000 * 60 * 60 * 24));
		}

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

		if (user == null)
			throw new InvalidRequestException("noUserExistsWithId");

		String assignee = user.getEmail();

		HistoricTaskInstanceQuery taskQuery = activitiHistorySrv.createHistoricTaskInstanceQuery().taskAssignee(assignee);
		
		if(after != 0)
			taskQuery.taskCreatedAfter(dateAfter);
		
		if(before != 0)
			taskQuery.taskCreatedBefore(dateBefore);
			
		List<HistoricTaskInstance> historicTasks = taskQuery.list();

		for (HistoricTaskInstance hit : historicTasks) {
			WfTask wfTask = new WfTask(hit);

			
			try {
				WorkflowInstance instance = processRepository.getInstanceById(hit.getProcessInstanceId());
				WorkflowDefinition workflowDefinition = instance.getDefinitionVersion().getWorkflowDefinition();

				if((hasRole(ROLE_PROCESS_ADMIN) || hasRole(ROLE_SUPERVISOR)) && !hasGroup(workflowDefinition.getOwner()))
					continue;
				wfTask.setProcessInstance(new WfProcessInstance(instance));
				wfTask.setIcon(workflowDefinition.getIcon());
				wfTask.setDefinitionName(workflowDefinition.getName());
				wfTask.setProcessId(workflowDefinition.getId());
				
				// finally add task if not already exists to return list
				if (!wfTasks.contains(wfTask))
					wfTasks.add(wfTask);
				
			} catch(Exception e) {
				
			}
		}
		
		return wfTasks;
	}

	/**
	 * Apply current workflow settings
	 * 
	 * @param task
	 */
	public void applyTaskSettings(Task task) {
		WorkflowSettings settings = definitionService.getSettings();

		List<WfUser> users = this.getCandidatesByTaskId(task.getId());
		String userEmail;

		// Case of no candidate
		if(users.isEmpty() && task.getAssignee() == null){
		}
		// Case of a candidate group
		else if(!settings.isAutoAssignment() || users.size() > 1) {
			for (WfUser user : users) {
				String email = user.getEmail();
				mailService.sendCandidateGroupMail(email, task);
			}
		}
		// Case of a candidate user
		else if(users.size() > 0) {
			userEmail = users.get(0).getEmail();
			activitiTaskSrv.claim(task.getId(), userEmail);
			if (settings.isAssignmentNotification())
				mailService.sendTaskAssignedMail(userEmail, task);
		}
		// Case of a task assignee
		else if (task.getAssignee() != null) {
			userEmail = task.getAssignee();
			activitiTaskSrv.claim(task.getId(), userEmail);
			if (settings.isAssignmentNotification())
				mailService.sendTaskAssignedMail(userEmail, task);
			logger.info("Assignee email: " + userEmail);
		}
		// Case of no candidate
		else {
			String adminEmail = environment.getProperty("mail.admin");
			WorkflowDefinition workflowDef = processRepository.getProcessByDefinitionId(task.getProcessDefinitionId());
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
			mailService.sendBpmnErrorEmail(adminEmail, workflowDef, task, instance);
		}
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
	 * Update the system settings using the settings (api model)
	 * 
	 * @param wfSettings
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public WorkflowSettings updateSettings(WfSettings wfSettings) {

		byte[] facebooktokens = null;
		byte[] twittertokens = null;

		if (settingsStatus.getWorkflowSettings() != null){
			facebooktokens = settingsStatus.getWorkflowSettings().getFacebookTokens();
			twittertokens = settingsStatus.getWorkflowSettings().getTwitterTokens();
		}

		WorkflowSettings settings = new WorkflowSettings(wfSettings, facebooktokens, twittertokens);
		settingsStatus.setWorkflowSettings(settings);

		return processRepository.updateSettings(settings);
	}

	/**
	 * Update the system settings
	 * 
	 * @param settings The entity to be updated
	 * 
	 * @return {@link WorkflowSettings} The updated settings
	 */
	@Transactional(rollbackFor = Exception.class)
	public WorkflowSettings updateSettings(WorkflowSettings settings) {
		settingsStatus.setWorkflowSettings(settings);
		return processRepository.updateSettings(settings);
	}

	/**
	 * Renamed from "getProcessExternalForms" Returns all available external
	 * forms for a definition by its id
	 * 
	 * @param definitionId
	 * @return
	 */
	public List<WfPublicForm> getExternalFromsByDefinitionId(int definitionId) {
		List<WfPublicForm> wfPublicForms = new ArrayList<WfPublicForm>();
		List<ExternalForm> externalForms = new ArrayList<ExternalForm>();

		externalForms = processRepository.getProcessExternalForms(definitionId);
		wfPublicForms = WfPublicForm.fromExternalForms(externalForms);

		return wfPublicForms;
	}

	/**
	 * Returns all available registries
	 * 
	 * @return List of {@link Registry}
	 */
	public List<Registry> getRegistries() {
		
		return processRepository.getRegistries();
	}

	/**
	 * Updates a registry
	 * 
	 * @param registry The entity to be updated
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void updateRegistry(Registry registry) throws InvalidRequestException {

		processRepository.saveRegistry(registry);
	}
	
	/**
	 * Creates a new registry
	 * 
	 * @param registry The entity to be created
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void createRegistry(Registry registry) throws InvalidRequestException {
		if (processRepository.checkIfRegistryExists(registry.getId()) > 0)
			throw new InvalidRequestException("Registry with id " + registry.getId() + " exists");

		processRepository.saveRegistry(registry);
	}
	
	/**
	 * Deletes a registry if it does not belong to a definition
	 * 
	 * @param registryId
	 *            The registry's id to be deleted
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteRegistry(String registryId) throws InvalidRequestException {
		if (processRepository.checkIfDefinitionHasRegistry(registryId) > 0)
			throw new InvalidRequestException("Cannot delete registry because its referred to a definition");
		else
			processRepository.deleteRegistry(registryId);
	}

	/**
	 * Renamed from "createExternalForm"
	 * 
	 * Creates an external form
	 * 
	 * @param wfPublicForm
	 *            The entity to be created
	 * 
	 * @return {@link WfPublicForm} The created entity
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfPublicForm createPublicForm(WfPublicForm wfPublicForm) throws InvalidRequestException {
		WorkflowDefinition workflowDefinition;
		ExternalForm externalForm = new ExternalForm();

		try {
			workflowDefinition = processRepository.getById(wfPublicForm.getWorkflowDefinitionId());
			
		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("noProcessDefinitionWithID");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(workflowDefinition.getOwner())) {
			
			// check if there is an external form with the same name
			Long count = processRepository.checkForExternalForm(wfPublicForm.getFormId());

			if (count > 0)
				throw new InvalidRequestException("An external form with identical id exists.");

			externalForm.updateFrom(wfPublicForm, workflowDefinition);
			externalForm = processRepository.saveExternalForm(externalForm);
			
		} else
			throw new InvalidRequestException("You are not authorized to create external form");

		return new WfPublicForm(externalForm);
	}

	/**
	 * Updates an external form
	 * 
	 * @param
	 * @return
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfPublicForm updateExternalForm(WfPublicForm wfPublicForm) throws InvalidRequestException {
		WorkflowDefinition workflow;
		ExternalForm externalForm = new ExternalForm();

		try {
			workflow = processRepository.getById(wfPublicForm.getWorkflowDefinitionId());
			
		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("noProcessDefinitionWithID");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(workflow.getOwner())) {
			externalForm.updateFrom(wfPublicForm, workflow);
			externalForm = processRepository.saveExternalForm(externalForm);

		} else
			throw new InvalidRequestException("You are not authorized to update external form");

		return new WfPublicForm(externalForm);
	}

	/**
	 * Delete an external form
	 * 
	 * @param externalFormId
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteExternalForm(String externalFormId) throws InvalidRequestException {
		ExternalForm externalForm;

		try {
			externalForm = processRepository.getExternalForm(externalFormId);
			
		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("There is no external form with the specified id");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(externalForm.getWorkflowDefinition().getOwner()))
			processRepository.deleteExternalForm(externalForm.getId());
		else
			throw new InvalidRequestException("You are not authorized to delete the external form");
	}

	/**
	 * Suspend / Resume an external form
	 * 
	 * @param externalFormId
	 *            External form's id
	 * 
	 * @param enabled
	 *            A boolean to enable/disable the external form
	 * 
	 * @return {@link WfPublicForm} The updated entity
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public WfPublicForm modifyExternalFormStatus(String externalFormId, boolean enabled) throws InvalidRequestException {
		ExternalForm externalForm;

		try {
			externalForm = processRepository.getExternalForm(externalFormId);

			
		} catch(NoResultException | EmptyResultDataAccessException noResult ) {
			throw new InvalidRequestException("There is no external form with the specified id");
		}
		
		if (hasRole(ROLE_ADMIN) || hasGroup(externalForm.getWorkflowDefinition().getOwner())){
			externalForm.setEnabled(enabled);
			externalForm = processRepository.saveExternalForm(externalForm);
			
		}else
			throw new InvalidRequestException("You are not authorized to delete the external form");

		return new WfPublicForm(externalForm);
	}

	/**
	 * Returns status for an instance based on reference id
	 * 
	 * @param referenceId
	 *            The reference id to search for an instance
	 * 
	 * @return {@link WfProcessStatus}
	 * @throws InvalidRequestException
	 */
	public WfProcessStatus getProcessStatusByReferenceId(String referenceId) throws InvalidRequestException {
		WfProcessStatus processStatus = new WfProcessStatus();
		WorkflowInstance instance = new WorkflowInstance();

		// need that list in order to use the function to get the tasks for
		// that instance
		List<String> instanceIds = new ArrayList<String>();

		try {
			instance = processRepository.getInstanceByReferenceId(referenceId);
			
			// set to process status object its properties
			processStatus.setStatus(instance.getStatus());
			instanceIds.add(instance.getId());
			
			List<Task> pendingTasks = activitiTaskSrv.createTaskQuery().processInstanceId(instance.getId()).orderByTaskCreateTime().desc().list();
			processStatus.setTasks(getCompletedTasksByInstances(instanceIds));

			if (pendingTasks != null && pendingTasks.size() > 0)
				processStatus.setPendingTaskDescr(pendingTasks.get(0).getName());

		} catch (Exception e) {
			logger.error("Request with id: " + referenceId + " not found.");
			throw new InvalidRequestException("No request with that reference found.");
		}

		return processStatus;
	}
	
	/**
	 * Updates from a UserTaskFormElement its description and device properties
	 * 
	 * @param wfFormProperty
	 *            The entity contains the description and the device of the
	 *            property
	 * 
	 * @param taskDefinitionKey
	 *            The task's key
	 * 
	 * @param definitionVersion
	 *            The definition's version
	 * 
	 * @return {@link UserTaskFormElement} The updated entity
	 */
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public UserTaskFormElement saveTaskFormElement(WfFormProperty wfFormProperty, String taskDefinitionKey, String definitionVersion) {

		UserTaskFormElement taskFormElement = processRepository.getUserTaskFromElement(definitionVersion, taskDefinitionKey, wfFormProperty.getId());

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
	 * Retrieves and stores a permanent token and the page id for a facebook page
	 * 
	 * @param fbResponse
	 * @throws InvalidRequestException
	 */
	public boolean claimPermanentAccessToken(FBLoginResponse fbResponse) throws InvalidRequestException {

		String accessPage = fbResponse.getPage();

		if (accessPage == null) {
			throw new InvalidRequestException("No page has been specified");
		}

		WorkflowSettings settings = this.getSettings();

		Map<String, String> tokensMap = settings.fetchFacebookTokensAsMap();
		if (tokensMap == null)
			tokensMap = new HashMap<String, String>();

		// check if a token exists for the page. If yes, return.
		if (tokensMap.get(accessPage) != null)
			return true;

		String oauthUrl = environment.getProperty("fb.graphOauthUrl");
		String clientId = environment.getProperty("fb.clientId");
		String grantType = environment.getProperty("fb.grantType");
		String clientSecret = environment.getProperty("fb.clientSecret");

		URI buildLLT = UriBuilder.fromPath(oauthUrl).queryParam("client_id", clientId)
				.queryParam("grant_type", grantType).queryParam("client_secret", clientSecret)
				.queryParam("fb_exchange_token", fbResponse.getAccessToken()).build();

		String url = buildLLT.toString();

		//Obtain long-live token to exchange for a permanent token
		Facebook facebook = new FacebookTemplate(fbResponse.getAccessToken());
		String graphTokenUrl = facebook.getBaseGraphApiUrl();
		ResponseEntity<String> exchange = facebook.restOperations().exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);
		String response = exchange.getBody();
		String longLiveToken = extractFBResponseElement(response, "access_token", "=");

		if (longLiveToken.isEmpty() || longLiveToken == null)
			return false;

		URI buildPT = UriBuilder.fromPath(graphTokenUrl + fbResponse.getUserID() + "/accounts")
				.queryParam("access_token", longLiveToken).build();

		url = buildPT.toString();

		try {
			exchange = facebook.restOperations().exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);
		} catch (RestClientException e) {

			throw new InvalidRequestException(
					"Request failed. Check the facebook " + "connection parameters:: " + e.getMessage());
		}

		response = exchange.getBody();

		JSONObject jObj = new JSONObject(response);
		String data = jObj.getString("data");

		JSONArray jsonArray = jObj.getJSONArray("data");
		Object jsonArrayObject;
		String page = null;
		String pageId = null;
		String permanentToken = null;

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonArrayObject = jsonArray.get(i);
			page = extractFBResponseElement(jsonArrayObject.toString(), "name", ":");
			page = page.substring(1, page.length() - 1);
			
			if (page.equals(accessPage)) {
				pageId = extractFBResponseElement(jsonArrayObject.toString(), "id", ":");
				pageId = pageId.substring(1, pageId.length() - 1);
				permanentToken = extractFBResponseElement(data, "access_token", ":");
				permanentToken = permanentToken.substring(1, permanentToken.length() - 1);				
			}
		}

		//Store permanent token and page id
		if (permanentToken != null) {
			tokensMap.put(accessPage, permanentToken+","+pageId);
			settings.assignFacebookTokensFromMap(tokensMap);
			this.updateSettings(settings);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Confirms validity of permanent access token for each registered Facebook
	 * page and retrieves the profile and cover images of the account
	 * 
	 * @param pages
	 * @return
	 * @throws IOException
	 */
	public List<ApiFacebookPage> confirmAccessTokens(String[] pages) throws IOException {

		List<ApiFacebookPage> facebookPages = new ArrayList<ApiFacebookPage>();

		Facebook facebook;
		String graphTokenUrl;
		String[] values;
		String tokenToTest;
		String pageId;

		String validateUrl;

		if (pages == null || pages.length == 0)
			return null;

		WorkflowSettings settings = this.getSettings();
		Map<String, String> tokensMap = settings.fetchFacebookTokensAsMap();

		if (tokensMap == null || tokensMap.isEmpty())
			return null;

		for (int i = 0; i < pages.length; i++) {
			String page = pages[i];
			if (tokensMap.containsKey(page)) {
				values = tokensMap.get(page).split(",");
				tokenToTest = values[0];
				pageId = values[1];

				validateUrl = environment.getProperty("fb.validateUrl");
				validateUrl += tokenToTest;

				URL url = new URL(validateUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				StringBuilder sb = new StringBuilder();
				String line;
				try {
					while ((line = rd.readLine()) != null) {
						sb.append(line);
					}
					rd.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				ApiFacebookPage fbpage;
				facebook = new FacebookTemplate(tokenToTest);
				graphTokenUrl = facebook.getBaseGraphApiUrl();

				if (sb.indexOf(environment.getProperty("fb.clientId")) > -1) {
					URI uri = null;
					try {
						// Use token to retrieve profile and cover pictures urls
						uri = new URI(
								graphTokenUrl + pageId + "?access_token=" + tokenToTest + "&fields=picture,cover");
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}

					ResponseEntity<String> exchange = facebook.restOperations().exchange(uri, HttpMethod.GET,
							HttpEntity.EMPTY, String.class);

					String exchangeBody = exchange.getBody();

					String profilePicUrl = null;
					String coverPicUrl = null;

					// Parse the json response to retrieve the desired urls.
					JsonParser jparser = Json.createParser(new StringReader(exchangeBody));
					while (jparser.hasNext()) {
						Event event = jparser.next();
						if (event.equals(Event.KEY_NAME) && jparser.getString().equals("url")) {
							jparser.next();
							profilePicUrl = jparser.getString();
						}
						if (event.equals(Event.KEY_NAME) && jparser.getString().equals("source")) {
							jparser.next();
							coverPicUrl = jparser.getString();
						}
					}

					fbpage = new ApiFacebookPage(page, true, profilePicUrl, coverPicUrl);

				} else
					fbpage = new ApiFacebookPage(page, false, null, null);

				facebookPages.add(fbpage);
			}

			else
				facebookPages.add(new ApiFacebookPage(page, false, null, null));
		}

		return facebookPages;
	}
	
	/**
	 * Removes access to the specified facebook page
	 * 
	 * @param pageName
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public void removeFacebookPageAccess(String pageName) {

		if (pageName == null || pageName.isEmpty())
			return;

		WorkflowSettings settings = this.getSettings();
		Map<String, String> tokensMap = settings.fetchFacebookTokensAsMap();
		
		if (tokensMap == null || tokensMap.isEmpty())
			return;

		tokensMap.remove(pageName);
		settings.assignFacebookTokensFromMap(tokensMap);
		processRepository.updateSettings(settings);
	}
	
	/**
	 * Authenticate & Authorize Twitter
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	public TwitterAuthorization authTwitter() {

		String consumerKey = environment.getProperty("twitter.consumerKey");
		String consumerSecret = environment.getProperty("twitter.consumerSecret");
		String callback = environment.getProperty("twitter.callback");

		Twitter twitter = new TwitterTemplate(consumerKey, consumerSecret);
		OAuth1Operations oauth1Operations = twitterConnectionFactory.getOAuthOperations();
		OAuth1Parameters oAuth1Parameters = new OAuth1Parameters();

		requestToken = oauth1Operations.fetchRequestToken(callback, null);

		@SuppressWarnings("static-access")
		String authorizeUrl = oauth1Operations.buildAuthorizeUrl(requestToken.getValue(), oAuth1Parameters.NONE);
		TwitterAuthorization auth = new TwitterAuthorization(authorizeUrl, requestToken.getValue());

		return auth;
	}
	
	/**
	 * Twitter Service associated with the callback
	 * 
	 * @param oauthVerifier
	 */
	public ModelAndView getTwitterAccessToken(String oauthVerifier) {

		WorkflowSettings settings = this.getSettings();
		Map<String, String> tokensMap = settings.fetchTwitterTokensAsMap();

		if (tokensMap == null)
			tokensMap = new HashMap<String, String>();

		String consumerKey = environment.getProperty("twitter.consumerKey");
		String consumerSecret = environment.getProperty("twitter.consumerSecret");
		String returnUrl = environment.getProperty("managerURL");

		OAuth1Operations oauth1Operations = twitterConnectionFactory.getOAuthOperations();

		AuthorizedRequestToken authorizedRequestToken = new AuthorizedRequestToken(requestToken, oauthVerifier);
		OAuthToken accessToken = oauth1Operations.exchangeForAccessToken(authorizedRequestToken, null);
		accessToken.getValue();
		Twitter twitter = new TwitterTemplate(consumerKey, consumerSecret, accessToken.getValue(),
				accessToken.getSecret());
		String screenName = twitter.userOperations().getScreenName();

		TwitterProfile profile = twitter.userOperations().getUserProfile();

		// check if an access token already exists for the twitter account
		if (tokensMap.get(screenName) != null)
			tokensMap.remove(screenName);

		tokensMap.put(screenName, accessToken.getValue() + "," + accessToken.getSecret() + "," + profile.getName() + ","
				+ profile.getProfileImageUrl() + "," + profile.getProfileBannerUrl());

		settings.assignTwitterTokensFromMap(tokensMap);
		updateSettings(settings);
		ModelAndView redirect = new ModelAndView("redirect:" + returnUrl + "/app");
		return redirect;
	}
	
	/**
	 * Get all authorized twitter accounts
	 * 
	 * @return
	 */
	public List<ApiTwitterAccount> getTwitterAccounts() {
		List<ApiTwitterAccount> accounts = new ArrayList<ApiTwitterAccount>();

		WorkflowSettings settings = getSettings();

		Map<String, String> accountsMap = settings.fetchTwitterTokensAsMap();

		if (accountsMap != null) {

			for (Map.Entry<String, String> entry : accountsMap.entrySet()) {
				String valuesString = entry.getValue();
				String[] values = valuesString.split(",");
				// value[2]:name
				// value[3]:profileImageUrl
				// value[4]:coverImageUrl
				ApiTwitterAccount account = new ApiTwitterAccount(entry.getKey(), values[2], values[3], values[4]);
				accounts.add(account);
			}

		}

		return accounts;
	}

	/**
	 * Removes access to the specified twitter account
	 * 
	 * @param screenName
	 */
	@Transactional(rollbackFor = Exception.class)
	public void removeTwitterAccountAccess(String screenName) {
		if (screenName == null || screenName.isEmpty())
			return;

		WorkflowSettings settings = getSettings();
		Map<String, String> tokensMap = settings.fetchTwitterTokensAsMap();
		
		if (tokensMap == null || tokensMap.isEmpty())
			return;

		tokensMap.remove(screenName);
		settings.assignTwitterTokensFromMap(tokensMap);
		processRepository.updateSettings(settings);
	}
	
	
	/**
	 * Returns all mobile enabled external forms
	 * 
	 * @return
	 */
	public List<WfPublicService> getExternalServices() {
		List<WfPublicService> returnList = new ArrayList<WfPublicService>();
		List<ExternalForm> externalForms = new ArrayList<ExternalForm>();

		externalForms = processRepository.getExternalForms();

		for (ExternalForm externalForm : externalForms) {
			if (externalForm.isEnabled() && externalForm.isMobileEnabled()) {
				WfPublicService externalService = new WfPublicService(externalForm);
				returnList.add(externalService);
			}
		}
		return returnList;
	}
	
	/**
	 * Returns a process instance by its id
	 * 
	 * @param instanceId
	 *            The process instance's id
	 * 
	 * @return {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 */
	public WfProcessInstance getProcessInstanceById(String instanceId) throws InvalidRequestException {
		WfProcessInstance wfProcessInstance;
		
		try {
			wfProcessInstance = new WfProcessInstance(processRepository.getInstanceById(instanceId));
			
		} catch (Exception e) {
			throw new InvalidRequestException("processInstanceIDNotValid");
		}
		return wfProcessInstance;
	}

	/**
	 * Deletes an instance by instance id
	 * 
	 * @param instanceId
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteProcessCompletedInstance(String instanceId) {
		// delete from activiti
		activitiHistorySrv.deleteHistoricProcessInstance(instanceId);

		// delete from workflow instance table
		processRepository.deleteProcessInstance(instanceId);
	}

	/**
	 * Returns a list of external wrapper class which contains group and forms
	 * 
	 * @return
	 */
	public List<ExternalWrapper> getExternalWrapper() {
		List<ExternalWrapper> returnList = new ArrayList<ExternalWrapper>();

		returnList = processRepository.getExternalFormsGroupsWrapped();
		return returnList;
	}

	/**
	 * Returns all available external groups
	 * 
	 * @return
	 */
	public List<WfPublicGroup> getExternalGroups() {
		List<WfPublicGroup> returnList = WfPublicGroup.fromExternalGroups(processRepository.getExternalGroups());

		return returnList;
	}

	/**
	 * Creates a new external group
	 * 
	 * @param wfExternalGroup
	 */
	@Transactional(rollbackFor = Exception.class)
	public void createExternalGroup(WfPublicGroup wfExternalGroup) {
		// create an entity object for an api one
		ExternalGroup externalGroup = new ExternalGroup(wfExternalGroup);

		processRepository.createExternalGroup(externalGroup);
	}

	/**
	 * Returns all available external forms
	 * 
	 * @return
	 */
	public List<WfPublicForm> getExternalforms() {

		return WfPublicForm.fromExternalForms(processRepository.getExternalForms());
	}

	/**
	 * Deletes a public group by id
	 * 
	 * @param groupId
	 *            Group's id to be deleted
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deletePublicGroup(int groupId) throws InvalidRequestException {

		if (processRepository.checkIfPublicGroupHasForms(groupId) > 0) {
			throw new InvalidRequestException("Group has external forms");

		} else {

			try {
				processRepository.deletePublicGroup(groupId);

			} catch (Exception e) {
				throw new InvalidRequestException("Couldn't delete group " + e.getMessage());
			}
		}
	}

	/**
	 * Updates a public group
	 * 
	 * @param publicGroup
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void updatePublicGroup(WfPublicGroup publicGroup) throws InvalidRequestException {

		try {
			ExternalGroup externalGroup = new ExternalGroup(publicGroup);
			processRepository.updatePublicGroup(externalGroup);

		} catch (Exception e) {
			throw new InvalidRequestException("Couldn't update group " + e.getMessage());
		}
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
	 * Creates a new mobile user or update the existing one
	 * 
	 * @param wfExternalUser
	 */
	@Transactional
	public void saveExternalUser(WfExternalUser wfExternalUser) {
		
		try {
			ExternalUser externalUser = processRepository.getExternalUserByDeviceId(wfExternalUser.getDeviceId());
			
			// in case user exists
			if(externalUser != null) {
				externalUser.setAddress(wfExternalUser.getAddress());
				externalUser.setDeviceId(wfExternalUser.getDeviceId());
				externalUser.setEmail(wfExternalUser.getEmail());
				externalUser.setName(wfExternalUser.getName());
				externalUser.setPhoneNumber(wfExternalUser.getPhoneNo());
				externalUser.setSimPhoneNumber(wfExternalUser.getSimPhoneNumber());
				externalUser.setClient(wfExternalUser.getClient());
				
				processRepository.saveExternalUser(externalUser);
			} 
			
		// in case user doesn't exists	
		} catch (Exception e) {
			processRepository.saveExternalUser(new ExternalUser(wfExternalUser));
		}
	}
	
	public void sendTaskDueDateNotification(String taskId, String content) throws InternalException {
		Task task = activitiTaskSrv.createTaskQuery().taskId(taskId).singleResult();
		
		mailService.sendTaskDueDateNotification(task, content);
	}
	
	/**
	 * 
	 * @param body
	 * @param element
	 * @param delimiter
	 * @return
	 */
	private String extractFBResponseElement(String body, String element, String delimiter) {
		String[] parts = body.split(",");
		for (String part : parts) {
			if (part.indexOf(element) != -1) {
				String[] partsParams = part.split(delimiter);
				//--
				if(partsParams[1].indexOf("&") > -1){
					String[] subparts = partsParams[1].split("&");
					return subparts[0];
				}
				//--
				return partsParams[1];
			}
		}
		return null;
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

	/**
	 * Returns all in progress instances by given criteria
	 *
	 * @return a list of in progress instances
	 */
	public List<WfProcessInstance> getInProgressInstances(String definitionName, String instanceTitle, long after,
														  long before) {
		ArrayList<WfProcessInstance> returnList = new ArrayList<>();
		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);

		if(definitionName.isEmpty() || definitionName.equals("all"))
			definitionName = null;
		else
			definitionName = processRepository.getDefinitionByKey(definitionName).getName();

		if(instanceTitle.isEmpty() || instanceTitle.equals(" "))
			instanceTitle = null;


		for(WfProcessInstance instance : WfProcessInstance.fromWorkflowInstances(processRepository.getInProgressInstances())) {
			if (instance != null) {
				String title = instance.getTitle().toLowerCase();
				String name = instance.getDefinitionName().toLowerCase();

				if (dateBefore.getTime() == 0) {
					dateBefore = new Date();
				}

				if ((instanceTitle == null || instanceTitle.toLowerCase().equals(title))
						&& (definitionName == null || definitionName.toLowerCase().equals(name))
						&& instance.getStartDate() != null && instance.getStartDate().after(dateAfter)
						&& instance.getStartDate() != null && instance.getStartDate().before(dateBefore)) {

					returnList.add(instance);
				}
			}
		}
		return returnList;
	}

	/**
	 * Returns all ended instances by given criteria
	 *
	 * @param definitionName
	 *            The definition's name to get its ended instances
	 * @param instanceTitle
	 *            The process instance title
	 * @param after
	 *            The date after which to get the ended instances
	 * @param before
	 *            The date before which to get the ended instances
	 *
	 * @return A list of {@link WfProcessInstance}
	 */
	public List<WfProcessInstance> getEndedProcessInstances(String definitionName, String instanceTitle, long after,
															long before) {
		ArrayList<WfProcessInstance> returnList = new ArrayList<>();
		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);

		if(definitionName.isEmpty() || definitionName.equals("all"))
			definitionName = null;
		else
			definitionName = processRepository.getDefinitionByKey(definitionName).getName();

		if(instanceTitle.isEmpty() || instanceTitle.equals(" "))
			instanceTitle = null;

		for(WorkflowInstance instance : processRepository.getEndedProcessInstances()){
			if(instance != null){
				if(hasRole(ROLE_PROCESS_ADMIN) && !hasGroup(instance.getDefinitionVersion().getWorkflowDefinition().getOwner()))
					continue;
				String title = instance.getTitle().toLowerCase();
				String name = instance.getDefinitionVersion().getWorkflowDefinition().getName().toLowerCase();

				if (dateBefore.getTime() == 0) {
					dateBefore = new Date();
				}

				if ((instanceTitle == null || instanceTitle.toLowerCase().equals(title))
						&& (definitionName == null || definitionName.toLowerCase().equals(name))
						&& instance.getEndDate() != null && instance.getEndDate().after(dateAfter)
						&& instance.getEndDate() != null && instance.getEndDate().before(dateBefore)) {

					returnList.add(new WfProcessInstance(instance));
				}
			}
		}

//		if(hasRole(ROLE_ADMIN)){
//			for(WfProcessInstance instance : WfProcessInstance.fromWorkflowInstances(processRepository.getEndedProcessInstances())) {
//				if (instance != null) {
//					String title = instance.getTitle().toLowerCase();
//					String name = instance.getDefinitionName().toLowerCase();
//
//					if (dateBefore.getTime() == 0) {
//						dateBefore = new Date();
//					}
//
//					if ((instanceTitle == null || instanceTitle.toLowerCase().equals(title))
//							&& (definitionName == null || definitionName.toLowerCase().equals(name))
//							&& instance.getEn/process/filter?ownersdDate() != null && instance.getEndDate().after(dateAfter)
//							&& instance.getEndDate() != null && instance.getEndDate().before(dateBefore)) {
//
//						returnList.add(instance);
//					}
//				}
//			}
//		}
//		else {
//			for (WfProcessInstance instance : WfProcessInstance.fromWorkflowInstances(processRepository.getEndedProcessInstancesByGroups(realmService.getUserGroups()))) {
//				if (instance != null) {
//					String title = instance.getTitle().toLowerCase();
//					String name = instance.getDefinitionName().toLowerCase();
//
//					if (dateBefore.getTime() == 0) {
//						dateBefore = new Date();
//					}
//
//					if ((instanceTitle == null || instanceTitle.toLowerCase().equals(title))
//							&& (definitionName == null || definitionName.toLowerCase().equals(name))
//							&& instance.getEndDate() != null && instance.getEndDate().after(dateAfter)
//							&& instance.getEndDate() != null && instance.getEndDate().before(dateBefore)) {
//
//						returnList.add(instance);
//					}
//				}
//			}
//		}
		return returnList;
	}

	/**
	 * Changes instance's supervisor
	 * 
	 * @param instanceId The instance's id
	 * 
	 * @param supervisor The email of the new supervisor
	 * 
	 * @throws InvalidRequestException
	 */
	@Transactional(rollbackFor = Exception.class)
	public void changeInstanceSupervisor(String instanceId, String supervisor) throws InvalidRequestException {

		WorkflowInstance workflowInstance;

		try {
			workflowInstance = processRepository.getInstanceById(instanceId);
			workflowInstance.setSupervisor(supervisor);
			processRepository.save(workflowInstance);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException("instanceSupervisorChangeFailed");
		}
	}

	/**
	 * Private method for retrieving logged user token
	 * 
	 * @return Logged-in user's token
	 */
	private AccessToken getAccessToken() {
		KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
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
		if(wfTask.getEndDate() == null) {

			TaskFormData taskForm = activitiFormSrv.getTaskFormData(wfTask.getId());
			formProperties = getWfFormProperties(taskForm.getFormProperties(), wfTask);

		} else {

			HistoricTaskInstance historicTaskInstance = activitiHistorySrv.createHistoricTaskInstanceQuery().taskId(wfTask.getId()).singleResult();
			List<org.activiti.bpmn.model.FormProperty> historicFormProperties = new ArrayList<org.activiti.bpmn.model.FormProperty>();
			List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
			Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
			UserTaskDetails taskDetails = new UserTaskDetails();

			// get properties for task
			historicFormProperties = ActivitiHelper.getTaskFormDefinition(activitiRepositorySrv, wfTask.getProcessDefinitionId(), historicTaskInstance.getTaskDefinitionKey());

			// get the task details
			taskDetails = processRepository.getUserTaskDetailByDefinitionKey(historicTaskInstance.getTaskDefinitionKey(), wfTask.getProcessDefinitionId());

			// get the task form elements
			taskFormElements = processRepository.getUserTaskFromElements(wfTask.getProcessDefinitionId(), taskDetails.getId());

			// fill the usertaskform element map using as key the element id and
			// as value the user taskform element
			for (UserTaskFormElement userTaskFormElement : taskFormElements) {
				mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
			}

			// get the map contains the value as a map of property id and
			// property value
			HashMap<String, String> propertyValueMap = getFormItemsValues(wfTask.getProcessInstance().getId(), wfTask.getEndDate());

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
		DefinitionVersion definitionVersion = processRepository.getVersionByProcessDefinitionId(wfTask.getProcessDefinitionId());
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
		List<HistoricDetail> details = activitiHistorySrv.createHistoricDetailQuery().processInstanceId(instanceId).orderByTime().desc().list();

		for (HistoricDetail historicDetail : details) {

			if (historicDetail instanceof HistoricFormPropertyEntity) {

				HistoricFormPropertyEntity formEntity = (HistoricFormPropertyEntity) historicDetail;

				if (formEntity.getTime().before(taskEndDate) || formEntity.getTime().equals(taskEndDate)) {

					if (formEntity.getPropertyValue() != null)
						returnMap.put(formEntity.getPropertyId(), formEntity.getPropertyValue());
				}
			} else if (historicDetail instanceof HistoricDetailVariableInstanceUpdateEntity) {

				HistoricDetailVariableInstanceUpdateEntity formEntity = (HistoricDetailVariableInstanceUpdateEntity) historicDetail;

				if (formEntity.getTime().before(taskEndDate) || formEntity.getTime().equals(taskEndDate)) {

					if (formEntity.getValue() != null)
						returnMap.put(formEntity.getVariableName(), formEntity.getValue().toString());
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

		UserTaskDetails taskDetails = processRepository.getUserTaskDetailByDefinitionKey(task.getTaskDefinitionKey(), wfTask.getProcessDefinitionId());

		taskFormElements = processRepository.getUserTaskFromElements(wfTask.getProcessDefinitionId(), taskDetails.getId());

		// create the map
		for (UserTaskFormElement userTaskFormElement : taskFormElements) {
			mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
		}

		for (FormProperty property : formProperties) {
			// get the value for the property
			String propertyValue = property.getValue();
			
			// set a default value when approve document
			if(property.getType().getClass().equals(ApproveFormType.class))
				propertyValue = "false";

			// get the date format
			String dateFormat = (String) property.getType().getInformation("datePattern");

			//get the task form element values
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

	/**
	 * Used for the task event's form properties
	 * 
	 * @param formProperties
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<WfFormProperty> getWfFormProperties(List<FormProperty> formProperties) {
		List<WfFormProperty> wfFormProperties = new ArrayList<WfFormProperty>();

		for (FormProperty property : formProperties) {
			
			// get the date format
			String dateFormat = (String) property.getType().getInformation("datePattern");
			
			// get the property's value
			String propertyValue = property.getValue();
			
			// set a default value when approve document
			if(property.getType().getClass().equals(ApproveFormType.class))
				propertyValue = "false";
			
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
	@Transactional(rollbackFor = Exception.class)
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
							userTaskFormElement.setFormat(formProperty.getDatePattern());
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
								: copyFormElementDescriptionFromSimilar(formProperty.getId(), previousDefinitionVersion.getId());
						userTaskFormElement.setDescription(formItemDescription);

						String formItemDevice = (previousDefinitionVersion == null) ? UserTaskFormElement.ALL_DEVICES
								: copyFormElementDeviceFromSimilar(formProperty.getId(), previousDefinitionVersion.getId());
						userTaskFormElement.setDevice(formItemDevice);

						userTaskFormElement.setElementId(formProperty.getId());
						userTaskFormElement.setFormat(formProperty.getDatePattern());
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

	private String copyFormElementDescriptionFromSimilar(String elementId, int definitionVersionId) {
		List<UserTaskFormElement> userTaskFormElemets = processRepository.getUserTaskFormElements(elementId, definitionVersionId);

		for (UserTaskFormElement element : userTaskFormElemets) {
			if (element.getElementId().equals(elementId)) {
				if (element.getDescription() != null && !element.getDescription().isEmpty()) {
					return element.getDescription();
				}
			}
		}

		return "";
	}

	private String copyFormElementDeviceFromSimilar(String elementId, int definitionVersionId) {
		List<UserTaskFormElement> userTaskFormElemets = processRepository.getUserTaskFormElements(elementId, definitionVersionId);

		for (UserTaskFormElement element : userTaskFormElemets) {
			if (element.getElementId().equals(elementId)) {
				if (element.getDevice() != null && !element.getDevice().isEmpty()) {
					return element.getDevice();
				}
			}
		}

		return UserTaskFormElement.ALL_DEVICES;
	}

	/**
	 * private
	 * 
	 * Checks if a process already exists with the same key
	 * 
	 * @param key
	 * @return
	 */
	private boolean definitionExistenceCheck(String key) {
		List<ProcessDefinition> processDefinitions = activitiRepositorySrv.createProcessDefinitionQuery().processDefinitionKey(key).list();
		
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
		// get all definition's versions for the process
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
	 * @param bpmn
	 * @return
	 * @throws InvalidRequestException
	 */
	@Deprecated
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

		if(userGroups == null || userGroups.size() ==0)
			return false;
		
		return userGroups.contains(group);
	}
	
	/**
	 * A custom exception which extends {@link ActivitiException},
	 * used by service layer in order to catch activiti-relation exceptions 
	 * and then throw an {@link InternalException} or {@link InvalidRequestException}
	 * to propagate it to the controller.
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
