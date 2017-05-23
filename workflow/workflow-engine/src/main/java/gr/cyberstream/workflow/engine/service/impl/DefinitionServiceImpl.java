package gr.cyberstream.workflow.engine.service.impl;

import gr.cyberstream.workflow.engine.cmis.CMISFolder;
import gr.cyberstream.workflow.engine.config.SettingsStatus;
import gr.cyberstream.workflow.engine.model.*;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfSettings;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.ActivitiHelper;
import gr.cyberstream.workflow.engine.service.DefinitionService;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.RealmService;
import gr.cyberstream.workflow.engine.util.string.StringUtil;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import javax.persistence.NonUniqueResultException;
import javax.ws.rs.core.UriBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.*;

@Service
public class DefinitionServiceImpl implements DefinitionService {

	@Autowired
	private Processes processRepository;

	@Autowired
	private RealmService realmService;

	@Autowired
	private RepositoryService activitiRepositoryService;

	@Autowired
	private HistoryService activitiHistoryService;

	@Autowired
	private FormService activitiFormService;

	@Autowired
	private CMISFolder cmisFolder;

	@Autowired
	private SettingsStatus settingsStatus;

	@Autowired
	private Environment environment;

	private static final Logger logger = LoggerFactory.getLogger(DefinitionServiceImpl.class);

	// user roles
	private static final String ROLE_ADMIN = "ROLE_Admin";
	private static final String ROLE_PROCESS_ADMIN = "ROLE_ProcessAdmin";

	@Override
	public WfProcess getProcessById(int definitionId) {
		WorkflowDefinition workflow = processRepository.getById(definitionId);

		return new WfProcess(workflow);
	}

	@Override
	public List<WfProcess> getAllProcesses() {
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

	@Override
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

	@Override
	public List<WfProcess> getProcessDefinitions() {
		List<WorkflowDefinition> definitions = processRepository.getAll();

		return WfProcess.fromWorkflowDefinitions(definitions);
	}

	@Override
	public List<WfProcess> getDefinitionsByOwner(String owner) {
		List<WorkflowDefinition> definitions = processRepository.getDefinitionsByOwner(owner);

		return WfProcess.fromWorkflowDefinitions(definitions);
	}

	@Override
	public List<WfProcess> getDefinitionsByOwners(List<String> owners) {
		List<WorkflowDefinition> definitions = processRepository.getDefinitionsByOwners(owners);

		return WfProcess.fromWorkflowDefinitions(definitions);
	}

	@Override
	@Transactional
	public WfProcess update(WfProcess process) throws InvalidRequestException {
		WorkflowDefinition definition;

		try {
			definition = processRepository.getById(process.getId());

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process found with the given id");
		}

		// check user's roles/groups
		if (hasRole(ROLE_ADMIN) || (hasGroup(definition.getOwner()) || definition.getOwner() == null)) {

			// 1. apply some rules
			if (StringUtil.isEmpty(definition.getName()))
				throw new InvalidRequestException("the name is required for the process definition");

			// check if name already exists
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

			if (process.getRegistryId() != null)
				definition.setRegistry(processRepository.getRegistryById(process.getRegistryId()));
			else
				definition.setRegistry(null);

			processRepository.save(definition);

			// user has neither admin role nor belongs to group same as the
			// definition
		} else
			throw new InvalidRequestException("Seems you are not authorized to update the definition");

		return new WfProcess(definition);
	}

	@Override
	public WfProcess createNewProcessDefinition(InputStream inputStream, String filename, String justification)
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
			deployment = activitiRepositoryService.createDeployment().addString("input.bpmn20.xml", bpmn).name(filename)
					.deploy();
		} catch (XMLException | ActivitiIllegalArgumentException ex) {
			String message = "The BPMN input is not valid. Error string: " + ex.getMessage();
			logger.error(message);
			throw new InvalidRequestException(message);
		}

		// 2. Check deployment and get metadata from the deployed process
		// definition
		if (deployment == null) {
			logger.error("BPMN file error");
			throw new InvalidRequestException("The BPMN input is not valid");
		}

		logger.info("New BPMN deployment: " + deployment.getName());

		ProcessDefinition processDef = ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositoryService,
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

			if (sameNameWorkflowDefinition != null)
				workflow.setName(workflow.getName() + " - "
						+ DateFormatUtils.format(Calendar.getInstance(), "d-M-yyyy HH.mm.ss"));

		} catch (EmptyResultDataAccessException e) {

		}

		// create a version and set it to workflow definition
		DefinitionVersion definitionVersion = new DefinitionVersion();
		definitionVersion.setDeploymentId(deployment.getId());
		definitionVersion.setStatus(WorkflowDefinitionStatus.NEW.toString());
		definitionVersion.setVersion(processDef.getVersion());
		definitionVersion.setDeploymentdate(deployment.getDeploymentTime());
		definitionVersion.setProcessDefinitionId(processDef.getKey());
		definitionVersion.setJustification(justification);

		workflow.addDefinitionVersion(definitionVersion);

		// 3. Create Process Definition Folder
		Folder folder = cmisFolder.createFolder(null, workflow.getName());
		workflow.setFolderId(folder.getId());

		// 4. save the new process definition
		workflow = processRepository.save(workflow);

		// 5. Get task information from the bpmn model and create task details
		// entities
		createTaskDetails(workflow);

		return new WfProcess(workflow);
	}

	@Override
	@Transactional
	public WfProcessVersion createNewProcessVersion(int id, InputStream inputStream, String filename, String justification)
			throws InvalidRequestException {
		Deployment deployment;
		ProcessDefinition processDef;
		String bpmn;
		WorkflowDefinition workflow;

		try {
			workflow = processRepository.getById(id);
		} catch (Exception e) {
			throw new InvalidRequestException("No process found with the given id");
		}

		// nothing to check
		if (hasRole(ROLE_ADMIN)) {
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
			if (!definitionVersionExistenceCheck(id, processId)) {
				logger.error("Successive process versions should have the same key");
				throw new InvalidRequestException("Successive process versions should have the same key");
			}

			try {
				deployment = ActivitiHelper.createDeployment(activitiRepositoryService, bpmn, filename);

			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}

			logger.info("New BPMN deployment: " + deployment.getName());

			try {
				processDef = ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositoryService,
						deployment.getId());

			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}

			// create definition version and set it to workflow definition
			DefinitionVersion definitionVersion = new DefinitionVersion();
			definitionVersion.setDeploymentId(deployment.getId());
			definitionVersion.setStatus(WorkflowDefinitionStatus.NEW.toString());
			definitionVersion.setVersion(processDef.getVersion());
			definitionVersion.setDeploymentdate(deployment.getDeploymentTime());
			definitionVersion.setProcessDefinitionId(ActivitiHelper
					.getProcessDefinitionByDeploymentId(activitiRepositoryService, deployment.getId()).getId());
			definitionVersion.setJustification(justification);

			workflow.addDefinitionVersion(definitionVersion);

			processRepository.save(workflow);
			createTaskDetails(workflow);

			return new WfProcessVersion(definitionVersion);

			// since user has no admin role will check for groups
		} else if (hasRole(ROLE_PROCESS_ADMIN)) {
			if (hasGroup(workflow.getOwner())) {

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
				if (!definitionVersionExistenceCheck(id, processId)) {
					logger.error("Successive process versions should have the same key");
					throw new InvalidRequestException("Successive process versions should have the same key");
				}

				try {
					deployment = ActivitiHelper.createDeployment(activitiRepositoryService, bpmn, filename);

				} catch (ActivitiException e) {
					logger.error(e.getMessage());
					throw new InvalidRequestException(e.getMessage());
				}

				logger.info("New BPMN deployment: " + deployment.getName());

				try {
					processDef = ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositoryService,
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
						.getProcessDefinitionByDeploymentId(activitiRepositoryService, deployment.getId()).getId());

				workflow.addDefinitionVersion(definitionVersion);
				processRepository.save(workflow);
				createTaskDetails(workflow);

				return new WfProcessVersion(definitionVersion);

			} else
				throw new InvalidRequestException("The definition you are trying to edit doesn't belong to your group");
		}
		return null;
	}

	@Override
	public WfProcessVersion updateVersion(int processId, WfProcessVersion version) throws InvalidRequestException {
		DefinitionVersion definitionVersion;

		try {
			definitionVersion = processRepository.getVersionById(version.getId());

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process version found with the given id");
		}

		if (hasRole(ROLE_ADMIN)) {
			definitionVersion.updateFrom(version);
			processRepository.saveVersion(processId, definitionVersion);

			return new WfProcessVersion(definitionVersion);

		} else if (hasRole(ROLE_PROCESS_ADMIN)) {
			if (hasGroup(definitionVersion.getWorkflowDefinition().getOwner())) {
				definitionVersion.updateFrom(version);
				processRepository.saveVersion(processId, definitionVersion);

				return new WfProcessVersion(definitionVersion);

			} else
				throw new InvalidRequestException("The definition you are trying to edit doesn't belong to your group");
		}

		return null;
	}

	@Override
	public InputStreamResource getProcessDiagram(int processId) {
		WorkflowDefinition process = processRepository.getById(processId);
		ProcessDefinition processDefinition = activitiRepositoryService.createProcessDefinitionQuery()
				.processDefinitionId(process.getKey()).singleResult();
		ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
		BpmnModel bpmnModel = activitiRepositoryService.getBpmnModel(processDefinition.getId());

		if (bpmnModel.getLocationMap().size() == 0) {
			BpmnAutoLayout autoLayout = new BpmnAutoLayout(bpmnModel);
			autoLayout.execute();
		}

		InputStream is = processDiagramGenerator.generateJpgDiagram(bpmnModel);
		return new InputStreamResource(is);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteProcessDefinition(int processId) throws InvalidRequestException {
		WorkflowDefinition definition;

		// get the definition throw exception if not found
		try {
			definition = processRepository.getById(processId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process version found with the given id");
		}

		if (hasRole(ROLE_ADMIN) || hasGroup(definition.getOwner())) {

			// check if any of the process deployments have instances
			boolean found = false;
			for (DefinitionVersion version : definition.getDefinitionVersions()) {

				if (activitiHistoryService.createHistoricProcessInstanceQuery().notDeleted()
						.deploymentId(version.getDeploymentId()).count() > 0)
					found = true;
			}

			if (found)
				throw new InvalidRequestException("The process definition with id: " + processId
						+ "could not be deleted. There are associated entries");

			// delete all process definitions (all versions)
			String activeDeploymentId = definition.getActiveDeploymentId();

			boolean activeDeleted = false;
			for (DefinitionVersion version : definition.getDefinitionVersions()) {

				if (version.getDeploymentId().isEmpty())
					continue;

				activitiRepositoryService.deleteDeployment(version.getDeploymentId());

				if (version.getDeploymentId().equals(activeDeploymentId))
					activeDeleted = true;
			}

			// delete active deployment if not already deleted
			if (!activeDeleted && activeDeploymentId != null && !activeDeploymentId.isEmpty())
				activitiRepositoryService.deleteDeployment(activeDeploymentId);

			// delete workflow definition entry
			processRepository.delete(processId);

			cmisFolder.deleteFolderById(definition.getFolderId());

		} else
			throw new InvalidRequestException("You are not authorized to delete the definition");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public WfProcess deleteProcessDefinitionVersion(int processId, String deploymentId) throws InvalidRequestException {
		WorkflowDefinition definition;

		// get the definition throw exception if not found
		try {
			definition = processRepository.getById(processId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process version found with the given id");
		}

		// check if the version id the last one
		if (definition.getDefinitionVersions().size() < 2)
			throw new InvalidRequestException(
					"Trying to delete the last version. Delete the process definition instead.");

		// no need to check anything
		if (hasRole(ROLE_ADMIN)) {
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
				if (activitiHistoryService.createHistoricProcessInstanceQuery().notDeleted().deploymentId(deploymentId)
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
						+ "could not be deleted. There are associated entries");

			// delete the deployment
			activitiRepositoryService.deleteDeployment(deploymentId);

			// remove the version for the process definition
			definition.setDefinitionVersions(versions);

			// update the process definition
			// if the deleted version was the active one, set the active
			// deployment
			// to most recent one
			if (definition.getActiveDeploymentId().equals(deploymentId))
				definition.setActiveDeploymentId(definition.getDefinitionVersions().get(0).getDeploymentId());

			return new WfProcess(processRepository.save(definition));

		} else if (hasRole(ROLE_PROCESS_ADMIN)) {
			if (hasGroup(definition.getOwner())) {
				// check the existence of the deploymentId
				boolean found = false;
				boolean used = false;
				List<DefinitionVersion> versions = definition.getDefinitionVersions();

				for (DefinitionVersion version : versions) {

					if (!version.getDeploymentId().equals(deploymentId))
						continue;

					found = true;

					// check if the version is ever used
					if (activitiHistoryService.createHistoricProcessInstanceQuery().notDeleted()
							.deploymentId(deploymentId).count() > 0) {
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
							+ "could not be deleted. There are associated entries");

				// delete the deployment
				activitiRepositoryService.deleteDeployment(deploymentId);

				// remove the version for the process definition
				definition.setDefinitionVersions(versions);

				// update the process definition
				// if the deleted version was the active one, set the active
				// deployment
				// to most recent one
				if (definition.getActiveDeploymentId().equals(deploymentId)) {
					definition.setActiveDeploymentId(definition.getDefinitionVersions().get(0).getDeploymentId());
				}
				return new WfProcess(processRepository.save(definition));

			} else
				throw new InvalidRequestException("The definition you are trying to edit doesn't belong to your group");
		}
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public WfProcess setActiveVersion(int processId, int versionId) throws InvalidRequestException {
		WorkflowDefinition definition;

		// get the definition throw exception if not found
		try {
			definition = processRepository.getById(processId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process version found with the given id");
		}

		// nothing to check
		if (hasRole(ROLE_ADMIN)) {
			boolean found = false;

			for (DefinitionVersion version : definition.getDefinitionVersions()) {
				if (version.getId() == versionId) {
					version.setStatus(WorkflowDefinitionStatus.ACTIVE.toString());
					definition.setActiveDeploymentId(version.getDeploymentId());
					definition.setKey(ActivitiHelper
							.getProcessDefinitionByDeploymentId(activitiRepositoryService, version.getDeploymentId())
							.getId());
					found = true;
				} else {
					if (version.getStatus().equals(WorkflowDefinitionStatus.ACTIVE.toString())) {
						version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());
					}
				}
			}

			if (!found)
				throw new InvalidRequestException("The process definition version with id: " + versionId
						+ " does not exist in process " + definition.getId());

			return new WfProcess(processRepository.save(definition));

			// since user has role PROCESS_ADMIN should check for group
		} else if (hasRole(ROLE_PROCESS_ADMIN)) {
			if (hasGroup(definition.getOwner())) {
				boolean found = false;

				for (DefinitionVersion version : definition.getDefinitionVersions()) {
					if (version.getId() == versionId) {
						version.setStatus(WorkflowDefinitionStatus.ACTIVE.toString());
						definition.setActiveDeploymentId(version.getDeploymentId());
						definition.setKey(ActivitiHelper.getProcessDefinitionByDeploymentId(activitiRepositoryService,
								version.getDeploymentId()).getId());
						found = true;
					} else {
						if (version.getStatus().equals(WorkflowDefinitionStatus.ACTIVE.toString())) {
							version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());
						}
					}
				}

				if (!found)
					throw new InvalidRequestException("The process definition version with id: " + versionId
							+ " does not exist in process " + definition.getId());

				return new WfProcess(processRepository.save(definition));

			} else
				throw new InvalidRequestException("The definition you are trying to edit doesn't belong to your group");
		}

		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public WfProcessVersion deactivateVersion(int processId, int versionId) throws InvalidRequestException {
		WorkflowDefinition definition = processRepository.getById(processId);
		DefinitionVersion version = definition.getVersion(versionId);

		// nothing to check
		if (hasRole(ROLE_ADMIN)) {
			version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());

			if (definition.getActiveDeploymentId() != null
					&& definition.getActiveDeploymentId().equals(version.getDeploymentId())) {
				definition.setActiveDeploymentId(null);
				processRepository.save(definition);
			}

			return new WfProcessVersion(processRepository.saveVersion(processId, version));

			// check user's groups
		} else if (hasRole(ROLE_PROCESS_ADMIN)) {
			if (hasGroup(definition.getOwner())) {
				version.setStatus(WorkflowDefinitionStatus.INACTIVE.toString());

				if (definition.getActiveDeploymentId() != null
						&& definition.getActiveDeploymentId().equals(version.getDeploymentId())) {
					definition.setActiveDeploymentId(null);
					processRepository.save(definition);
				}

				return new WfProcessVersion(processRepository.saveVersion(processId, version));

			} else
				throw new InvalidRequestException("The definition you are trying to edit doesn't belong to your group");
		}

		return null;
	}

	@Override
	public WfProcess getProcessMetadata(int processId, String device) throws InvalidRequestException {
		WorkflowDefinition definition;

		// get the definition throw exception if not found
		try {
			definition = processRepository.getById(processId);

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("No process version found with the given id");
		}

		WfProcess process = new WfProcess(definition);
		StartFormData startForm = activitiFormService.getStartFormData(definition.getKey());
		BpmnModel bpmnModel = activitiRepositoryService.getBpmnModel(process.getProcessDefinitionId());
		List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
		Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
		UserTaskDetails taskDetail = new UserTaskDetails();

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

	@Override
	public WorkflowSettings getSettings() {
		WorkflowSettings settings = settingsStatus.getWorkflowSettings();

		if (settings == null) {
			settings = processRepository.getSettings();
			settingsStatus.setWorkflowSettings(settings);
		}
		return settings;
	}

	@Override
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
	 * @param settings
	 * @return
	 */
	@Override
	public WorkflowSettings updateSettings(WorkflowSettings settings) {
		settingsStatus.setWorkflowSettings(settings);
		return processRepository.updateSettings(settings);
	}

	@Override
	public boolean claimPermanentAccessToken(FBLoginResponse fbResponse) throws InvalidRequestException {
		String accessPage = fbResponse.getPage();

		if (accessPage == null) {
			throw new InvalidRequestException("No page has been specified");
		}

		WorkflowSettings settings = getSettings();

		Map<String, String> tokensMap = settings.fetchFacebookTokensAsMap();
		if (tokensMap == null)
			tokensMap = new HashMap<String, String>();

		// check if a token exists for the page. If yes, return.
		if (tokensMap.get(accessPage) != null)
			return true;

		PropertyResourceBundle properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");

		String oauthUrl = properties.getString("fb.graphOauthUrl");
		String clientId = properties.getString("fb.clientId");
		String grantType = properties.getString("fb.grantType");
		String clientSecret = properties.getString("fb.clientSecret");
		String graphTokenUrl = properties.getString("fb.graphTokenUrl");

		URI buildLLT = UriBuilder.fromPath(oauthUrl).queryParam("client_id", clientId)
				.queryParam("grant_type", grantType).queryParam("client_secret", clientSecret)
				.queryParam("fb_exchange_token", fbResponse.getAccessToken()).build();

		String url = buildLLT.toString();

		Facebook facebook = new FacebookTemplate(fbResponse.getAccessToken());
		ResponseEntity<String> exchange = facebook.restOperations().exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
				String.class);

		String response = exchange.getBody();
		String longLiveToken = extractFBResponseElement(response, "access_token", "=");

		//if (longLiveToken == null && longLiveToken.isEmpty())
		if (longLiveToken == null)
			return false;

		URI buildPT = UriBuilder.fromPath(graphTokenUrl + fbResponse.getUserID() + "/accounts").queryParam("access_token", longLiveToken).build();

		url = buildPT.toString();

		try {
			exchange = facebook.restOperations().exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);
			
		} catch (RestClientException e) {
			throw new InvalidRequestException("Request failed. Check the facebook " + "connection parameters:: " + e.getMessage());
		}

		response = exchange.getBody();

		JSONObject jObj = new JSONObject(response);
		String data = jObj.getString("data");

		JSONArray jsonArray = jObj.getJSONArray("data");
		Object jsonArrayObject;
		String page = null;
		String permanentToken = null;

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonArrayObject = jsonArray.get(i);
			page = extractFBResponseElement(jsonArrayObject.toString(), "name", ":");
			page = page.substring(1, page.length() - 1);
			if (page.equals(accessPage)) {
				permanentToken = extractFBResponseElement(data, "access_token", ":");
				permanentToken = permanentToken.substring(1, permanentToken.length() - 1);
			}
		}

		if (permanentToken != null) {
			tokensMap.put(accessPage, permanentToken);
			settings.assignFacebookTokensFromMap(tokensMap);
			updateSettings(settings);
			return true;
		}

		return false;
	}

	/**
	 * Private helping method for retrieving elements from facebook api auth
	 * responses
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
				// --
				if (partsParams[1].indexOf("&") > -1) {
					String[] subparts = partsParams[1].split("&");
					return subparts[0];
				}
				// --
				return partsParams[1];
			}
		}
		return null;
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

		BpmnModel bpmnModel = activitiRepositoryService.getBpmnModel(latestDefinitionVersion.getProcessDefinitionId());

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
							activitiRepositoryService, latestDefinitionVersion.getProcessDefinitionId(),
							userTask.getId());

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

	/**
	 * Checks for user tasks with the same name between the currently deployed
	 * and the latest version, so that it is possible to copy the user task form
	 * element description
	 * 
	 * @param elementId
	 * @param taskDetailId
	 * @return
	 */
	private String copyFormElementDescriptionFromSimilar(String elementId, int taskDetailId) {
		List<UserTaskFormElement> userTaskFormElemets = processRepository.getUserTaskFormElements(elementId,
				taskDetailId);

		for (UserTaskFormElement element : userTaskFormElemets) {
			if (element.getElementId().equals(elementId)) {
				if (element.getDescription() != null && !element.getDescription().isEmpty()) {
					return element.getDescription();
				}
			}
		}
		return "";
	}

	/**
	 * Checks for user tasks with the same name between the currently deployed
	 * and the latest version, so that it is possible to copy the user task form
	 * element device
	 * 
	 * @param elementId
	 * @param taskDetailId
	 * @return
	 */
	private String copyFormElementDeviceFromSimilar(String elementId, int taskDetailId) {
		List<UserTaskFormElement> userTaskFormElemets = processRepository.getUserTaskFormElements(elementId,
				taskDetailId);

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
	 * Creates a list of WfFormProperty based on a list of Activiti FormProperty
	 * 
	 * @param formProperties
	 * @return
	 */
	private List<WfFormProperty> getWfFormProperties(List<FormProperty> formProperties) {
		List<WfFormProperty> wfFormProperties = new ArrayList<WfFormProperty>();
		String dateFormat = "";

		for (FormProperty property : formProperties) {
			dateFormat = (String) property.getType().getInformation("datePattern");
			String propertyValue = property.getValue();

			WfFormProperty wfProperty = new WfFormProperty();
			wfProperty.setId(property.getId());
			wfProperty.setName(property.getName());
			wfProperty.setType(property.getType().getName());
			wfProperty.setValue(propertyValue);
			wfProperty.setReadable(property.isReadable());
			wfProperty.setWritable(property.isWritable());
			wfProperty.setFormat(dateFormat);
			// wfProperty.setDescription("");
			// wfProperty.setDevice("");

			wfFormProperties.add(wfProperty);
		}
		return wfFormProperties;
	}

	/**
	 * Checks if a process already exists with the same key
	 * 
	 * @param key
	 * @return
	 */
	private boolean definitionExistenceCheck(String key) {
		List<ProcessDefinition> processDefinitions = activitiRepositoryService.createProcessDefinitionQuery()
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
	 * Parses the bpmn file to retrieve the id of the process.
	 * 
	 * @param bpmn
	 *            as inputstream
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

		return userGroups.contains(group) ? true : false;
	}
}