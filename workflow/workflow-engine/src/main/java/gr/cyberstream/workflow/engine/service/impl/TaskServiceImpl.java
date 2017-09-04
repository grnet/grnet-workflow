package gr.cyberstream.workflow.engine.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cyberstream.workflow.engine.cmis.CMISDocument;
import gr.cyberstream.workflow.engine.cmis.CMISFolder;
import gr.cyberstream.workflow.engine.customtypes.ConversationType;
import gr.cyberstream.workflow.engine.customtypes.DocumentType;
import gr.cyberstream.workflow.engine.customtypes.MessageType;
import gr.cyberstream.workflow.engine.listeners.CustomTaskFormFields;
import gr.cyberstream.workflow.engine.model.*;
import gr.cyberstream.workflow.engine.model.api.*;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.*;
import gr.cyberstream.workflow.engine.service.TaskService;
import org.activiti.bpmn.model.FormValue;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class TaskServiceImpl implements TaskService {

	@Autowired
	private Processes processRepository;

	@Autowired
	private RealmService realmService;

	@Autowired
	private org.activiti.engine.TaskService activitiTaskService;

	@Autowired
	private RepositoryService activitiRepositoryService;

	@Autowired
	private HistoryService activitiHistoryService;

	@Autowired
	private FormService activitiFormService;

	@Autowired
	private CMISFolder cmisFolder;

	@Autowired
	private CMISDocument cmisDocument;

	@Autowired
	private MailService mailService;
	
	@Autowired
	private DefinitionService definitionService;

	@Autowired
	private Environment environment;

	private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

	private static final String ROLE_ADMIN = "ROLE_Admin";
	private static final String ROLE_PROCESS_ADMIN = "ROLE_ProcessAdmin";

	@Override
	public List<WfTaskDetails> getVersionTaskDetails(int versionId) {
		List<UserTaskDetails> taskDetails = processRepository.getVersionTaskDetails(versionId);
		return WfTaskDetails.fromUserTaskDetails(taskDetails);
	}

	@Override
	public List<WfTask> getSupervisedTasks() {
		List<Task> tasks = new ArrayList<Task>();
		List<WfTask> returnList = new ArrayList<WfTask>();

		if (hasRole(ROLE_ADMIN)) {
			tasks = activitiTaskService.createTaskQuery().active().orderByDueDateNullsLast().asc().list();

			for (Task task : tasks) {
				TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());
				WfTask wfTask = new WfTask(task);
				wfTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));
				wfTask.setStartForm(taskPath.getDefinition().hasStartForm());
				hydrateTask(wfTask);
				returnList.add(wfTask);
			}

		} else {
			tasks = activitiTaskService.createTaskQuery().active().orderByDueDateNullsLast().asc().list();

			for (Task task : tasks) {
				TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());

				if (taskPath.getInstance().getSupervisor().equals(retrieveToken().getEmail())) {
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

	@Override
	public List<WfTask> getTasksByInstanceId(String instanceId) {
		List<WfTask> returnList = new ArrayList<WfTask>();
		List<HistoricTaskInstance> historicTasks = new ArrayList<HistoricTaskInstance>();
		historicTasks = activitiHistoryService.createHistoricTaskInstanceQuery().processInstanceId(instanceId).list();

		WorkflowInstance instance = new WorkflowInstance();

		// loop through completed tasks
		for (HistoricTaskInstance task : historicTasks) {
			instance = processRepository.getInstanceById(task.getProcessInstanceId());

			WfTask wfTask = new WfTask(task);
			wfTask.setProcessInstance(new WfProcessInstance(instance));
			wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());

			hydrateTask(wfTask);
			returnList.add(wfTask);
		}
		return returnList;
	}

	@Override
	public List<WfTask> getUserCompletedTasks() {
		List<WfTask> returnList = new ArrayList<WfTask>();
		String assignee = retrieveToken().getEmail();
		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();

		completedUserTasks = activitiHistoryService.createHistoricTaskInstanceQuery().taskAssignee(assignee).orderByHistoricTaskInstanceEndTime().desc().list();

		for (HistoricTaskInstance completedUserTask : completedUserTasks) {
			WorkflowInstance instance = processRepository.getInstanceById(completedUserTask.getProcessInstanceId());

			WfTask wfTask = new WfTask(completedUserTask);
			wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
			wfTask.setProcessInstance(new WfProcessInstance(instance));

			returnList.add(wfTask);
		}
		
		return returnList;
	}

	@Override
	public List<WfTask> searchCompletedTasks(String definitionKey, String instanceTitle, long after, long before, String isSupervisor) throws InvalidRequestException {
		List<WfTask> returnList = new ArrayList<WfTask>();
		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);
		List<HistoricTaskInstance> taskList = new ArrayList<HistoricTaskInstance>();
		WorkflowInstance instance = new WorkflowInstance();

		// show tasks for user
		if (isSupervisor.equals("false")) {
			// Process defintion id == process definition key
			HistoricTaskInstanceQuery query = activitiHistoryService.createHistoricTaskInstanceQuery();

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
					// set criteria to query

					if (processInstanceIds != null || processInstanceIds.size() == 0)
						query.processInstanceIdIn(processInstanceIds);

				} else
					query.processDefinitionId(definitionKey);
			}

			if (dateAfter != null)
				query = query.taskCompletedAfter(dateAfter);

			if (dateBefore != null)
				query = query.taskCompletedBefore(dateBefore);

			taskList = query.taskAssignee(retrieveToken().getEmail()).list();

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
						wfTask.setDefinitionName(instance.getDefinitionVersion().getWorkflowDefinition().getName());
						wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
						returnList.add(wfTask);
					}
				}
			}

			// show supervised tasks
		} else if (isSupervisor.equals("true")) {

			HistoricTaskInstanceQuery query = activitiHistoryService.createHistoricTaskInstanceQuery();

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

					if (processInstanceIds != null || processInstanceIds.size() == 0) {
						query.processInstanceIdIn(processInstanceIds);
					}

				} else
					query.processDefinitionId(definitionKey);

			}

			if (dateAfter != null)
				query = query.taskCompletedAfter(dateAfter);

			if (dateBefore != null)
				query = query.taskCompletedBefore(dateBefore);

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
							wfTask.setDefinitionName(instance.getDefinitionVersion().getWorkflowDefinition().getName());
							wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
							returnList.add(wfTask);
						}
					}
					// user is not admin or process admin
				} else {
					if (instance != null) {
						if (StringUtils.containsIgnoreCase(instance.getTitle(), instanceTitle)
								&& instance.getSupervisor().equals(retrieveToken().getEmail())) {

							WfTask wfTask = new WfTask(task);
							wfTask.setProcessInstance(new WfProcessInstance(instance));
							wfTask.setDefinitionName(instance.getDefinitionVersion().getWorkflowDefinition().getName());
							wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
							returnList.add(wfTask);
						}
					}

				} // end of check roles
			}
		}
		return returnList;
	}

	@Override
	public List<WfTask> getUserCompledTasksByInstanceIds(List<String> instanceIds) {
		List<WfTask> returnList = new ArrayList<WfTask>();
		String assignee = retrieveToken().getEmail();
		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();

		completedUserTasks = activitiHistoryService.createHistoricTaskInstanceQuery().processInstanceIdIn(instanceIds)
				.taskAssignee(assignee).orderByHistoricTaskInstanceEndTime().desc().list();

		for (HistoricTaskInstance completedUserTask : completedUserTasks) {
			DefinitionVersion definitionVersion = processRepository.getVersionByProcessDefinitionId(completedUserTask.getProcessDefinitionId());
			WorkflowInstance instance = processRepository.getInstanceById(completedUserTask.getProcessInstanceId());

			WfTask wfTask = new WfTask(completedUserTask);
			wfTask.setIcon(definitionVersion.getWorkflowDefinition().getIcon());
			wfTask.setProcessInstance(new WfProcessInstance(instance));
			returnList.add(wfTask);
		}
		return returnList;
	}

	@Override
	public List<WfTask> getCompletedTasksByInstances(List<String> instanceIds) {
		List<WfTask> returnList = new ArrayList<WfTask>();
		List<HistoricTaskInstance> completedUserTasks = new ArrayList<HistoricTaskInstance>();

		completedUserTasks = activitiHistoryService.createHistoricTaskInstanceQuery().processInstanceIdIn(instanceIds).orderByHistoricTaskInstanceEndTime().desc().list();

		for (HistoricTaskInstance completedUserTask : completedUserTasks) {
			DefinitionVersion definitionVersion = processRepository.getVersionByProcessDefinitionId(completedUserTask.getProcessDefinitionId());
			WorkflowInstance instance = processRepository.getInstanceById(completedUserTask.getProcessInstanceId());

			WfTask wfTask = new WfTask(completedUserTask);
			wfTask.setIcon(definitionVersion.getWorkflowDefinition().getIcon());
			wfTask.setProcessInstance(new WfProcessInstance(instance));
			returnList.add(wfTask);
		}
		return returnList;
	}

	@Override
	public WfTask getTask(String taskId) throws InvalidRequestException {
		HistoricTaskInstance task = activitiHistoryService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();

		if (task == null)
			throw new InvalidRequestException("noTaskWithID");

		WfTask wfTask = new WfTask(task);
		WorkflowInstance taskInstance = processRepository.getInstanceById(task.getProcessInstanceId());
		wfTask.setStartForm(taskInstance.getDefinitionVersion().getWorkflowDefinition().hasStartForm());

		hydrateTask(wfTask);

		wfTask.setProcessInstance(new WfProcessInstance(taskInstance));
		wfTask.setIcon(taskInstance.getDefinitionVersion().getWorkflowDefinition().getIcon());
		List<UserTaskDetails> taskDetails = processRepository.getUserTaskDetailsByDefinitionKey(task.getTaskDefinitionKey(), taskInstance.getDefinitionVersion().getId());

		for (UserTaskDetails details : taskDetails) {
			wfTask.setTaskDetails(new WfTaskDetails(details));
		}
		return wfTask;
	}

	@Override
	public List<WfFormProperty> getTaskFormPropertiesByTaskDefintionKey(String taskDefinitionKey, String processDefinitionId) throws InvalidRequestException {
		List<WfFormProperty> returnList = new ArrayList<WfFormProperty>();
		List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
		Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
		UserTaskDetails taskDetails = new UserTaskDetails();
		List<org.activiti.bpmn.model.FormProperty> formProperties = new ArrayList<org.activiti.bpmn.model.FormProperty>();

		//
		try {
			formProperties = ActivitiHelper.getTaskFormDefinition(activitiRepositoryService, processDefinitionId, taskDefinitionKey);
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

				WfFormProperty WfFormProperty = new WfFormProperty();
				WfFormProperty.setId(formPropery.getId());
				WfFormProperty.setName(formPropery.getName());
				WfFormProperty.setType(formPropery.getType());
				WfFormProperty.setValue("");
				WfFormProperty.setReadable(formPropery.isReadable());
				WfFormProperty.setWritable(formPropery.isWriteable());
				WfFormProperty.setRequired(formPropery.isRequired());
				WfFormProperty.setFormValues(values);
				WfFormProperty.setFormat(dateFormat);
				WfFormProperty.setDescription(userTaskFormElement.getDescription());
				WfFormProperty.setDevice(userTaskFormElement.getDevice());

				returnList.add(WfFormProperty);
			}

		} catch (Exception e) {

			formProperties = ActivitiHelper.getTaskFormDefinition(activitiRepositoryService, processDefinitionId);
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

	@Override
	public void completeTask(WfTask task) throws InvalidRequestException {
		String assignee = retrieveToken().getEmail();

		// check if task has the same assignee as the person requests to
		// complete it or if that person has role admin
		if (task.getAssignee().equals(assignee) || hasRole(ROLE_ADMIN)) {
			try {

				if (task.getTaskForm() != null) {

					for (WfFormProperty property : task.getTaskForm()) {

						if (property.getType().equals("conversation")) {

							property.setValue(fixConversationMessage(property.getValue(), assignee));
						}
					}

					Map<String, String> variableValues = task.getVariableValues();

					if (variableValues != null && !variableValues.isEmpty()) {

						activitiFormService.saveFormData(task.getId(), variableValues);
					}

					activitiTaskService.complete(task.getId());

				}
			} catch (ActivitiException e) {

				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}
			// task's assignee not matched with the person who requests to
			// complete it or not admin
		} else
			throw new InvalidRequestException("notAuthorizedToCompleteTask");
	}

	@Override
	public void completeTask(WfTask task, MultipartFile[] files) throws InvalidRequestException {
		String userId = retrieveToken().getEmail();
		String user = retrieveToken().getName();
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

							Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(),file.getContentType());
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

				activitiFormService.saveFormData(task.getId(), variableValues);
			}

			activitiTaskService.complete(task.getId());

		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}

	@Override
	public void tempTaskSave(WfTask task, MultipartFile[] files) throws InvalidRequestException {
		String userId = retrieveToken().getEmail();
		String user = retrieveToken().getName();
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

				activitiFormService.saveFormData(task.getId(), variableValues);
			}

		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}

	@Override
	public void tempTaskSave(WfTask task) throws InvalidRequestException {
		try {
			activitiFormService.saveFormData(task.getId(), task.getVariableValues());

		} catch (ActivitiException e) {
			logger.error(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
	}

	@Override
	public List<WfUser> getCandidatesByTaskId(String taskId) {
		List<WfUser> candidates = new ArrayList<WfUser>();
		List<WfUser> tempList = new ArrayList<WfUser>();
		List<IdentityLink> links = activitiTaskService.getIdentityLinksForTask(taskId);

		if (links.size() == 0 || links == null) {
			for (WfUser user : realmService.getAllUsers()) {

				if (user.getEmail() != null)
					user.setPendingTasks(activitiTaskService.createTaskQuery().active().taskAssignee(user.getEmail()).count());

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
							logger.info("Error getting groups " + term1 + " " + e.getMessage());
							continue;
						}
						
						for (WfUser user : tempList) {
							user.setPendingTasks(activitiTaskService.createTaskQuery().active().taskAssignee(user.getEmail()).count());

							if (!candidates.contains(user))
								candidates.add(user);
						}
						logger.info("Getting candidates for Role: other " + term1);

					} else {
						// group only (term1)
						for (WfUser user : realmService.getUsersByGroup(term1)) {
							user.setPendingTasks(activitiTaskService.createTaskQuery().active().taskAssignee(user.getEmail()).count());

							if (!candidates.contains(user))
								candidates.add(user);
						}
						logger.info("Getting candidates for Group: " + term1);
					}
				} else if (term1.isEmpty() && !term2.isEmpty()) {
					// role only (term2)
					for (WfUser user : realmService.getUsersByRole(term2)) {
						user.setPendingTasks(activitiTaskService.createTaskQuery().active().taskAssignee(user.getEmail()).count());

						if (!candidates.contains(user))
							candidates.add(user);
					}
					logger.info("Getting candidates for Role: test " + term2);

				} else {
					// term1 = group, term2: role
					for (WfUser user : realmService.getUsersByGroupAndRole(term1, term2)) {
						user.setPendingTasks(activitiTaskService.createTaskQuery().active().taskAssignee(user.getEmail()).count());

						if (!candidates.contains(user))
							candidates.add(user);
					}
					logger.info("Getting candidates for User Group : " + term1 + " and Role: " + term2);
				}
			}
		}
		return candidates;
	}

	@Override
	public List<WfTask> getTasksForUser() {
		List<WfTask> returnList = new ArrayList<WfTask>();
		List<Task> taskList = new ArrayList<Task>();
		String userId = retrieveToken().getEmail();

		// Getting tasks for user
		taskList = activitiTaskService.createTaskQuery().orderByTaskCreateTime().taskAssignee(userId).asc().list();

		for (Task task : taskList) {
			TaskPath taskPath = processRepository.getTaskPath(task.getProcessInstanceId(), task.getTaskDefinitionKey());
			WfTask wfTask = new WfTask(task);
			wfTask.setStartForm(taskPath.getDefinition().hasStartForm());
			wfTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));

			returnList.add(hydrateTask(wfTask));
		}
		return returnList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void assignTask(WfTask wfTask, String assigneeId) throws InvalidRequestException {
		String userId = retrieveToken().getEmail();

		// check if task is supervised by the person who request to assign the
		// task or if is admin
		if (wfTask.getProcessInstance().getSupervisor().equals(userId) || hasRole(ROLE_ADMIN)) {
			try {
				if (wfTask.getTaskForm() != null) {
					for (WfFormProperty property : wfTask.getTaskForm()) {

						if (property.getType().equals("conversation"))
							property.setValue(fixConversationMessage(property.getValue(), userId));
					}
				}
				activitiTaskService.claim(wfTask.getId(), assigneeId);
				mailService.sendTaskAssignedMail(assigneeId, wfTask);

			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}
			// the person who request to assign the task, is not supervisor for
			// the task or admin
		} else
			throw new InvalidRequestException("noAuthorizedToAssignTask");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void assignTask(WfTask wfTask, String assigneeId, MultipartFile[] files) throws InvalidRequestException {
		String userId = retrieveToken().getEmail();
		String user = retrieveToken().getName();

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

								Document document = saveOrUpdateDocument(folder, wfDocument, file.getInputStream(), file.getContentType());
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

						} else if (property.getType().equals("conversation"))
							property.setValue(fixConversationMessage(property.getValue(), userId));
					}
				}
				activitiTaskService.claim(wfTask.getId(), assigneeId);
				mailService.sendTaskAssignedMail(assigneeId, wfTask);

			} catch (ActivitiException e) {
				logger.error(e.getMessage());
				throw new InvalidRequestException(e.getMessage());
			}
			// the person who request to assign the task, is not supervisor for
			// the task or admin
		} else
			throw new InvalidRequestException("noAuthorizedToAssignTask");
	}

	@Override
	public void unClaimTask(String taskId) throws InvalidRequestException {
		String user = retrieveToken().getEmail();
		try {
			Task task = activitiTaskService.createTaskQuery().taskId(taskId).singleResult();
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());

			if (instance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
				throw new InvalidRequestException("claimTaskInstanceSuspended");

			if (instance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
				throw new InvalidRequestException("claimTaskInstanceDeleted");

			if (instance.getStatus().equals(WorkflowInstance.STATUS_ENDED))
				throw new InvalidRequestException("claimTaskInstanceEnded");

			if (instance.getSupervisor().equals(user) || task.getAssignee().equals(user) || hasRole(ROLE_ADMIN)) {
					activitiTaskService.unclaim(taskId);
			} else
				throw new InvalidRequestException("noAuthorizedToUnclaim");
		} catch (Exception e) {
			logger.error(e.getMessage());
			if(!(e instanceof InvalidRequestException)){
				throw new InvalidRequestException("claimTaskInstanceNotActive");
			} else
				throw new InvalidRequestException(e.getMessage());
		}
	}

	@Override
	public void claimTask(String taskId) throws InvalidRequestException {
		try {
			Task task = activitiTaskService.createTaskQuery().taskId(taskId).singleResult();
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());

			if (instance.getStatus().equals(WorkflowInstance.STATUS_SUSPENDED))
				throw new InvalidRequestException("claimTaskInstanceSuspended");

			if (instance.getStatus().equals(WorkflowInstance.STATUS_DELETED))
				throw new InvalidRequestException("claimTaskInstanceDeleted");

			if (instance.getStatus().equals(WorkflowInstance.STATUS_ENDED))
				throw new InvalidRequestException("claimTaskInstanceEnded");

			String assignee = retrieveToken().getEmail();
			activitiTaskService.claim(taskId, assignee);
		} catch (Exception e) {
			logger.error(e.getMessage());
			if(!(e instanceof InvalidRequestException)){
				throw new InvalidRequestException("claimTaskInstanceNotActive");
			} else
				throw new InvalidRequestException(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WfTask> getCandidateUserTasks() {
		// list to store tasks according to user role/group
		List<Task> taskList = new ArrayList<Task>();

		// final list with tasks to be claimed by user
		List<WfTask> returnList = new ArrayList<WfTask>();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();
		
		Set<String> userRoles = new HashSet<>();
		
		for (GrantedAuthority authority : authorities) {
			userRoles.add(authority.getAuthority());
		}
		
		//Set<String> userRoles = retrieveToken().getRealmAccess().getRoles();

		// get active tasks
		List<Task> tasks = activitiTaskService.createTaskQuery().active().taskUnassigned().orderByTaskId().asc().orderByProcessInstanceId().asc().list();

		List<String> userGroups = realmService.getUserGroups();

		// filter tasks according to group and roles
		for (Task task : tasks) {
			DefinitionVersion definitionVersion = processRepository.getVersionByProcessDefinitionId(task.getProcessDefinitionId());

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
			if (!taskPath.getTaskDetails().isAssign()) {
				WfTask hydratedTask = hydrateTask(new WfTask(task));
				hydratedTask.setStartForm(taskPath.getDefinition().hasStartForm());
				hydratedTask.setProcessInstance(new WfProcessInstance(taskPath.getInstance()));
				returnList.add(hydratedTask);
			}
		}
		return returnList;
	}

	@Override
	@Transactional
	public WfTaskDetails updateTaskDetails(WfTaskDetails wfTaskDetails) throws InvalidRequestException {
		UserTaskDetails taskDetails;

		try {
			taskDetails = processRepository.getUserTaskDetailsById(wfTaskDetails.getId());

		} catch (EmptyResultDataAccessException e) {
			throw new InvalidRequestException("noTaskDetailsEntity");
		}

		if (hasRole(ROLE_ADMIN)) {
			taskDetails.updateFrom(wfTaskDetails);
			taskDetails = processRepository.save(taskDetails);

		} else if (hasGroup(taskDetails.getDefinitionVersion().getWorkflowDefinition().getOwner())) {
			taskDetails.updateFrom(wfTaskDetails);
			taskDetails = processRepository.save(taskDetails);

		} else
			throw new InvalidRequestException("notAuthorizedToUpdateTaskDetails");

		return new WfTaskDetails(taskDetails);
	}

	@Override
	public List<WfTask> getAllActiveTasks() {
		List<WfTask> wfTasks = new ArrayList<WfTask>();

		// get all active tasks
		List<Task> tasks = activitiTaskService.createTaskQuery().active().list();

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
	 * Returns active tasks by given criteria
	 *
	 * @param definitionName
	 * @param taskName
	 * @param after
	 * @param before
	 *
	 * @return
	 */
	@Transactional
	public List<WfTask> getActiveTasks(String definitionName, String taskName, long after, long before) {
		List<WfTask> wfTasks = new ArrayList<>();

		// get all active tasks
		List<Task> tasks = activitiTaskService.createTaskQuery().active().list();

		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);

		if(definitionName.isEmpty() || definitionName.equals("all"))
			definitionName = null;
		else
			definitionName = processRepository.getDefinitionByKey(definitionName).getName();

		if(taskName.isEmpty() || taskName.equals(" "))
			taskName = null;

		for (Task task : tasks) {
			if(dateBefore.getTime() == 0)
				dateBefore = new Date();

			if((taskName != null && !taskName.toLowerCase().equals(task.getName().toLowerCase()))||
					!task.getCreateTime().after(dateAfter) ||
					!task.getCreateTime().before(dateBefore))
				continue;

			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());

			if(hasRole(ROLE_PROCESS_ADMIN) && !hasGroup(instance.getDefinitionVersion().getWorkflowDefinition().getOwner()))
				continue;

			if(definitionName == null ||
					definitionName.toLowerCase().equals(instance.getDefinitionVersion().getWorkflowDefinition().
							getName().toLowerCase())){
				WfTask wfTask = new WfTask(task);
				wfTask.setIcon(instance.getDefinitionVersion().getWorkflowDefinition().getIcon());
				wfTask.setDefinitionName(instance.getDefinitionVersion().getWorkflowDefinition().getName());
				wfTask.setProcessInstance(new WfProcessInstance(instance));

				wfTasks.add(wfTask);
			}
		}

		return wfTasks;
	}

	@Override
	public List<WfTask> getEndedProcessInstancesTasks(String title, long after, long before, boolean anonymous) {
		List<WfTask> wfTasks = new ArrayList<WfTask>();
		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);
		WorkflowInstance instance;
		String assignee = (anonymous) ? null : retrieveToken().getEmail();
		List<HistoricTaskInstance> historicTasks = activitiHistoryService.createHistoricTaskInstanceQuery().taskAssignee(assignee).processFinished().list();

		if (title.isEmpty() || title.equals(" "))
			title = null;

		for (HistoricTaskInstance hit : historicTasks) {

			try {
				instance = processRepository.getInstanceById(hit.getProcessInstanceId());

			} catch (EmptyResultDataAccessException e) {
				instance = null;
			}

			if (instance != null) {
				if (instance.getStatus().equals(WorkflowInstance.STATUS_ENDED)
						&& (title == null || instance.getTitle().indexOf(title) > -1)
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

	@Override
	public List<WfTask> getUserActivity(long after, long before, String userId) throws InvalidRequestException {
		List<WfTask> wfTasks = new ArrayList<WfTask>();
		WorkflowInstance instance = null;
		Date dateAfter = new Date(after);
		Date dateBefore = new Date(before);
		WfUser user = realmService.getUser(userId);

		if (user == null)
			throw new InvalidRequestException("No user exists with id " + userId);

		String assignee = user.getEmail();

		List<HistoricTaskInstance> historicTasks = activitiHistoryService.createHistoricTaskInstanceQuery()
				.taskAssignee(assignee).taskCreatedAfter(dateAfter).taskCreatedBefore(dateBefore).list();

		for (HistoricTaskInstance hit : historicTasks) {
			WfTask wfTask = new WfTask(hit);

			try {
				instance = processRepository.getInstanceById(hit.getProcessInstanceId());

			} catch (Exception e) {

			}

			// hydrate task with extra info
			hydrateTask(wfTask);

			// also set instance since its used
			if (instance != null)
				wfTask.setProcessInstance(new WfProcessInstance(instance));

			// finally add task if not already exists to return list
			if (!wfTasks.contains(wfTask))
				wfTasks.add(wfTask);
		}
		return wfTasks;
	}

	@Override
	@Transactional
	public void applyTaskSettings(Task task) {
		WorkflowSettings settings = definitionService.getSettings();

		List<WfUser> users = this.getCandidatesByTaskId(task.getId());

		if ((users == null || users.isEmpty()) && task.getAssignee() == null) {
			String adminEmail = environment.getProperty("mail.admin");
			WorkflowDefinition workflowDef = processRepository.getProcessByDefinitionId(task.getProcessDefinitionId());
			WorkflowInstance instance = processRepository.getInstanceById(task.getProcessInstanceId());
			mailService.sendBpmnErrorEmail(adminEmail, workflowDef, task, instance);
			return;
		}

		if (!settings.isAutoAssignment() || users.size() > 1)
			return;

		String userEmail = users.get(0).getEmail();

		activitiTaskService.claim(task.getId(), userEmail);

		if (settings.isAssignmentNotification())
			mailService.sendTaskAssignedMail(userEmail, new WfTask(task));
	}

	@Override
	@Transactional
	public UserTaskFormElement saveTaskFormElement(WfFormProperty wfFormProperty, String taskDefinitionKey, String definitionVersion) {
		UserTaskFormElement taskFormElement = processRepository.getUserTaskFromElement(definitionVersion, taskDefinitionKey, wfFormProperty.getId());

		taskFormElement.setDescription(wfFormProperty.getDescription());
		taskFormElement.setDevice(wfFormProperty.getDevice());
		return processRepository.save(taskFormElement);
	}

	public List<WfDocument> getProcessInstanceDocumentsByTask(int id) throws InvalidRequestException {

		Task task = activitiTaskService.createTaskQuery().taskId("" + id).singleResult();

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
	 * Helper function to get links from task
	 * 
	 * @param taskId
	 * @return
	 */
	private List<String[]> getCandidateGroupAndRole(String taskId) {
		List<String[]> result = new ArrayList<String[]>();
		String[] groupAndRole;
		List<IdentityLink> links = activitiTaskService.getIdentityLinksForTask(taskId);

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

	private Document saveOrUpdateDocument(Folder folder, WfDocument wfDocument, InputStream inputStream,
			String contentType) throws InvalidRequestException {
		Document document = null;
		logger.info("Saving document " + wfDocument.getTitle() + " document.");

		if (wfDocument.getDocumentId() != null) {
			document = cmisDocument.updateDocumentById(wfDocument.getDocumentId(), wfDocument.getTitle(), contentType, inputStream);
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

	/**
	 * 
	 * @param conversationValue
	 * @param userId
	 * @return
	 */
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
	 * Hydrates Task with extra information
	 * 
	 * @param wfTask
	 * @return
	 */
	private WfTask hydrateTask(WfTask wfTask) {
		List<WfFormProperty> formProperties = new ArrayList<WfFormProperty>();

		// for task which is running
		try {
			TaskFormData taskForm = activitiFormService.getTaskFormData(wfTask.getId());
			formProperties = getWfFormProperties(taskForm.getFormProperties(), wfTask);
		}

		// for task which is completed
		catch (ActivitiObjectNotFoundException e) {
			List<HistoricDetail> historicDetails = activitiHistoryService.createHistoricDetailQuery().formProperties().taskId(wfTask.getId()).list();
			HistoricTaskInstance historicTaskInstance = activitiHistoryService.createHistoricTaskInstanceQuery().taskId(wfTask.getId()).singleResult();
			List<org.activiti.bpmn.model.FormProperty> historicFormProperties = new ArrayList<org.activiti.bpmn.model.FormProperty>();
			Map<String, String> propertyValueMap = new LinkedHashMap<String, String>();
			List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
			Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();
			UserTaskDetails taskDetails = new UserTaskDetails();

			// get properties for task
			historicFormProperties = ActivitiHelper.getTaskFormDefinition(activitiRepositoryService,wfTask.getProcessDefinitionId(), historicTaskInstance.getTaskDefinitionKey());

			// fill the map using as key the property id and as value the
			// property value
			for (HistoricDetail detail : historicDetails) {
				HistoricFormProperty historicFormProperty = (HistoricFormProperty) detail;
				propertyValueMap.put(historicFormProperty.getPropertyId(), historicFormProperty.getPropertyValue());
			}

			// get the task details
			taskDetails = processRepository.getUserTaskDetailByDefinitionKey(historicTaskInstance.getTaskDefinitionKey(), wfTask.getProcessDefinitionId());

			// get the task form elements
			taskFormElements = processRepository.getUserTaskFromElements(wfTask.getProcessDefinitionId(), taskDetails.getId());

			// fill the usertaskform element map using as key the element id and
			// as value the user taskform element
			for (UserTaskFormElement userTaskFormElement : taskFormElements) {
				mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
			}

			// loop through form properties
			for (org.activiti.bpmn.model.FormProperty formPropery : historicFormProperties) {
				String propertyValue = propertyValueMap.get(formPropery.getId());

				// prepare formValues
				Map<String, String> values = new HashMap<String, String>();

				// date pattern
				String dateFormat = "";

				if (formPropery.getType().equals("enum")) {
					List<FormValue> formValues = formPropery.getFormValues();

					for (int i = 0; formValues != null && i < formValues.size(); i++) {
						values.put(formValues.get(i).getId(), formValues.get(i).getName());
					}
				}

				if (formPropery.getDatePattern() != null)
					dateFormat = formPropery.getDatePattern();
				else
					dateFormat = CustomTaskFormFields.DATETIME_PATTERN_PRESENTATION;

				WfFormProperty wfFormProperty = new WfFormProperty();
				wfFormProperty.setId(formPropery.getId());
				wfFormProperty.setName(formPropery.getName());
				wfFormProperty.setType(formPropery.getType());
				wfFormProperty.setValue(propertyValue);
				wfFormProperty.setReadable(formPropery.isReadable());
				wfFormProperty.setWritable(formPropery.isWriteable());
				wfFormProperty.setRequired(formPropery.isRequired());
				wfFormProperty.setFormValues(values);
				wfFormProperty.setFormat(dateFormat);
				wfFormProperty.setDescription(mappedUserTaskFormElements.get(formPropery.getId()).getDescription());
				wfFormProperty.setDevice(mappedUserTaskFormElements.get(formPropery.getId()).getDevice());

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
	 * 
	 * @param formProperties
	 * @param wfTask
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<WfFormProperty> getWfFormProperties(List<FormProperty> formProperties, WfTask wfTask) {
		List<WfFormProperty> wfFormProperties = new ArrayList<WfFormProperty>();
		Task task = activitiTaskService.createTaskQuery().taskId(wfTask.getId()).singleResult();
		UserTaskDetails taskDetails = processRepository.getUserTaskDetailByDefinitionKey(task.getTaskDefinitionKey(), wfTask.getProcessDefinitionId());
		List<UserTaskFormElement> taskFormElements = new ArrayList<UserTaskFormElement>();
		taskFormElements = processRepository.getUserTaskFromElements(wfTask.getProcessDefinitionId(), taskDetails.getId());
		Map<String, UserTaskFormElement> mappedUserTaskFormElements = new HashMap<>();

		// create the map
		for (UserTaskFormElement userTaskFormElement : taskFormElements) {
			mappedUserTaskFormElements.put(userTaskFormElement.getElementId(), userTaskFormElement);
		}

		for (FormProperty property : formProperties) {
			String propertyValue = property.getValue();
			String dateFormat = (String) property.getType().getInformation("datePattern");

			UserTaskFormElement userTaskFormElement = null;
			if (!mappedUserTaskFormElements.isEmpty()) {
				userTaskFormElement = mappedUserTaskFormElements.get(property.getId());
			}

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
			wfFormProperty.setDescription(userTaskFormElement.getDescription());
			wfFormProperty.setDevice(userTaskFormElement.getDevice());

			wfFormProperties.add(wfFormProperty);
		}
		return wfFormProperties;
	}

	/**
	 * Private method for retrieving logged user token
	 * 
	 * @return Logged-in user's token
	 */
	private AccessToken retrieveToken() {
		KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		AccessToken token = authentication.getAccount().getKeycloakSecurityContext().getToken();

		return token;
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

		return userGroups.contains(group) ? true : false;
	}

	/**
	 * This function is called when there is no candidate for a specific task.
	 *
	 * @param taskId
	 *            The ID of the task that has no candidates
	 */
	public void notifyAdminForTask(String taskId, String username) throws InvalidRequestException {
		String adminEmail = environment.getProperty("mail.admin");
		Task workflowTask = activitiTaskService.createTaskQuery().taskId(taskId).singleResult();

		try {
			mailService.sendNoCandidatesErrorEmail(adminEmail, workflowTask, username);
		} catch (InternalException e) {
			throw new InvalidRequestException("emailNotSentContactAdmin");
		}
	}
}